#pragma once

#include <iostream>
#include <opencv2/opencv.hpp>
#include <jni.h>

using namespace std;
using namespace cv;

/*
Video processing functions
*/

string amplify_spatial_Gdown_temporal_ideal(JNIEnv *env, string inFile, string outDir, double alpha,
                                         int level, double f1, double fh, int samplingRate,
                                         double chromAttenuation, int roiX, int roiY);

string amplify_spatial_lpyr_temporal_ideal(JNIEnv *env, string inFile, string outDir, double alpha,
                                        double lambda_c, double fl, double fh, double samplingRate,
                                        double chromAttenuation);

string amplify_spatial_lpyr_temporal_butter(JNIEnv *env, string inFile, string outDir, double alpha,
                                         double lambda_c, double fl, double fh, int samplingRate,
                                         double chromAttenuation, int roiX, int roiY);

