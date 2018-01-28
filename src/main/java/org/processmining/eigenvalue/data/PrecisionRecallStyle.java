package org.processmining.eigenvalue.data;

import org.apache.commons.math3.util.FastMath;

public enum PrecisionRecallStyle {
    EIGENVALUE, ENTROPY;

    /**
     *
     * @param input the input measure to transform. We assume that
     * @return
     */
    public double applyTransformation(double input){
        if (input < 0){
            throw new IllegalArgumentException("Complexity measure cannot be smaller than 0! (argument: " + input+")");
        }
        switch (this){
            case ENTROPY:
                return FastMath.log(2,input);
            case EIGENVALUE:
            default:
                return input;
        }
    }
}
