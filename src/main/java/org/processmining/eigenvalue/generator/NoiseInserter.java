package org.processmining.eigenvalue.generator;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.eigenvalue.generator.edit.AddOp;
import org.processmining.eigenvalue.generator.edit.EditOp;
import org.processmining.eigenvalue.generator.edit.RemoveOp;
import org.processmining.eigenvalue.generator.edit.SwapOp;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

import java.util.*;

public class NoiseInserter {
    private final double tracesAffectedRatio;
    private final double chaosRatio;
    private final RandomGenerator random;

    public NoiseInserter(double tracesAffectedRatio, double chaosRatio){
        this.tracesAffectedRatio = tracesAffectedRatio;
        this.chaosRatio = chaosRatio;
        this.random = new MersenneTwister();
    }

    public XLog insertNoise(XLog log){
        XLog copy = StochasticNetUtils.cloneLog(log);
        Set<Integer> traceSet = new HashSet<>();
        int size = copy.size();
        for (int i = 0; i < size; i++){
            traceSet.add(i);
        }
        Set<Integer> affectedTraces = new HashSet<>();

        Iterator<Integer> iter = traceSet.iterator();
        while (affectedTraces.size() / (double)size < tracesAffectedRatio){
            Integer i = iter.next();
            iter.remove();
            affectedTraces.add(i);
        }

        for (Integer index : affectedTraces){
            XTrace traceToModify = copy.get(index);
            int edits = Math.max(1, (int)(traceToModify.size() * chaosRatio));
            SortedSet<EditOp> editSet = new TreeSet<>();
            for(int i = 0; i < edits; i++){
                int nextInt = random.nextInt(3);
                if(nextInt == 0) { // add event
                    int fromTrace = random.nextInt(size);
                    editSet.add( new AddOp(fromTrace, random.nextInt(log.get(fromTrace).size()), random.nextInt(traceToModify.size())) );
                } else if (nextInt == 1){ // remove event
                    editSet.add(new RemoveOp(random.nextInt(traceToModify.size())));
                } else if (nextInt == 2) { // swap events
                    int leftPos = random.nextInt(traceToModify.size());
                    int rightPos = random.nextInt(traceToModify.size());
                    editSet.add(new SwapOp(leftPos, rightPos));
                }
            }
            for (EditOp edit : editSet){
                traceToModify = edit.apply(log, traceToModify);
            }
        }
        return copy;
    }
}
