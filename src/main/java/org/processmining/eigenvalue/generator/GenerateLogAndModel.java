package org.processmining.eigenvalue.generator;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.plugins.ProcessTree2EfficientTreePlugin;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.processtree.ProcessTree;

/**
 * Created by andreas on 4/25/17.
 */
public class GenerateLogAndModel {

    public static Pair<XLog, NAryTree> generate(int traces, int activities){
        NAryTreeGenerator generator = new NAryTreeGenerator();
        NAryTree tree = generator.generate(20);

        XLog newLog = generateLog(tree,10000);
        return new MutablePair<>(newLog, tree);
    }

    public static XLog generateLog(NAryTree tree, int numberOfTraces) {
        return generateLog(tree, numberOfTraces, null);
    }


    public static XLog generateLog(NAryTree tree, int numberOfTraces, XEventClasses eventClasses) {
        ProcessTree2EfficientTreePlugin converter = new ProcessTree2EfficientTreePlugin();

        ProcessTree processTree = NAryTreeToProcessTree.convert(tree, eventClasses);
        EfficientTree efficientTree = converter.convert(null, processTree);

        XLog newLog = XFactoryRegistry.instance().currentDefault().createLog();

        GenerateLog simulator = new GenerateLog();

        GenerateLogParameters parameters = new GenerateLogParameters(numberOfTraces,1);
        try {
            for (XTrace trace : simulator.generateTraces(efficientTree, parameters)){
                newLog.add(trace);
            }
        } catch (Exception e) {
            throw new RuntimeException("Trace generation failed",e);
        }
        return newLog;
    }
}
