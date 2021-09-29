package com.example.videomagnification;

import androidx.lifecycle.ViewModel;

public class VideoMagnificator extends ViewModel {


    public native String amplify_spatial_lpyr_temporal_ideal(String videoIn, String outDir,
                                                             double alpha, double lambdaC,
                                                             double fl, double fh, double samplingRate,
                                                             double chromAttenuation);

    public native String amplify_spatial_gdown_temporal_ideal(String videoIn, String outDir,
                                                              double alpha, int level,
                                                              double fl, double fh, double samplingRate,
                                                              double chromAttenuation);

    public native String amplify_spatial_lpyr_temporal_butter(String videoIn, String outDir,
                                                              double alpha, double lambdaC,
                                                              double fl, double fh, double samplingRate,
                                                              double chromAttenuation);
}


