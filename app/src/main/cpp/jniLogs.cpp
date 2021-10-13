#include <jni.h>
#include <android/looper.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/videoio/registry.hpp>
#include <unistd.h>
#include "android/log.h"
#include "jniLogs.h"

static ALooper* mainThreadLooper;
static int messagePipe[2];

struct JniData {
    JNIEnv *env;
    jmethodID setText;
    jobject magnifierLog;
    size_t messageSize;
};

void logDebug(std::string title, std::string str) {
    std::string output = title + ": " + str;
    __android_log_write(ANDROID_LOG_DEBUG, "Video Magnification (native lib)", output.c_str());
}

void logDebugAndShowUser(JNIEnv *env, std::string title, std::string str) {
    logDebug(title, str);

    std::string output = title + ": " + str;
    jstring jstring1 = env->NewStringUTF(output.c_str());

    jclass clazz = env->FindClass("com/example/videomagnification/gui/processing/NativeLibManagerActivity");
    jmethodID methodId = env->GetStaticMethodID(clazz, "updateMagnifierLog",
                                          "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(clazz, methodId, jstring1);
}

void updateProgress(JNIEnv *env, int progress) {
    jclass clazz = env->FindClass("com/example/videomagnification/gui/processing/NativeLibManagerActivity");
    jmethodID methodId = env->GetStaticMethodID(clazz, "updateProgress",
                                                "(I)V");
    env->CallStaticVoidMethod(clazz, methodId, progress);
}

void logDebugVideoBackends() {
    std::string backendsString = "";
    std::vector<cv::VideoCaptureAPIs> backends = cv::videoio_registry::getBackends();

    for (int i = 0; i < backends.size(); ++i) {
        backendsString += cv::videoio_registry::getBackendName(backends[i]) + " ";
    }

    logDebug("Backends", backendsString);
}

