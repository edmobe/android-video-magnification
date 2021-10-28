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
#include "gdown_functions.h"

/**
* GDOWN_STACK = build_GDown_stack(VID_FILE, START_INDEX, END_INDEX, LEVEL)
*
* Apply Gaussian pyramid decomposition on VID_FILE from START_INDEX to
*  END_INDEX and select a specific band indicated by LEVEL
*
* GDOWN_STACK: stack of one band of Gaussian pyramid of each frame
*  the first dimension is the time axis
*  the second dimension is the y axis of the video
*  the third dimension is the x axis of the video
*  the forth dimension is the color channel
*
* Copyright(c) 2011 - 2012 Massachusetts Institute of Technology,
*  Quanta Research Cambridge, Inc.
*
* Authors: Hao - yu Wu, Michael Rubinstein, Eugene Shih,
* License : Please refer to the LICENCE file
* Date : June 2012
*
* --Update--
* Code translated to C++
* Author: Ki - Sung Lim
* Date: May 2021
**/
vector<Mat> build_GDown_stack(JNIEnv *env, string vidFile, int startIndex, int endIndex, int level) {

    // Read video
    // Create a VideoCapture object and open the input file
    // If the input is the web camera, pass 0 instead of the video file name
    VideoCapture video(vidFile);

    // Check if camera opened successfully
    if (!video.isOpened()) {
        logDebugAndShowUser(env, "Video processing", "Error opening video stream or file");
        exit(1);
    }

    // Extracting video info
    int vidHeight = (int)video.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = (int)video.get(CAP_PROP_FRAME_WIDTH);
    int nChannels = 3;
    int t_size = endIndex - startIndex + 1;

    vector<Mat> GDown_stack;
    GDown_stack.reserve(t_size);

    for (int i = startIndex; i < endIndex; i++) {
        Mat frame;
        video >> frame;
        GDown_stack.push_back(frame);
    }

    logDebugAndShowUser(env, "Spatial processing", "Building GDown stack");

#pragma omp parallel for shared(GDown_stack)
    for (int i = startIndex; i < endIndex; i++) {
        Mat frame, rgbframe, ntscframe;
        vector<Mat> pyr_output;
        // Capture frame-by-frame
        //video >> frame;

        cvtColor(GDown_stack[i], rgbframe, COLOR_BGR2RGB);
        frame = im2double(rgbframe);
        ntscframe = rgb2ntsc(frame);

        buildPyramid(ntscframe, pyr_output, level + 1, BORDER_REFLECT101);

        GDown_stack[i] = pyr_output[level].clone();
    }

    video.release();

    return GDown_stack;
}

