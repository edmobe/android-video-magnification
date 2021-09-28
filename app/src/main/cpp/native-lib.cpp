#include <string>
#include <omp.h>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/videoio.hpp>
#include "android/bitmap.h"

#include <stdio.h>
#include <iostream>
#include <fstream>

#include "jniLogs.h"
#include "processing_functions.h"

/**
* Spatial Filtering : Laplacian pyramid
* Temporal Filtering : Ideal bandpass
*
* Copyright(c) 2021 Tecnologico de Costa Rica.
*
* Authors: Eduardo Moya Bello, Ki - Sung Lim
* Date : June 2021
*
* This work was based on a project EVM
*
* Original copyright(c) 2011 - 2012 Massachusetts Institute of Technology,
* Quanta Research Cambridge, Inc.
*
* Original authors : Hao - yu Wu, Michael Rubinstein, Eugene Shih,
* License : Please refer to the LICENCE file (MIT license)
* Original date : June 2012
*/
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_videomagnification_MainActivity_amplifySpatialLpyrTemporalIdeal(
        JNIEnv *env, jobject thiz,
        jstring video_in, jstring out_dir, jdouble alpha,
        jdouble lambda_c, jdouble fl, jdouble fh, jdouble sampling_rate,
        jdouble chrom_attenuation) {

    const char *videoInCharArr = env->GetStringUTFChars(video_in, 0);
    std::string inFile = std::string(videoInCharArr);
    const char *outputDirCharArr = env->GetStringUTFChars(out_dir, 0);
    std::string outDir = std::string(outputDirCharArr);

    logDebug("Video reception - Input file", inFile);

    int status = amplify_spatial_lpyr_temporal_ideal(env, inFile, outDir, alpha, lambda_c, fl, fh,
                                                     sampling_rate, chrom_attenuation);

    return status;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_videomagnification_MainActivity_amplifySpatialGdownTemporalIdeal(
        JNIEnv *env, jobject thiz,
        jstring video_in, jstring out_dir, jdouble alpha,
        jint level, jdouble fl, jdouble fh, jdouble sampling_rate,
        jdouble chrom_attenuation) {
    const char *videoInCharArr = env->GetStringUTFChars(video_in, 0);
    std::string inFile = std::string(videoInCharArr);
    const char *outputDirCharArr = env->GetStringUTFChars(out_dir, 0);
    std::string outDir = std::string(outputDirCharArr);

    logDebug("Video reception - Input file", inFile);

    int status = amplify_spatial_Gdown_temporal_ideal(env, inFile, outDir, alpha, level, fl, fh,
                                                     sampling_rate, chrom_attenuation);

    return status;
}