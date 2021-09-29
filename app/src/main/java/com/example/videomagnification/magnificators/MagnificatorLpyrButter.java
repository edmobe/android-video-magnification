package com.example.videomagnification.magnificators;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;

public class MagnificatorLpyrButter  implements Callable<String> {

    Handler handler;
    String videoIn;
    String outDir;
    double alpha;
    double lambdaC;
    double fl;
    double fh;
    double samplingRate;
    double chromAttenuation;

    public MagnificatorLpyrButter(String videoIn, String outDir,
                                 double alpha, double lambdaC,
                                 double fl, double fh, double samplingRate,
                                 double chromAttenuation) {
        this.handler = new Handler(Looper.getMainLooper());
        this.videoIn = videoIn;
        this.outDir = outDir;
        this.alpha = alpha;
        this.lambdaC = lambdaC;
        this.fl = fl;
        this.fh = fh;
        this.samplingRate = samplingRate;
        this.chromAttenuation = chromAttenuation;
    }

    public native String amplify_spatial_lpry_temporal_butter(String videoIn, String outDir,
                                                             double alpha, double lambdaC,
                                                             double fl, double fh, double samplingRate,
                                                             double chromAttenuation);

    @Override
    public String call() {
        return amplify_spatial_lpry_temporal_butter(
                videoIn, outDir, alpha, lambdaC, fl, fh, samplingRate, chromAttenuation);
    }
}
