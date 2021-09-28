#include <iostream>
#include <opencv2/opencv.hpp>
#include <jni.h>

using namespace std;
using namespace cv;

vector<Mat> build_GDown_stack(JNIEnv *env, string vidFile, int startIndex, int endIndex, int level);
vector<Mat> ideal_bandpassing(vector<Mat> input, int dim, double wl, double wh, int samplingRate);
