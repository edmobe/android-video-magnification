#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/videoio/registry.hpp>
#include "android/log.h"

void logDebug(std::string title, std::string str) {
    std::string output = title + ": " + str;
    __android_log_write(ANDROID_LOG_DEBUG, "Video Magnification (native lib)", output.c_str());
}

void logDebugVideoBackends() {
    std::string backendsString = "";
    std::vector<cv::VideoCaptureAPIs> backends = cv::videoio_registry::getBackends();

    for (int i = 0; i < backends.size(); ++i) {
        backendsString += cv::videoio_registry::getBackendName(backends[i]) + " ";
    }

    logDebug("Backends", backendsString);
}

