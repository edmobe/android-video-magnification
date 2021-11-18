package com.example.videomagnification.processing.magnificators;

public class MagnificatorLpyrButter extends Magnificator {

    private double lambdaC;

    public MagnificatorLpyrButter(String videoIn, String outDir,
                                 double alpha, double lambdaC,
                                 double fl, double fh, double samplingRate,
                                 double chromAttenuation, int roiX, int roiY) {
        this.videoIn = videoIn;
        this.outDir = outDir;
        this.alpha = alpha;
        this.lambdaC = lambdaC;
        this.fl = fl;
        this.fh = fh;
        this.samplingRate = samplingRate;
        this.chromAttenuation = chromAttenuation;
        this.roiX = roiX;
        this.roiY = roiY;
    }

    public native String amplify_spatial_lpry_temporal_butter(String videoIn, String outDir,
                                                             double alpha, double lambdaC,
                                                             double fl, double fh, double samplingRate,
                                                             double chromAttenuation, int roiX, int roiY);

    @Override
    public String magnify() {
        return amplify_spatial_lpry_temporal_butter(
                videoIn, outDir, alpha, lambdaC, fl, fh, samplingRate, chromAttenuation, roiX, roiY);
    }
}
