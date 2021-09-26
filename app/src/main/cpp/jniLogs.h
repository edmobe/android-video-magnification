#include <iostream>
#include <jni.h>

void logDebug(std::string title, std::string str);
void logDebugAndShowUser(JNIEnv *env, std::string title, std::string str);
void updateProgress(JNIEnv *env, int progress);
void logDebugVideoBackends();