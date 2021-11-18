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

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_videomagnification_processing_magnificators_MagnificatorGdownIdeal_amplify_1spatial_1gdown_1temporal_1ideal(
        JNIEnv *env, jobject thiz, jstring video_in, jstring out_dir, jdouble alpha, jint level,
        jdouble fl, jdouble fh, jdouble sampling_rate, jdouble chrom_attenuation, jint roiX, jint roiY) {

    const char *videoInCharArr = env->GetStringUTFChars(video_in, 0);
    std::string inFile = std::string(videoInCharArr);
    const char *outputDirCharArr = env->GetStringUTFChars(out_dir, 0);
    std::string outDir = std::string(outputDirCharArr);

    logDebug("Video reception - Input file", inFile);

    string output = amplify_spatial_Gdown_temporal_ideal(env, inFile, outDir, alpha, level, fl, fh,
                                                         sampling_rate, chrom_attenuation,
                                                         roiX, roiY);

    return env->NewStringUTF(output.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_videomagnification_processing_magnificators_MagnificatorLpyrButter_amplify_1spatial_1lpry_1temporal_1butter(
        JNIEnv *env, jobject thiz, jstring video_in, jstring out_dir, jdouble alpha,
        jdouble lambda_c, jdouble fl, jdouble fh, jdouble sampling_rate,
        jdouble chrom_attenuation, jint roiX, jint roiY) {

    const char *videoInCharArr = env->GetStringUTFChars(video_in, 0);
    std::string inFile = std::string(videoInCharArr);
    const char *outputDirCharArr = env->GetStringUTFChars(out_dir, 0);
    std::string outDir = std::string(outputDirCharArr);

    logDebug("Video reception - Input file", inFile);

    string output = amplify_spatial_lpyr_temporal_butter(env, inFile, outDir, alpha, lambda_c,
                                                         fl, fh, sampling_rate, chrom_attenuation,
                                                         roiX, roiY);

    return env->NewStringUTF(output.c_str());

}