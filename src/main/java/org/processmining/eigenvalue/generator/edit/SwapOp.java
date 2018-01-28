package org.processmining.eigenvalue.generator.edit;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class SwapOp extends EditOp {

    private int rightPos;

    private int leftPos;

    public SwapOp(int left, int right) {
        super(EditOp.SWAP);
        this.leftPos = left;
        this.rightPos = right;
    }

    @Override
    public XTrace apply(XLog log, XTrace trace) {
        if (rightPos == leftPos){
            rightPos = (leftPos+2) % trace.size();
        }
        if (leftPos > rightPos){
            int temp = leftPos;
            leftPos = rightPos;
            rightPos = temp;
        }
        XEvent left = trace.get(leftPos);
        XEvent right = trace.get(rightPos);
        trace.remove(leftPos);
        trace.add(leftPos, right);
        trace.remove(rightPos);
        trace.add(rightPos, left);
        return trace;
    }
}
