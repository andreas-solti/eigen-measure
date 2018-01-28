package org.processmining.eigenvalue.generator;

public class GenerateLogParameters {
    private long seed;
    private int numberOfTraces;
    private int noisyTraces;
    private int noiseEventsPerTrace;
    private long noiseSeed = 6778706438l;

    public GenerateLogParameters() {
        this(2000,1001);
    }

    public GenerateLogParameters(int numberOfTraces, long seed) {
        this(numberOfTraces, seed, 0, 0);
    }

    public GenerateLogParameters(int numberOfTraces, long seed, int noisyTraces, int noiseEventsPerTrace) {
        this.seed = seed;
        this.numberOfTraces = numberOfTraces;
        this.noisyTraces = noisyTraces;
        this.noiseEventsPerTrace = noiseEventsPerTrace;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getNumberOfTraces() {
        return numberOfTraces;
    }

    public void setNumberOfTraces(int numberOfTraces) {
        this.numberOfTraces = numberOfTraces;
    }

    public int getNoisyTraces() {
        return noisyTraces;
    }

    public int getNoiseEventsPerTrace() {
        return noiseEventsPerTrace;
    }

    public long getNoiseSeed() {
        return noiseSeed;
    }

    public void setNumberOfNoisyTraces(int noisyTraces) {
        this.noisyTraces = noisyTraces;
    }

    public void setNoiseEventsPerTrace(int noiseEventsPerTrace) {
        this.noiseEventsPerTrace = noiseEventsPerTrace;
    }

}