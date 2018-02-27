package org.processmining.eigenvalue.automata;

import dk.brics.automaton2.Automaton;
import dk.brics.automaton2.RunAutomaton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.framework.packages.impl.CancelledException;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.impl.EfficientStochasticNetSemanticsImpl;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.DefTransClassifier;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.projectedrecallandprecision.helperclasses.AcceptingPetriNet2automaton;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;
import org.processmining.projectedrecallandprecision.helperclasses.ProjectPetriNetOntoActivities;

import java.util.HashSet;
import java.util.Set;

public class PrecisionRecallComputer {

    private static final Log logger = LogFactory.getLog(PrecisionRecallComputer.class);

    /**
     * Computes Precision and Recall for event log and an accepting petri net. Uses the default Name Classifier to
     * establish a link between model and log.
     *
     * @param context {@link PluginContext} that can be null in testing or UI-less computation
     * @param canceller {@link ProMCanceller} a handler that indicates, whether computation should be aborted due to user cancellation
     * @param log {@link XLog} the event log to check for precision & fitness
     * @param net {@link AcceptingPetriNet} that has corresponding initial and final markings set
     * @return
     */
    public static EntropyPrecisionRecall getPrecisionAndRecall(PluginContext context, ProMCanceller canceller, XLog log, AcceptingPetriNet net) {
        try {
            return getPrecisionAndRecall(context, canceller, log, net, XLogInfoImpl.NAME_CLASSIFIER, null);
        } catch (CancelledException e) {
            logger.info("Precision computation cancelled!");
            return null;
        }
    }

    public static EntropyPrecisionRecall getPrecisionAndRecall(Automaton aM, String mName, Automaton aL, String lName, Automaton aLM, double fittingTracesFraction, ProMCanceller canceller){
        return getPrecisionAndRecall(aM, mName, aL, lName, aLM, mName+"&"+lName, fittingTracesFraction, canceller);
    }

    public static EntropyPrecisionRecall getPrecisionAndRecall(Automaton aM, String mName, Automaton aL, String lName, Automaton aLM, String lmName, double fittingTracesFraction, ProMCanceller canceller){
        EntropyResult resultM = getResult(mName, aM.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(aM, mName, canceller));
        EntropyResult resultL = getResult(lName, aL.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(aL, lName, canceller));
        EntropyResult resultLM = getResult(lmName, aLM.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(aLM, lmName, canceller));

        return new EntropyPrecisionRecall(resultLM, resultM, resultL, fittingTracesFraction);
    }

