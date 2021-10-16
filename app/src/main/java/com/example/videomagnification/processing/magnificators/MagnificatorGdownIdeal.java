package com.example.videomagnification.processing.magnificators;

public class MagnificatorGdownIdeal extends Magnificator {

    private int level;

    public MagnificatorGdownIdeal(String videoIn, String outDir,
                                  double alpha, int level,
                                  double fl, double fh, double samplingRate,
                                  double chromAttenuation, int roiX, int roiY) {
        this.videoIn = videoIn;
        this.outDir = outDir;
        this.alpha = alpha;
        this.level = level;
        this.fl = fl;
        this.fh = fh;
        this.samplingRate = samplingRate;
        this.chromAttenuation = chromAttenuation;
        this.roiX = roiX;
        this.roiY = roiY;
    }

    public native String amplify_spatial_gdown_temporal_ideal(String videoIn, String outDir,
                                                              double alpha, int level,
                                                              double fl, double fh,
                                                              double samplingRate,
                                                              double chromAttenuation,
                                                              int roiX, int roiY);

    @Override
    public String magnify() {
        return amplify_spatial_gdown_temporal_ideal(
                videoIn, outDir, alpha, level, fl, fh, samplingRate, chromAttenuation, roiX, roiY);
    }
}


