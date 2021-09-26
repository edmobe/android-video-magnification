#pragma once

#include <iostream>
#include <opencv2/opencv.hpp>
#include <jni.h>

using namespace std;
using namespace cv;

/*
Video processing functions
*/

int amplify_spatial_Gdown_temporal_ideal(string inFile, string outDir, double alpha, int level,
                                         double f1, double fh, int samplingRate,
                                         double chromAttenuation);

int amplify_spatial_lpyr_temporal_butter(string inFile, string outDir, double alpha,
                                         double lambda_c, double fl, double fh, int samplingRate,
                                         double chromAttenuation);

int amplify_spatial_lpyr_temporal_ideal(JNIEnv *env, string inFile, string outDir, double alpha,
                                        double lambda_c, double fl, double fh, double samplingRate,
                                        double chromAttenuation);

int amplify_spatial_lpyr_temporal_iir(string inFile, string outDir, double alpha,
                                      double lambda_c, double r1, double r2,
                                      double chromAttenuation);

/*
Spatial filter functions
*/

vector<Mat> build_GDown_stack(string vidFile, int startIndex, int endIndex, int level);


/*
Temporal filter functions
*/

vector<Mat> ideal_bandpassing(vector<Mat> input, int dim, double wl, double wh, int samplingRate);