    /**
     * Computes Precision and Recall for event log and an accepting petri net.
     * @param context {@link PluginContext} that can be null in testing or UI-less computation
     * @param canceller {@link ProMCanceller} a handler that indicates, whether computation should be aborted due to user cancellation
     * @param log {@link XLog} the event log to check for precision & fitness
     * @param net {@link AcceptingPetriNet} that has corresponding initial and final markings set
     * @param classifier the classifier to be used (for example the name classifier XLogInfoImpl.NAME_CLASSIFIER)
     * @param resultL {@link EntropyResult} of a log (it is possible to pass in the result, to avoid recomputing it all the time.)
     *
     * @return EntropyPrecisionRecall result object encapsulating
     * @throws CancelledException
     */
    public static EntropyPrecisionRecall getPrecisionAndRecall(PluginContext context, ProMCanceller canceller, XLog log, AcceptingPetriNet net, XEventClassifier classifier, EntropyResult resultL) throws CancelledException {
        String name = Utils.getName(net.getNet(),"M");
        String logName = Utils.getName(log,"L");

        if (context != null && context.getProgress() != null) {
            context.getProgress().setValue(0);
            context.getProgress().setMaximum(100);
        }
        log(context, "Starting precision computation for "+name, 1);
        checkCancelled(canceller);

        // prepare
        EfficientLog elog = new EfficientLog(log, classifier);
        String[] activities = elog.getActivities();
        log(context, "Converted log to efficient log.", 28);
        if (canceller.isCancelled()){
            return null;
        }
        String[] names = getTransitionNames(net, activities);

        AcceptingPetriNet projectedNet = ProjectPetriNetOntoActivities.project(net, canceller, names);
        Automaton a = null;
        try {
            a = AcceptingPetriNet2automaton.convert(projectedNet, Integer.MAX_VALUE, canceller);
        } catch (AutomatonFailedException e){
            e.printStackTrace();
        }
        log(context, "Converted net to automaton.", 35);
        checkCancelled(canceller);


        log(context, "Projected net to (all) activities.", 30);
        EntropyPrecisionRecall precision = null;
        try {

            EntropyResult resultM = getResult(name, net.getNet().getNodes().size(), TopologicalEntropyComputer.getTopologicalEntropy(a, name, canceller));

            log(context, "Computed model automaton topology.", 60);
            checkCancelled(canceller);

            RunAutomaton ra = new RunAutomaton(a, canceller);
            double fittingTracesFraction;
            EntropyResult resultLM;
            {
                Pair<Double, Automaton> pair = processLog(elog, ra, true, canceller, names);
                fittingTracesFraction = pair.getA();
                log(context, "Projected log into model.", 70);
                checkCancelled(canceller);

                a = pair.getB(); // log automaton of accepted traces
                if (a == null) {
                    log(context, "No fitting traces in log " + logName +
                            " that match model " + name + ". Precision is 0 be definition!", 79);
                }
                resultLM = getResult(logName, elog.size(), TopologicalEntropyComputer.getTopologicalEntropy(a, logName, canceller));

                log(context, "Computed log-model automaton topology.", 75);
            }
            if (fittingTracesFraction >= 1){ // optimization in case all the log fits, we assume its automaton is equal to the intersection automaton
                resultL = resultLM;
            }
            if (resultL == null) {
                resultL = getEntropyLogResult(context, canceller, logName, elog, names);
                checkCancelled(canceller);
            }
            precision = new EntropyPrecisionRecall(resultLM, resultM, resultL, fittingTracesFraction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return precision;
    }

    private static void checkCancelled(ProMCanceller canceller) throws CancelledException {
        if (canceller.isCancelled()){
            throw new CancelledException();
        }
    }
    public static EntropyResult getEntropyLogResult(PluginContext context, ProMCanceller canceller, String logName, EfficientLog elog, String[] names) throws CancelledException {
        Pair<Double, Automaton> pair = processLog(elog, null, false, canceller, names);
        log(context, "Computed Log automaton.", 80);
        checkCancelled(canceller);

        Automaton a = pair.getB();
        EntropyResult resultL = getResult(logName, elog.size(), TopologicalEntropyComputer.getTopologicalEntropy(a, logName, canceller));
        log(context, "Computed log automaton topological entropy.", 95);
        return resultL;
    }

    public static String[] getTransitionNames(AcceptingPetriNet net, String[] activities) {
        Set<String> transitionNames = new HashSet<>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition t : net.getNet().getTransitions()) {
            if (!t.isInvisible() && t.getLabel() != null) {
                transitionNames.add(t.getLabel());
            }
        }
        for (int i = 0; i<activities.length; i++) transitionNames.add(activities[i]);
        return transitionNames.toArray(new String[transitionNames.size()]);
    }

    private static void log(PluginContext context, String message, int degreeDone) {
        if (context != null && context.getProgress() != null) {
            context.getProgress().setValue(25);
            context.log(message);
        }
        logger.debug(message);
    }

    public static EntropyResult getResult(String name, int size, EntropyResult entResult) {
        entResult.name = name;
        entResult.size = size;
        return entResult;
    }

    /**
     * Use automaton acceptance of each trace, as we are only interested in truly in the model (like in soccer).
     * To check language containment with an automaton is much faster than the alignment,
     * if we only are interested in completely fitting traces
     *
     * @param context
     * @param canceller
     * @param log
     * @param net
     * @param initMarking
     * @param eventClassifier
     * @param fittingLog
     */
    @Deprecated
    private static void filterForFittingTracesByAlignment(PluginContext context, ProMCanceller canceller, XLog log, AcceptingPetriNet net, Marking initMarking, XEventClassifier eventClassifier, XLog fittingLog) {
        TransEvClassMapping transitionEventClassMap = StochasticNetUtils.getEvClassMapping(net.getNet(), log, eventClassifier);
        TransClasses transClasses = new TransClasses(net.getNet(), new DefTransClassifier());
        PNManifestReplayerParameter parameters = StochasticNetUtils.getParameters(log, transitionEventClassMap, net.getNet(),
                net.getInitialMarking(), net.getFinalMarkings().iterator().next(),
                eventClassifier, transClasses);
        Manifest manifest = (Manifest) StochasticNetUtils.replayLog(context, net.getNet(), log, parameters, true);
        if (canceller.isCancelled()){
            return;
        }
        org.processmining.models.graphbased.directed.petrinet.elements.Transition[] idx2Trans = net.getNet().getTransitions().toArray(new org.processmining.models.graphbased.directed.petrinet.elements.Transition[net.getNet().getTransitions().size()]);

        for (int i = 0; i < manifest.getCasePointers().length; i++){
            int caseId = manifest.getCasePointers()[i];
            boolean fitting = true;
            if (caseId >= 0) {
                int[] man = manifest.getManifestForCase(i);
                int currIdx = 0;
                while (currIdx < man.length) {
                    if (man[currIdx] == Manifest.MOVELOG) {
                        fitting = false;
                        currIdx++;
                    } else if (man[currIdx] == Manifest.MOVEMODEL) {
                        if (!idx2Trans[man[currIdx + 1]].isInvisible()) {
                            fitting = false;
                        }
                        currIdx += 2;
                    } else if (man[currIdx] == Manifest.MOVESYNC) {
                        currIdx += 2;
                    }
                }
                if (fitting){
                    fittingLog.add(log.get(i));
                }
            }
        }
    }

    /**
     * Extracts the intersection automaton of the log and the model.
     * Does this by incrementally building an automaton of the log for each trace variant that fits the model.
     *
     * @param log {@link EfficientLog}
     * @param modelAutomaton {@link RunAutomaton} for parsing the traces
     * @param selectOnlyFittingTraces
     * @param canceller
     * @param names
     * @return
     */
    public static Pair<Double, Automaton> processLog(EfficientLog log, RunAutomaton modelAutomaton, boolean selectOnlyFittingTraces, ProMCanceller canceller, String... names) {
        short[] projectionKey = log.getProjectionKey(names);
        int replayableTraces = 0;
        Automaton logAutomaton = null;

        for(int traceIndex = 0; traceIndex < log.size(); ++traceIndex) {
            Pair<Boolean, Automaton> p = processTrace(modelAutomaton, logAutomaton, log, traceIndex, projectionKey, selectOnlyFittingTraces, canceller);
            if (canceller.isCancelled() || p == null) {
                return null;
            }

            logAutomaton = p.getB();
            if (p.getA()) {
                ++replayableTraces;
            }
        }
        if (logAutomaton != null) {
            logAutomaton.minimize(canceller);
        }
        if (canceller.isCancelled()) {
            return null;
        } else {
            return Pair.of(replayableTraces/(double)log.size(), logAutomaton);
        }
    }

    /**
     * Incorporates fitting traces into the log automaton.
     * @param modelAutomaton {@link RunAutomaton} that encodes the model for parsing each trace
     * @param logAutomaton the current state of the log automaton that is invrementally enriched.
     * @param log {@link EfficientLog} encoded log
     * @param trace int the trace index
     * @param projectionKey short[] encoded short values that refer to the activities in the log
     * @param onlyFittingTraces flag that specifies whether only fitting traces should be added to the log automaton
     * @param canceller {@link ProMCanceller} that allows to stop the computation on user request.
     * @return Pair of boolean, Automaton:  flag that indicates whether the trace was incorporated, and the resulting automaton.
     */
    public static Pair<Boolean, Automaton> processTrace(RunAutomaton modelAutomaton, Automaton logAutomaton, EfficientLog log, int trace, short[] projectionKey, boolean onlyFittingTraces, ProMCanceller canceller) {
        short[] projectedTrace = log.getProjectedTrace(trace, projectionKey);
        boolean addToAutomaton = !onlyFittingTraces || modelAutomaton.run(projectedTrace);
        if (addToAutomaton) { // don't add trace to log automaton, if it can't be replayed.
            if (logAutomaton == null) {
                String automatonString = projectedTraceToString(projectedTrace);
                logAutomaton = Automaton.makeString(automatonString);
                logAutomaton.expandSingleton();
            } else {
                logAutomaton.incorporateTrace(projectedTrace, canceller);
                if (canceller.isCancelled()) {
                    return null;
                }
            }
        }

        return Pair.of(addToAutomaton, logAutomaton);
    }

    public static String projectedTraceToString(short[] projectedTrace) {
        String result = "";

        for(int event = 0; event < projectedTrace.length; ++event) {
            if (projectedTrace[event] >= 0) {
                result = result + (char)projectedTrace[event];
            }
        }

        return result;
    }
}
