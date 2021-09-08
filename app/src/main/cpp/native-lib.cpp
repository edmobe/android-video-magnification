#include <jni.h>
#include <string>
#include "opencv-utils.h"
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/core.hpp>
#include <opencv2/videoio/registry.hpp>
#include <opencv2/videoio.hpp>
#include "android/bitmap.h"
#include "android/log.h"
#include <stdio.h>
#include <iostream>
#include <exception>
#include <filesystem>

void logDebug(std::string title, std::string str) {
    std::string output = title + ": " + str;
    __android_log_write(ANDROID_LOG_DEBUG, "Native lib", output.c_str());
}

void logDebugVideoBackends() {
    std::string backendsString = "";
    std::vector<VideoCaptureAPIs> backends = cv::videoio_registry::getBackends();

    for (int i = 0; i < backends.size(); ++i) {
        backendsString += cv::videoio_registry::getBackendName(backends[i]) + " ";
    }

    logDebug("Backends", backendsString);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_videomagnification_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */,
        jstring video_path) {

    const char *videoPathCharArray = env->GetStringUTFChars(video_path, 0);
    std::string videoPath = std::string(videoPathCharArray);

    logDebug("File path", videoPathCharArray);
//
//    FILE* file = fopen(videoPathCharArray, "r");
//
//    if (file != NULL)
//    {
//        logDebug("File open status", "File is VALID!");
//        fclose(file);
//    } else {
//        logDebug("File open status", "File is null");
//    }

    VideoCapture video;
    video.open(videoPathCharArray);

    // Check if video opened successfully
    if (!video.isOpened()) {
        logDebugVideoBackends();
        return env->NewStringUTF("Error opening the video!");
    }

    logDebugVideoBackends();
    logDebug("Video Capture CODEC", video.getBackendName());

    // Extract video info
    std::cout << "Codec: " << CAP_PROP_FOURCC << std::endl;
    int len = video.get(CAP_PROP_FRAME_COUNT);
    int startIndex = 0;
    int endIndex = len - 10;
    int vidHeight = video.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = video.get(CAP_PROP_FRAME_WIDTH);
    int fr = video.get(CAP_PROP_FPS);

    std::string hello = "Hello from C++. The video is " + std::to_string(len) +
            " long. It is also " + std::to_string(vidWidth) + "x" + std::to_string(vidHeight) +
            " and it is at " + std::to_string(fr) + " fps!";

    return env->NewStringUTF(hello.c_str());
}



















void bitmapToMat(JNIEnv *env, jobject bitmap, Mat& dst, jboolean needUnPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv* env, Mat src, jobject bitmap, jboolean needPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width == (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_videomagnification_MainActivity_flip(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    // NOTE bitmapToMat returns Mat in RGBA format, if needed convert to BGRA using cvtColor

    myFlip(src);

    // NOTE matToBitmap expects Mat in GRAY/RGB(A) format, if needed convert using cvtColor
    matToBitmap(env, src, bitmapOut, false);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_videomagnification_MainActivity_blur(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut,
        jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    // NOTE bitmapToMat returns Mat in RGBA format, if needed convert to BGRA using cvtColor

    myBlur(src, sigma);

    // NOTE matToBitmap expects Mat in GRAY/RGB(A) format, if needed convert using cvtColor
    matToBitmap(env, src, bitmapOut, false);
}