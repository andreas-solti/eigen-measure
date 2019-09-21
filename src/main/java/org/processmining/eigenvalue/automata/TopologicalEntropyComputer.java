package org.processmining.eigenvalue.automata;

import dk.brics.automaton2.Automaton;
import dk.brics.automaton2.State;
import dk.brics.automaton2.Transition;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import no.uib.cipr.matrix.DenseVectorSub;
import no.uib.cipr.matrix.sparse.ArpackGen;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.jbpt.utils.IOUtils;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.projectedrecallandprecision.helperclasses.AcceptingPetriNet2automaton;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.helperclasses.ProjectPetriNetOntoActivities;

import java.util.*;
import java.util.logging.Logger;

public class TopologicalEntropyComputer {

    private static final Logger logger = Logger.getLogger(TopologicalEntropyComputer.class.getName());
    private static final double EPSILON = 0.000000000001;

    /**
     * Set this to true to see intermediate results as dot files
     */
    public static boolean DEBUG_AUTOMATA_TO_DISK = false;

    /**
     * Computes topological entropy of a Petri net.
     * The topogical entropy is defined as the logarithm of the largest eigenvalue of the transition matrix
     * of the automaton of the statespace of the net.
     *
     * @param context {@link PluginContext}
     * @param net     {@link Petrinet}
     * @return EntropyResult
     */
    public static EntropyResult getTopologicalEntropy(final PluginContext context, Petrinet net) {
//        TestUtils.showModel(net);
        ProMCanceller canceller = new ProMCanceller() {
            @Override
            public boolean isCancelled() {
                if (context != null && context.getProgress() != null) {
                    return context.getProgress().isCancelled();
                }
                return false;
            }
        };
        if (context != null && context.getProgress() != null) {
            context.getProgress().setMaximum(100);
            context.getProgress().setValue(5);
        }
        AcceptingPetriNet acceptingPetriNet = new AcceptingPetriNetImpl(net);
        Set<String> transitionNames = new HashSet<String>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition t : acceptingPetriNet.getNet().getTransitions()) {
            if (!t.isInvisible() && t.getLabel() != null) {
                transitionNames.add(t.getLabel());
            }
        }
        String[] names = new String[transitionNames.size()];
        Iterator<String> iter = transitionNames.iterator();
        int k = 0;
        while (iter.hasNext()) {
            names[k++] = iter.next();
        }

