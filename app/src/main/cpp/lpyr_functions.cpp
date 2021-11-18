#include <omp.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <numeric>
#include <cmath>
#include <jni.h>

#include "im_conv.h"
#include "jniLogs.h"
#include "lpyr_functions.h"

using namespace std;
using namespace cv;

constexpr auto MAX_FILTER_SIZE = 5;
constexpr auto BAR_WIDTH = 70;

int maxPyrHt(int frameWidth, int frameHeight, int filterSizeX, int filterSizeY) {
    // 1D image
    if (frameWidth == 1 || frameHeight == 1) {
        frameWidth = frameHeight = 1;
        filterSizeX = filterSizeY = filterSizeX * filterSizeY;
    }
        // 2D image
    else if (filterSizeX == 1 || filterSizeY == 1) {
        filterSizeY = filterSizeX;
    }
    // Stop condition
    if (frameWidth < filterSizeX || frameWidth < filterSizeY ||
        frameHeight < filterSizeX || frameHeight < filterSizeY)
    {
        return 0;
    }
        // Next level
    else {
        return 1 + maxPyrHt(frameWidth / 2, frameHeight / 2, filterSizeX, filterSizeY);
    }
}

vector<Mat> buildLpyrfromGauss(Mat image, int levels) {
    vector<Mat> gaussianPyramid;
    vector<Mat> laplacianPyramid(levels);

    buildPyramid(image, gaussianPyramid, levels, BORDER_REFLECT);

//#pragma omp parallel for shared(gaussianPyramid, laplacianPyramid)
    for (int l = 0; l < levels - 1; l++) {
        Mat expandedPyramid;
        //pyrDown(gaussianPyramid[l], gaussianPyramid[l + 1], Size((gaussianPyramid[l].cols + 1) / 2, (gaussianPyramid[l].rows + 1) / 2), BORDER_REFLECT101);
        pyrUp(gaussianPyramid[l+1], expandedPyramid, Size(gaussianPyramid[l].cols, gaussianPyramid[l].rows), BORDER_REFLECT101);
        subtract(gaussianPyramid[l], expandedPyramid, laplacianPyramid[l]);
    }

    laplacianPyramid[levels-1] = gaussianPyramid[levels-1].clone();

    return laplacianPyramid;
}

vector<vector<Mat>> build_Lpyr_stack(JNIEnv *env, string vidFile, int startIndex, int endIndex) {
    // Read video
    // Create a VideoCapture object and open the input file
    // If the input is the web camera, pass 0 instead of the video file name
    VideoCapture video(vidFile);

    // Extract video info
    int vidHeight = (int)video.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = (int)video.get(CAP_PROP_FRAME_WIDTH);

    // Compute maximum pyramid height for every frame
    int max_ht = 1 + maxPyrHt(vidWidth, vidHeight, MAX_FILTER_SIZE, MAX_FILTER_SIZE);

    vector<vector<Mat>> pyr_stack(endIndex, vector<Mat>(max_ht));

    logDebugAndShowUser(env, "Spatial processing", "Building LPYR stack");

    for (int i = startIndex; i < endIndex; i++) {

        // Define variables
        Mat frame, rgbframe, ntscframe;

        // Capture frame-by-frame
        video >> frame;

        // If the frame is empty, break immediately
        if (frame.empty())
            break;

        cvtColor(frame, rgbframe, COLOR_BGR2RGB);
        rgbframe = im2double(rgbframe);
        ntscframe = rgb2ntsc(rgbframe);

        vector<Mat> pyr_output = buildLpyrfromGauss(ntscframe, max_ht);
        pyr_stack[i] = pyr_output;
    }

    return pyr_stack;
}

/**
* res = reconLpyr(lpyr)
*
* Reconstruct image from Laplacian pyramid, as created by buildLpyr.
*
* lpyr is a vector of matrices containing the N pyramid subbands, ordered from fine
* to coarse.
*
* --Update--
* Code translated to C++
* Author: Ki - Sung Lim
* Date: June 2021
*/
Mat reconLpyr(vector<Mat> lpyr) {
    int levels = (int)lpyr.size();

    int this_level = levels - 1;
    Mat res = lpyr[this_level].clone();

    for (int l = levels - 2; l >= 0; l--) {
        Size res_sz = Size(lpyr[l].cols, lpyr[l].rows);
        pyrUp(res, res, res_sz, BORDER_REFLECT101);
        add(res, lpyr[l], res);
    }

    return res.clone();
}

