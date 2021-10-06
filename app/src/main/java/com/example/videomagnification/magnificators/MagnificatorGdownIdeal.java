package com.example.videomagnification.magnificators;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;

public class MagnificatorGdownIdeal implements Callable<String>{

    Handler handler;
    String videoIn;
    String outDir;
    double alpha;
    int level;
    double fl;
    double fh;
    double samplingRate;
    double chromAttenuation;
    int roiX;
    int roiY;

    public MagnificatorGdownIdeal(String videoIn, String outDir,
                                  double alpha, int level,
                                  double fl, double fh, double samplingRate,
                                  double chromAttenuation, int roiX, int roiY) {
        this.handler = new Handler(Looper.getMainLooper());
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
    public String call() {
        return amplify_spatial_gdown_temporal_ideal(
                videoIn, outDir, alpha, level, fl, fh, samplingRate, chromAttenuation, roiX, roiY);
    }
}


