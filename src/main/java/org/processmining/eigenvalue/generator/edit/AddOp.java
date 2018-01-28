package org.processmining.eigenvalue.generator.edit;

import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class AddOp extends EditOp {

    private final int sourceTrace;
    private final int sourcePos;
    private final int tracePos;

    public AddOp(int sourceTrace, int sourcePos, int tracePos) {
        super(EditOp.ADD);
        this.sourceTrace = sourceTrace;
        this.sourcePos = sourcePos;
        this.tracePos = tracePos;
    }

    @Override
    public XTrace apply(XLog log, XTrace trace) {
        XEvent someEvent = log.get(sourceTrace).get(sourcePos);
        XEvent eventClone = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap)someEvent.getAttributes().clone());
        trace.add(eventClone);
        return trace;
    }
}
