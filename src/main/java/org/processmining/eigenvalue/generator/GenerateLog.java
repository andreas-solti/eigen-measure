package org.processmining.eigenvalue.generator;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.impl.AbstractTask.Manual;

import java.util.*;

public class GenerateLog {

    public static final String ORIGINAL_LABEL_ATTRIBUTE = "OrgLabel";

    public Iterable<XTrace> generateTraces(final EfficientTree tree, final GenerateLogParameters parameters)
            throws Exception {
        final Random random = new Random(parameters.getSeed());
        return new Iterable<XTrace>() {
            public Iterator<XTrace> iterator() {
                return new Iterator<XTrace>() {
                    int count = 0;
                    public XTrace next() {
                        count++;
                        try {
                            int[] trace = generateTrace(tree, tree.getRoot(), random);

                            XAttributeMap traceMap = new XAttributeMapImpl();
                            XTrace result = new XTraceImpl(traceMap);
                            ArrayList<String> traceName = new ArrayList<>();
                            for (int activity : trace) {
                                XAttributeMap attMap = new XAttributeMapImpl();
                                putLiteral(attMap, "concept:name", tree.getInt2activity()[activity]);
                                putLiteral(attMap, "lifecycle:transition", "complete");
                                putLiteral(attMap, "org:resource", "artificial");
                                result.add(new XEventImpl(attMap));
                                traceName.add(tree.getInt2activity()[activity]);
                            }
                            putLiteral(traceMap, "concept:name", traceName.toString());
                            return result;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove not implemented!");
                    }

                    public boolean hasNext() {
                        return count < parameters.getNumberOfTraces();
                    }
                };
            }
        };
    }

    private static int[] generateTrace(EfficientTree tree, int node, Random random) throws Exception {
        if (tree.isTau(node)) {
            return generateTraceTau();
        } else if (tree.isActivity(node)) {
            return generateTraceActivity(tree, node);
        } else if (tree.isXor(node)) {
            return generateTraceXor(tree, node, random);
        } else if (tree.isSequence(node)) {
            return generateTraceSeq(tree, node, random);
        } else if (tree.isLoop(node)) {
            return generateTraceXorLoop(tree, node, random);
        } else if (tree.isConcurrent(node)) {
            return generateTraceAnd(tree, node, random);
        } else if (tree.isOr(node)) {
            return generateTraceOr(tree, node, random);
        } else if (tree.isInterleaved(node)) {
            return generateTraceInterleaved(tree, node, random);
        }
        throw new Exception("not implemented");
    }

    private static int[] generateTraceTau() {
        return new int[0];
    }

    private static int[] generateTraceActivity(EfficientTree tree, int node) {
        int[] result = new int[1];
        result[0] = tree.getActivity(node);
        return result;
    }

    private static int[] generateTraceXor(EfficientTree tree, int node, Random random) throws Exception {
        int child = random.nextInt(tree.getNumberOfChildren(node));
        return generateTrace(tree, tree.getChild(node, child), random);
    }

    private static int[] generateTraceSeq(EfficientTree tree, int node, Random random) throws Exception {
        return sequentialTraces(tree, tree.getChildren(node), random);
    }

    private static int[] sequentialTraces(EfficientTree tree, Iterable<Integer> children, Random random)
            throws Exception {
        TIntList result = new TIntArrayList();
        for (int child : children) {
            result.addAll(generateTrace(tree, child, random));
        }
        return result.toArray();
    }

    private static int[] generateTraceXorLoop(EfficientTree tree, int node, Random random) throws Exception {
        int leftChild = tree.getChild(node, 0);
        int middleChild = tree.getChild(node, 1);
        int rightChild = tree.getChild(node, 2);

        TIntList result = new TIntArrayList();
        result.addAll(generateTrace(tree, leftChild, random));
        while (random.nextInt(10) > 4) {
            result.addAll(generateTrace(tree, middleChild, random));
            result.addAll(generateTrace(tree, leftChild, random));
        }
        result.addAll(generateTrace(tree, rightChild, random));
        return result.toArray();
    }

    private static int[] generateTraceAnd(EfficientTree tree, int node, Random random) throws Exception {
        int countChildren = tree.getNumberOfChildren(node);
        return concurrentChildren(countChildren, tree, tree.getChildren(node), random);
    }

    private static int[] concurrentChildren(int countChildren, EfficientTree tree, Iterable<Integer> children,
                                            Random random) throws Exception {
        //create a list of branches
        TIntList branches = new TIntArrayList();
        int[][] subtraces = new int[countChildren][];
        {
            int branch = 0;
            for (int child : children) {
                subtraces[branch] = generateTrace(tree, child, random);
                for (int i = 0; i < subtraces[branch].length; i++) {
                    branches.add(branch);
                }
                branch++;
            }
        }

        //shuffle the result
        int[] branchesA = branches.toArray();
        shuffle(branchesA, random);

        //transform the trace back form branches to events
        int[] result = new int[branchesA.length];
        int branch = 0;
        for (int[] subtrace : subtraces) {
            int subtracePos = 0;
            for (int resultPos = 0; resultPos < result.length; resultPos++) {
                if (branchesA[resultPos] == branch) {
                    result[resultPos] = subtrace[subtracePos];
                    subtracePos++;
                }
            }
            branch++;
        }

        return result;
    }

    private static int[] generateTraceInterleaved(EfficientTree tree, int node, Random random) throws Exception {
        //create a list of children
        List<Integer> children = new ArrayList<>();
        for (int child : tree.getChildren(node)) {
            children.add(child);
        }
        Collections.shuffle(children);
        return sequentialTraces(tree, children, random);
    }

    private static int[] generateTraceOr(EfficientTree tree, int node, Random random) throws Exception {
        //select children
        Set<Integer> children = new HashSet<>();
        //add one child for sure
        children.add(tree.getChild(node, random.nextInt(tree.getNumberOfChildren(node))));
        //randomly add other children
        for (int childNr = 0; childNr < tree.getNumberOfChildren(node); childNr++) {
            if (random.nextBoolean()) {
                children.add(tree.getChild(node, childNr));
            }
        }
        return concurrentChildren(children.size(), tree, children, random);
    }

    public static void putLiteral(XAttributeMap attMap, String key, String value) {
        attMap.put(key, new XAttributeLiteralImpl(key, value));
    }

    /**
     * Get a list of all manual leaves of a tree.
     *
     * @param node
     * @return
     */
    public static List<String> getActivities(Node node) {
        List<String> result = new ArrayList<>();
        if (node instanceof Manual) {
            result.add(((Manual) node).getName());
        } else if (node instanceof Block) {
            for (Node child : ((Block) node).getChildren()) {
                result.addAll(getActivities(child));
            }
        }
        return result;
    }

    public static void shuffle(int[] array, Random random) {
        int index;
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }
}