/**
* FILTERED = ideal_bandpassing(INPUT, DIM, WL, WH, SAMPLINGRATE)
*
* Apply ideal band pass filter on INPUT along dimension DIM.
*
* WL: lower cutoff frequency of ideal band pass filter
* WH : higher cutoff frequency of ideal band pass filter
* SAMPLINGRATE : sampling rate of INPUT
*
* Copyright(c) 2011 - 2012 Massachusetts Institute of Technology,
*  Quanta Research Cambridge, Inc.
*
* Authors : Hao - yu Wu, Michael Rubinstein, Eugene Shih,
* License : Please refer to the LICENCE file
* Date : June 2012
*
* --Update--
* Code translated to C++
* Author: Ki - Sung Lim
* Date: May 2021
*
* --Notes--
* The commented sections that say "test" or "testing" are manual tests that were applied to the
* method or algorithm before them. You can just ignore them or use them if you want to check
* the results.
**/
vector<Mat> ideal_bandpassing(vector<Mat> input, int dim, double wl, double wh, int samplingRate) {

    /*
    Comprobation of the dimention
    It is so 'dim' doesn't excede the actual dimension of the input
    In Matlab you can shift the dimentions of a matrix, for example, 3x4x3 can be shifted to 4x3x3
    with the same values stored in the correspondant dimension.
    Here (C++) it is not applied any shifting, yet.
    */
    if (dim > 1 + input[0].channels()) {
        cout << "Exceed maximun dimension" << endl;
        exit(1);
    }

    // Printing the cut frequencies
    //cout << "F1: " + to_string(wl) + " F2: " + to_string(wh) << endl;

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
    Freq.convertTo(Freq, CV_64FC1, alpha); // alpha is mult to every value

    Mat mask = (Freq > wl) & (Freq < wh); // creates a boolean matrix/mask

    // Sum of rows, columns and color channels of a single cv::Mat in input
    int total_rows = input[0].rows * input[0].cols * input[0].channels();

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
    If you didn't see it: pixel_time-row/x-col/y-colorchannel
    */
    Mat temp_dft(total_rows, n, CV_64FC1);

    // Here we populate the forementioned matrix
#pragma omp parallel for
    for (int x = 0; x < input[0].rows; x++) {
#pragma omp parallel for
        for (int y = 0; y < input[0].cols; y++) {
#pragma omp parallel for shared(input, temp_dft)
            for (int i = 0; i < n; i++) {
                int pos_temp = 3 * (y + x * input[0].cols);

                Vec3d pix_colors = input[i].at<Vec3d>(x, y);
                temp_dft.at<double>(pos_temp, i) = pix_colors[0];
                temp_dft.at<double>(pos_temp + 1, i) = pix_colors[1];
                temp_dft.at<double>(pos_temp + 2, i) = pix_colors[2];
            }
        }
    }

    // Testing the values in temp_dft [OK - PASSED]
    //int test_index = n-1;
    //cout << "-----Testing vectorizing values in " + to_string(test_index) + "-------" << endl;
    //for (int i = total_rows-3; i < total_rows; i++) {
    //    cout << temp_dft.at<double>(i, test_index) << endl;
    //}
    //cout << input[test_index].at<Vec3d>(5, 9) << endl;
    //cout << "----------End of tests----------" << endl;

    Mat input_dft, input_idft; // For DFT input / output

    // 1-D DFT applied for every row, complex output
    // (2 value real-complex vector)
    dft(temp_dft, input_dft, DFT_ROWS | DFT_COMPLEX_OUTPUT);

    // Testing the values in the transformation DFT [OK - Passed]
    //int test_index = total_rows-1;
    //cout << "-----Testing DFT values in " + to_string(test_index) + "-------" << endl;
    //for (int i = 0; i < 10; i++) {
    //    cout << input_dft.at<Vec2d>(test_index, i) << endl;
    //}
    //for (int i = 879; i < 890; i++) {
    //    cout << input_dft.at<Vec2d>(test_index, i) << endl;
    //}
    //cout << "----------End of tests----------" << endl;

    // Filtering the video matrix with a mask
#pragma omp parallel for
    for (int i = 0; i < total_rows; i++) {
#pragma omp parallel for shared(input_dft)
        for (int j = 0; j < n; j++) {
            if (!mask.at<bool>(j, 0)) {
                Vec2d temp_zero_vector(0.0f, 0.0f);
                input_dft.at<Vec2d>(i, j) = temp_zero_vector;
            }
        }
    }

    // 1-D inverse DFT applied for every row, complex output
    // Only the real part is importante
    idft(input_dft, input_idft, DFT_ROWS | DFT_COMPLEX_INPUT | DFT_SCALE);

    // Testing the values for the transformation IDFT [OK - Passed]
    //int test_index = total_rows-1;
    //cout << "-----Testing IDFT values in " + to_string(test_index) + "-------" << endl;
    //for (int i = 0; i < 10; i++) {
    //    cout << input_idft.at<Vec2d>(test_index, i) << endl;
    //}
    //for (int i = 879; i < 890; i++) {
    //    cout << input_idft.at<Vec2d>(test_index, i) << endl;
    //}
    //cout << "----------End of tests----------" << endl;

    // Reording the matrix to a vector of matrixes,
    // contrary of what was done for temp_dft
#pragma omp parallel for shared(input)
    for (int i = 0; i < n; i++) {
        Mat temp_filtframe(input[0].rows, input[0].cols, CV_64FC3);
        //int pos_temp = 0;
#pragma omp parallel for
        for (int x = 0; x < input[0].rows; x++) {
#pragma omp parallel for shared(input_idft, temp_filtframe)
            for (int y = 0; y < input[0].cols; y++) {

                int pos_temp = 3 * (y + x * input[0].cols);

                Vec3d pix_colors;
                pix_colors[0] = input_idft.at<Vec2d>(pos_temp, i)[0];
                pix_colors[1] = input_idft.at<Vec2d>(pos_temp + 1, i)[0];
                pix_colors[2] = input_idft.at<Vec2d>(pos_temp + 2, i)[0];
                temp_filtframe.at<Vec3d>(x, y) = pix_colors;

                //pos_temp += 3;
            }
        }

        input[i] = temp_filtframe;
    }

    return input;
}

double calculateBpm(int highFrameIndex, int lowFrameIndex, int fr) {
    return fr * 60 / (highFrameIndex - lowFrameIndex);
}

double calculateAverageBpm(vector<int> data, int fr, double fh, double fl) {
    float maxBpm = fh * 60;
    float minBpm = fl * 60;
    double bpm = 0;
    double currBpm = 0;
    double averageBpm = 0;
    int incorrectReadings = 0;
    for (int i = 1; i < data.size(); i++) {
        currBpm = calculateBpm(data[i], data[i - 1], fr);
        if (currBpm > maxBpm || currBpm < minBpm)
            incorrectReadings++;
        else
            bpm += currBpm;
    }

    averageBpm = bpm / ((data.size() - 1) - incorrectReadings);
    bpm = bpm + averageBpm * incorrectReadings;

    return bpm / (data.size() - 1);
}

Mat cropFrame(Mat frame, int roiX, int roiY) {
    // TODO: validate if bounds are covered in video editor
    if (roiX >= 50)
        roiX -= 50;
    if (roiY >= 50)
        roiY -= 50;
    // Setup a rectangle to define your region of interest
    cv::Rect myRoi(roiX, roiY, 100, 100);
    return frame(myRoi);
}

long double getPredominantRedColor(Mat frame) {
    Scalar tempVal = mean(frame);
    return tempVal.val[2];
}

void printIndicator(Mat &frame, int vidWidth, bool redColor) {
    Scalar color;
    if (redColor) {
        color = Scalar( 0, 0, 255 );
    } else {
        Scalar( 0, 0, 0 );
    }

    circle(frame, Point( vidWidth - 30,  30),
            20, color, FILLED, FILLED);
}