vector<vector<Mat>> ideal_bandpassing_lpyr(JNIEnv *env, vector<vector<Mat>>& input,
                                           int dim, double wl, double wh, double samplingRate) {

    logDebugAndShowUser(env, "Temporal processing", "Starting temporal analysis");

    /*
    Comprobation of the dimention
    It is so 'dim' doesn't excede the actual dimension of the input
    In Matlab you can shift the dimentions of a matrix, for example, 3x4x3 can be shifted to 4x3x3
    with the same values stored in the correspondant dimension.
    Here (C++) it is not applied any shifting, yet.
    */
    if (dim > 1 + input[0][0].channels()) {
        logDebugAndShowUser(env, "Temporal processing",
                            "Error: exceeded maximum dimension");
        exit(1);
    }

    vector<vector<Mat>> filtered = input;

    // Number of frames in the video
    // Represents time
    int n = (int)input.size();

    // Temporal vector that's constructed for the mask
    // iota is used to fill the vector with a integer sequence
    // [0, 1, 2, ..., n]
    vector<int> Freq_temp(n);
    iota(begin(Freq_temp), end(Freq_temp), 0); //0 is the starting number

    // Initialize the cv::Mat with the temp vector and without copying values
    Mat Freq(Freq_temp, false);
    double alpha = (double)samplingRate / (double)n;
    Freq.convertTo(Freq, CV_64FC1, alpha);

    Mat mask = (Freq > wl) & (Freq < wh); // creates a boolean matrix/mask

    // Sum of total pixels to be processed
    int total_pixels = 0;
    int levels = (int)input[0].size();

    updateProgress(env, 20);

#pragma omp parallel for
    for (int level = 0; level < levels; level++) {
        total_pixels += input[0][level].cols * input[0][level].rows * input[0][0].channels();
    }

    /*
    Temporal matrix that is constructed so the DFT method (Discrete Fourier Transform)
    that OpenCV provides can be used. The most common use for the DFT in image
    processing is the 2-D DFT, in this case we want 1-D DFT for every pixel time vector.
    Every row of the matrix is the timeline of an specific pixel.
    The structure of temp_dft is:
    [
         [pixel_0000, pixel_1000, pixel_2000, ..., pixel_n000],
         [pixel_0001, pixel_1001, pixel_2001, ..., pixel_n001],
         [pixel_0002, pixel_1002, pixel_2002, ..., pixel_n002],
         [pixel_0010, pixel_1010, pixel_2010, ..., pixel_n010],
         .
         .
         .
         [pixel_0xy0, pixel_1xy0, pixel_2xy0, ..., pixel_nxy0],
         .
         .
         .
         [pixel_0xy3, pixel_1xy3, pixel_2xy3, ..., pixel_nxy0],
    ]
    In other words: pixel_time-row/x-col/y-colorchannel
    */
    Mat tmp(total_pixels, n, CV_64FC1);

    updateProgress(env, 25);
    logDebugAndShowUser(env, "Temporal processing", "Populating the DFT matrix");

    // Here we populate the forementioned matrix
    // 14.99 s
#pragma omp parallel for
    for (int level = 0; level < levels; level++) {
#pragma omp parallel for
        for (int x = 0; x < input[0][level].rows; x++) {
#pragma omp parallel for
            for (int y = 0; y < input[0][level].cols; y++) {
#pragma omp parallel for shared(input, tmp)
                for (int i = 0; i < n; i++) {
                    int pos_temp = 3 * (y + x * input[0][level].cols);
                    Vec3d pix_colors = input[i][level].at<Vec3d>(x, y);
                    tmp.at<double>(pos_temp, i) = pix_colors[0];
                    tmp.at<double>(pos_temp + 1, i) = pix_colors[1];
                    tmp.at<double>(pos_temp + 2, i) = pix_colors[2];
                }
            }
        }
    }

    updateProgress(env, 45);

    logDebugAndShowUser(env, "Temporal processing", "Calculating the DFT");

    /*
    cout << "0: " << tmp.at<double>(0, 0) << endl;
    cout << "1: " << tmp.at<double>(0, 1) << endl;
    cout << "2: " << tmp.at<double>(0, 2) << endl;
    cout << "3: " << tmp.at<double>(0, 3) << endl;
    cout << "4: " << tmp.at<double>(0, 4) << endl;
    cout << "5: " << tmp.at<double>(0, 5) << endl;
    cout << "6: " << tmp.at<double>(0, 6) << endl;
    cout << "7: " << tmp.at<double>(0, 7) << endl;
    cout << "8: " << tmp.at<double>(0, 8) << endl;
    cout << "9: " << tmp.at<double>(0, 9) << endl;
    cout << "10: " << tmp.at<double>(0, 10) << endl;
    */


    dft(tmp, tmp, DFT_ROWS | DFT_COMPLEX_OUTPUT);

    updateProgress(env, 55);

    logDebugAndShowUser(env, "Temporal processing",
                        "Filtering the video matrix with a mask");

    // Filtering the video matrix with a mask

#pragma omp parallel for
    for (int i = 0; i < total_pixels; i++) {
#pragma omp parallel for shared(tmp)
        for (int j = 0; j < n; j++) {
            if (!mask.at<bool>(j, 0)) {
                Vec2d temp_zero_vector(0.0f, 0.0f);
                tmp.at<Vec2d>(i, j) = temp_zero_vector;
            }
        }
    }

    updateProgress(env, 60);

    logDebugAndShowUser(env, "Temporal processing",
                        "Calculating the IDFT");

    // 1-D inverse DFT applied for every row, complex output
    // Only the real part is important
    idft(tmp, tmp, DFT_ROWS | DFT_COMPLEX_INPUT | DFT_SCALE);

    updateProgress(env, 70);

    logDebugAndShowUser(env, "Temporal processing",
                        "Retrieving matrix data");

    // Reording the matrix to a vector of matrixes,
    // contrary of what was done for temp_dft
#pragma omp parallel for shared(input)
    for (int i = 0; i < n; i++) {
        vector<Mat> levelsVector(levels);
#pragma omp parallel for shared(levelsVector)
        for (int level = 0; level < levels; level++) {
            Mat temp_filtframe(input[0][level].rows, input[0][level].cols, CV_64FC3);
#pragma omp parallel for
            for (int x = 0; x < input[0][level].rows; x++) {
#pragma omp parallel for shared(tmp, temp_filtframe)
                for (int y = 0; y < input[0][level].cols; y++) {
                    int pos_temp = 3 * (y + x * input[0][level].cols);

                    Vec3d pix_colors;
                    pix_colors[0] = tmp.at<Vec2d>(pos_temp, i)[0];
                    pix_colors[1] = tmp.at<Vec2d>(pos_temp + 1, i)[0];
                    pix_colors[2] = tmp.at<Vec2d>(pos_temp + 2, i)[0];
                    temp_filtframe.at<Vec3d>(x, y) = pix_colors;
                }
            }
            levelsVector[level] = temp_filtframe.clone();
        }
        filtered[i] = levelsVector;
    }

    return filtered;
}

