package com.example.videomagnification.magnificators;

public abstract class Magnificator {
    protected String videoIn;
    protected String outDir;
    protected double alpha;
    protected double samplingRate;
    protected double chromAttenuation;
    protected int roiX;
    protected int roiY;

    public abstract String magnify();
}
