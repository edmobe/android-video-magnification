#include <iostream>
#include <opencv2/opencv.hpp>
#include <jni.h>

using namespace std;
using namespace cv;

int maxPyrHt(int frameWidth, int frameHeight, int filterSizeX, int filterSizeY);

Mat pyrVectorToMat(vector<Mat> pyr);

vector<Mat> pyrMatToVector(Mat mat, vector<Mat> pyrTemplate);

vector<Mat> buildLpyrfromGauss(Mat image, int levels);

vector<Mat> buildLpyr(Mat image, int levels);

vector<vector<Mat>> build_Lpyr_stack(JNIEnv *env, string vidFile, int startIndex, int endIndex);

Mat reconLpyr(vector<Mat> lpyr);

vector<vector<Mat>> ideal_bandpassing_lpyr(JNIEnv *env, vector<vector<Mat>>& input,
                                           int dim, double wl, double wh, double samplingRate);