bpmValues getBpmValues(vector<char> contourList, int fr, int secondsToUpdate) {
    double factor = fr * 60;
    int keyFrameCount = 0;
    int lastImpulseIndex = 0;
    double currBpm = 0;
    int bpmSampleCount = 0;
    double averageBpm = 0;

    bpmValues result;
    result.bpm.reserve(contourList.size());
    result.signal.reserve(contourList.size());

    secondsToUpdate *= fr;

    if (secondsToUpdate < 1) {
        secondsToUpdate = 1;
    }

    // First frame
    result.signal.push_back(contourList[0]);
    result.bpm.push_back(0);

    for (int i = 1; i <= contourList.size() - 1; i++) {
        // Time to update the BPM
        if (bpmSampleCount > 0 && i % secondsToUpdate == 0) {
            averageBpm = currBpm / bpmSampleCount;
            bpmSampleCount = 0;
        }
        // POSEDGE
        if (!contourList[i - 1] && contourList[i]) {
            result.signal.push_back(contourList[i]);
            // This is not the first BPM calculation
            if (keyFrameCount > 0) {
                currBpm += factor / (i - lastImpulseIndex);
                bpmSampleCount++;
            }
            lastImpulseIndex = i;
            keyFrameCount++;
        } else {
            result.signal.push_back(0);
        }
        result.bpm.push_back(averageBpm);
    }

    return result;
}

