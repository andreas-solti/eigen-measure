package org.processmining.eigenvalue.generator.edit;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public abstract class EditOp implements Comparable<EditOp>{
    public static int ADD = 0;
    public static int SWAP = 1;
    public static int REMOVE = 2;

    protected int type;

    public EditOp(int type){
        this.type = type;
    }

    @Override
    public int compareTo(EditOp o) {
        return Integer.compare(type, o.type);
    }

    public abstract XTrace apply(XLog log, XTrace trace);
}
