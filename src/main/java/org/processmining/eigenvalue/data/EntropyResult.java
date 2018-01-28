package org.processmining.eigenvalue.data;

public class EntropyResult {

    public static final String SEPARATOR = ";";

    /**
     * the size of the automaton right after transformation from the model
     */
    public int automatonSizeOrig;

    /**
     * The size of the automaton after making it deterministic
     */
    public int automatonSizeDeterministic;

    /**
     * the size of the minimal automaton
     */
    public int automatonSizeMinimal;

    /**
     * The largest eigenvalue of the eigenvalue decomposition
     */
    public double largestEigenvalue;

    /**
     * The topological entropy is just the logarithm of the largest eigenvalue
     */
    public double topologicalEntropy;

    /**
     * Stores whether the power method converged and the result is reliable
     */
    public boolean converged;
    public final long timeMatrixConversion;
    public final long timeAutomatonDeterminization;
    public final long timeAutomatonMinimization;
    public final long timeEigenDecomposition;

    public String name;
    public long computationMillis;
    public int size;

    public EntropyResult(int automatonSizeOrig, int automatonSizeDeterministic, int automatonSizeMinimal,
                         long timeAutomatonDeterminization, long timeAutomatonMinimization,
                         double largestEigenvalue, double topologicalEntropy,
                         long timeEigenDecomposition, long timeMatrixConversion, boolean converged) {
        this("",0,0,automatonSizeOrig,automatonSizeDeterministic,automatonSizeMinimal, timeAutomatonDeterminization,
                timeAutomatonMinimization,largestEigenvalue,topologicalEntropy,timeEigenDecomposition,timeMatrixConversion,
                converged);
    }

    public EntropyResult(String name, int size, long millis, int automatonSizeOrig, int automatonSizeDeterministic, int automatonSizeMinimal,
                         long timeAutomatonDeterminization, long timeAutomatonMinimization,
                         double largestEigenvalue, double topologicalEntropy,
                         long timeEigenDecomposition, long timeMatrixConversion, boolean converged) {
        this.name = name;
        this.size = size;
        this.computationMillis = millis;
        this.automatonSizeOrig = automatonSizeOrig;
        this.automatonSizeDeterministic = automatonSizeDeterministic;
        this.automatonSizeMinimal = automatonSizeMinimal;
        this.timeAutomatonDeterminization = timeAutomatonDeterminization;
        this.timeAutomatonMinimization = timeAutomatonMinimization;
        this.largestEigenvalue = largestEigenvalue;
        this.timeEigenDecomposition = timeEigenDecomposition;
        this.timeMatrixConversion = timeMatrixConversion;
        this.topologicalEntropy = topologicalEntropy;
        this.converged = converged;
    }

    public double getResult(){
        return topologicalEntropy;
    }

    public String toString(){
        return "EntropyResult: " + String.valueOf(getResult());
    }

    public static String getHeader(){
        return "name"+SEPARATOR+"timeMillis"+SEPARATOR+
                "timeDeterminization"+SEPARATOR+"timeMinimization"+SEPARATOR+
                "timeEigen"+SEPARATOR+"netSize"+SEPARATOR+
                "automatonSizeOrig"+SEPARATOR+"automatonSizeDeterministic"+SEPARATOR+
                "automatonSize"+SEPARATOR+"entropy"+SEPARATOR+
                "largestEigenValue"+SEPARATOR+"entropyComputationConverged";
    }

    public String resultString(){
        return name +SEPARATOR+ computationMillis +SEPARATOR+
                timeAutomatonDeterminization +SEPARATOR+ timeAutomatonMinimization +SEPARATOR+
                timeEigenDecomposition +SEPARATOR+ size +SEPARATOR+
                automatonSizeOrig +SEPARATOR+ automatonSizeDeterministic +SEPARATOR+
                automatonSizeMinimal +SEPARATOR+ topologicalEntropy +SEPARATOR+
                largestEigenvalue +SEPARATOR+ String.valueOf(converged).toUpperCase();
    }
}

