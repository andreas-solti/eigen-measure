package org.processmining.eigenvalue.generator.edit;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class RemoveOp extends EditOp {
    private final int tracePos;

    public RemoveOp(int tracePos) {
        super(EditOp.REMOVE);
        this.tracePos = tracePos;
    }

    @Override
    public XTrace apply(XLog log, XTrace trace) {
        trace.remove(tracePos);
        return trace;
    }
}