        // compute entropy of the net
        AcceptingPetriNet projectedNet = ProjectPetriNetOntoActivities.project(acceptingPetriNet, canceller, names);
        EntropyResult entM = null;
        try {
            dk.brics.automaton2.Automaton a = AcceptingPetriNet2automaton.convert(projectedNet, Integer.MAX_VALUE, canceller);
            entM = getTopologicalEntropy(a, "M " + net.getLabel(), canceller);

        } catch (AutomatonFailedException e) {
            e.printStackTrace();
        }
        return entM;
    }

    public static EntropyResult getTopologicalEntropy(Automaton a, String name, ProMCanceller canceller) {
        return getTopologicalEntropy(a, name, canceller, 1.0);
    }

    /**
     * @param a                  automaton of which to compute the largest eigenvalue
     * @param name               String a name
     * @param canceller          allows to cancel the execution, if it takes too long
     * @param shortCircuitFactor sets the edge degree of the short circuiting (setting to 0 avoids short-circuiting)
     * @return EntropyResult
     */
    public static EntropyResult getTopologicalEntropy(Automaton a, String name, ProMCanceller canceller, double shortCircuitFactor) {
        if (a == null || a.getStates().size() == 0) {
            return new EntropyResult(name, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 0.00001);
        }
        AutomatonMinimization minimize = new AutomatonMinimization(a, name, canceller).minimize();
        long timeNow = System.currentTimeMillis();
        double largestEigenvalue = -1;
        long timeMatrixConversion;
        boolean converged = true;
        if (a.getStates().size() < 5) {
            SparseRealMatrix matrix = getSparseApacheMathMatrix(a, shortCircuitFactor);
            timeMatrixConversion = System.currentTimeMillis() - timeNow;
            timeNow = System.currentTimeMillis();
            EigenDecomposition decomp = new EigenDecomposition(matrix);
            largestEigenvalue = getMax(decomp.getRealEigenvalues());
        } else {
            MatrixAndDegreeStats matrixAndStats = getCompressedSparseMatrix(a, shortCircuitFactor);
            double lowerBound = Math.max(matrixAndStats.avgDegree, Math.sqrt(matrixAndStats.getMaxDegree()));
            double upperBound = matrixAndStats.getMaxDegree();

            timeMatrixConversion = System.currentTimeMillis() - timeNow;
            timeNow = System.currentTimeMillis();

            if (lowerBound >= upperBound - EPSILON) {
                // no need to compute eigenvalues!
                largestEigenvalue = lowerBound;
            } else {
                logger.info("possible range from " + lowerBound + " to " + upperBound + " (tolerance: " + (upperBound - lowerBound) + ")");
                try {
                    ArpackGen generalArpack = new ArpackGen(matrixAndStats.getMatrix());
                    Map<Double, DenseVectorSub> result = generalArpack.solve(Math.min(matrixAndStats.getMatrix().numColumns() / 2, 10), ArpackGen.Ritz.LR);
                    if (!result.isEmpty()) {
                        largestEigenvalue = result.keySet().iterator().next();
                    } else {
                        largestEigenvalue = -1;
                        converged = false;
                    }
                    logger.info(lowerBound + " <= " + largestEigenvalue + " <= " + upperBound);
                } catch (IllegalStateException e) {
                    logger.warning("Debug this!");
                }
            }
        }
        return new EntropyResult(minimize.automatonSizeOrig, minimize.automatonSizeDeterministic, minimize.automatonSizeMinimal,
                minimize.timeDeterminize, minimize.timeMinimize, largestEigenvalue, FastMath.log(2, largestEigenvalue),
                System.currentTimeMillis() - timeNow, timeMatrixConversion, converged, ArpackGen.convergedTolerance);
    }

    /**
     * Creates a compressed column vector form representation of the matrix.
     * {@link CompColMatrix} stores information in one single array into which each column with only non-zero entries is
     * linearized column after column. Each column can have a different number of non-zero entries.
     *
     * @param a {@link Automaton}
     * @return CompColMatrix sparse representation (can handle huge models)
     */
    public static MatrixAndDegreeStats getCompressedSparseMatrix(Automaton a) {
        return getCompressedSparseMatrix(a, 1.0);
    }

    /**
     * Creates a compressed column vector form representation of the matrix.
     * {@link CompColMatrix} stores information in one single array into which each column with only non-zero entries is
     * linearized column after column. Each column can have a different number of non-zero entries.
     *
     * @param a {@link Automaton}
     * @return CompColMatrix sparse representation (can handle huge models)
     */
    public static MatrixAndDegreeStats getCompressedSparseMatrix(Automaton a, double shortCircuitFactor) {
        DescriptiveStatistics degreeStats = new DescriptiveStatistics();

        TIntObjectMap<TIntDoubleMap> map = new TIntObjectHashMap<>();
        // initialize matrix based on automaton
        TObjectIntHashMap<State> stateIds = new TObjectIntHashMap<>();

        List<State> list = new ArrayList<State>(a.getStates());
        int size = list.size();
        for (State s : list) {
            stateIds.putIfAbsent(s, stateIds.size());
            int from = stateIds.get(s);
            if (!map.containsKey(from)) {
                map.put(from, new TIntDoubleHashMap());
            }
//            int outDegree = 0;
            for (Transition t : s.getTransitions()) {
                stateIds.putIfAbsent(t.getDest(), stateIds.size());
                int target = stateIds.get(t.getDest());
                int c = t.getMax() - t.getMin() + 1;
                map.get(from).adjustOrPutValue(target, c, c);
                //              outDegree += c;
            }
        }

        // short circuit matrix:
        int start = stateIds.get(a.getInitialState());
        for (State as : a.getAcceptStates()) {
            int end = stateIds.get(as);
            if (!map.containsKey(end)) {
                map.put(end, new TIntDoubleHashMap());
            }
            map.get(end).adjustOrPutValue(start, shortCircuitFactor, shortCircuitFactor);
        }

        for (int key : map.keys()) {
            double[] values = map.get(key).values();
            double sum = 0;
            for (double d : values) {
                sum += d;
            }
            degreeStats.addValue(sum);
        }

        int[][] rowsNotEmpty = new int[size][];
        for (int i = 0; i < size; i++) {
            TIntDoubleMap column = map.get(i);
            rowsNotEmpty[i] = new int[column.size()];
            int row = 0;
            for (int r : column.keys()) {
                rowsNotEmpty[i][row++] = r;
            }
        }
        CompColMatrix cp = new CompColMatrix(size, size, rowsNotEmpty);
        for (int col = 0; col < size; col++) {
            TIntDoubleMap column = map.get(col);
            for (int row : column.keys()) {
                cp.set(row, col, map.get(col).get(row));
            }
        }
        return new MatrixAndDegreeStats(cp, degreeStats.getMean(), degreeStats.getMax());
    }

    /**
     * Makes sure to properly check, whether an automaton is deterministic.
     *
     * @param a
     * @return
     */
    private static boolean isDeterministic(Automaton a) {
        boolean isDeterministic = a.isDeterministic();
        Iterator<State> stateIterator = a.getStates().iterator();
        while (stateIterator.hasNext() && isDeterministic) {
            State state = stateIterator.next();
            Set<Character> exitLabels = new HashSet<>();
            Iterator<Transition> transIter = state.getTransitions().iterator();
            while (transIter.hasNext() && isDeterministic) {
                Transition t = transIter.next();
                if (exitLabels.contains(t.getMin())) {
                    isDeterministic = false;
                }
                exitLabels.add(t.getMin());
            }
        }
        return isDeterministic;
    }

    public static class MatrixAndDegreeStats {
        private CompColMatrix cp;
        private double avgDegree;
        private double maxDegree;

        public MatrixAndDegreeStats(CompColMatrix cp, double avgDegree, double maxDegree) {
            this.cp = cp;
            this.avgDegree = avgDegree;
            this.maxDegree = maxDegree;
        }

        public CompColMatrix getMatrix() {
            return cp;
        }

        public double getAvgDegree() {
            return avgDegree;
        }

        public double getMaxDegree() {
            return maxDegree;
        }
    }

    private static class AutomatonMinimization {
        private final ProMCanceller canceller;
        private Automaton a;
        private String name;
        private int automatonSizeOrig;
        private int automatonSizeDeterministic;
        private int automatonSizeMinimal;
        private long timeDeterminize;
        private long timeMinimize;

        public AutomatonMinimization(Automaton a, String name, ProMCanceller canceller) {
            this.a = a;
            this.name = name;
            this.canceller = canceller;
        }

        public int getAutomatonSizeOrig() {
            return automatonSizeOrig;
        }

        public int getAutomatonSizeDeterministic() {
            return automatonSizeDeterministic;
        }

        public int getAutomatonSizeMinimal() {
            return automatonSizeMinimal;
        }

        public AutomatonMinimization minimize() {
            long startTime = System.currentTimeMillis();
            logger.info("-------------------");
            logger.info(name);
            logger.info("-------------------");
            automatonSizeOrig = a.getStates().size();
            automatonSizeDeterministic = automatonSizeOrig;
            logger.info("Number of states: " + automatonSizeOrig);
            if (DEBUG_AUTOMATA_TO_DISK) {
                IOUtils.toFile(name + ".dot", a.toDot());
            }

            if (!isDeterministic(a)) {
                logger.info("non-deterministic, determinizing ...");
                a.setDeterministic(false);
                a.determinize(canceller);
                if (DEBUG_AUTOMATA_TO_DISK) {
                    IOUtils.toFile(name + "_det.dot", a.toDot());
                }
                automatonSizeDeterministic = a.getStates().size();
                logger.info("Number of states: " + automatonSizeDeterministic);
            }
            long now = System.currentTimeMillis();
            timeDeterminize = now - startTime;

            logger.info("minimizing ...");
            a.minimize(canceller);
            timeMinimize = System.currentTimeMillis() - now;


            automatonSizeMinimal = a.getStates().size();
            logger.info("Number of states: " + automatonSizeMinimal);

            if (DEBUG_AUTOMATA_TO_DISK) {
                IOUtils.toFile(name + "_min.dot", a.toDot());
            }
            return this;
        }
    }

    private static SparseRealMatrix getSparseApacheMathMatrix(Automaton a){
        return getSparseApacheMathMatrix(a, 1.0);
    }

    private static SparseRealMatrix getSparseApacheMathMatrix(Automaton a, double shortCircuidFactor) {
        SparseRealMatrix matrix = new OpenMapRealMatrix(a.getNumberOfStates(), a.getNumberOfStates());

        TObjectIntHashMap<State> stateIds = new TObjectIntHashMap<>();
        for (State state : a.getStates()) {
            int row = getId(state, stateIds);
            for (Transition t : state.getTransitions()) {
                int col = getId(t.getDest(), stateIds);
                int c = t.getMax() - t.getMin() + 1;
                double val = 0;
                double entry = matrix.getEntry(row, col);
                matrix.setEntry(row, col, entry + c);
            }
        }
        // short circuit matrix:
        for (State as : a.getAcceptStates()) {
            int row = getId(as, stateIds);
            int col = getId(a.getInitialState(), stateIds);

            double entry = matrix.getEntry(row, col);
            matrix.setEntry(row, col, entry + shortCircuidFactor);
        }
        return matrix;
    }

    private static double getMax(double[] values) {
        double max = Double.NEGATIVE_INFINITY;
        for (double d : values) {
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    /**
     * Based on the access of transitions, this method checks whether we already saw a state and specified its id,
     * or it assigns a increasing id accordingly to freshly visited states.
     *
     * @param state    {@link State} the state in the automaton
     * @param stateIds {@link TObjectIntHashMap} a store of ids
     * @return int the id
     */
    private static int getId(State state, TObjectIntHashMap<State> stateIds) {
        if (!stateIds.containsKey(state)) {
            stateIds.put(state, stateIds.size());
        }
        return stateIds.get(state);
    }
}
