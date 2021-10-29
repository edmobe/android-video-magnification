#include <omp.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <vector>
#include <numeric>
#include <cmath>
#include <algorithm>

#include "jniLogs.h"
#include "im_conv.h"
#include "processing_functions.h"
#include "lpyr_functions.h"
#include "gdown_functions.h"
#include "vector_functions.h"
#include "PeakFinder.h"


extern "C" {
#include "ellf.h"
}

using namespace std;
using namespace std::chrono;
using namespace cv;

extern "C" int butter_coeff(int, int, double, double);

constexpr auto MAX_FILTER_SIZE = 5;

// Statistical constants
constexpr double AVG_ALPHA = 0.01;
constexpr int INDICATOR_LEN = 5;
constexpr int LAG = 10;
constexpr ld THRESHOLD = 0;
constexpr ld INFLUENCE = 1;

/**
* Spatial Filtering : Gaussian blurand down sample
* Temporal Filtering : Ideal bandpass
*
* Copyright(c) 2021 Tecnologico de Costa Rica.
*
* BPM calculation author: Eduardo Moya Bello
*
* Optimization authors: Eduardo Moya Bello, Ki - Sung Lim
* Date: April 2021
*
* This work was based on a project EVM
*
* Original copyright(c) 2011 - 2012 Massachusetts Institute of Technology,
* Quanta Research Cambridge, Inc.
*
* Original authors : Hao - yu Wu, Michael Rubinstein, Eugene Shih,
* License : Please refer to the LICENCE file (MIT license)
* Original date : June 2012
**/
string amplify_spatial_Gdown_temporal_ideal(JNIEnv *env, string inFile, string outDir, double alpha,
                                         int level, double fl, double fh, int samplingRate,
                                         double chromAttenuation, int roiX, int roiY) {

    jclass clazz = env->FindClass("com/example/videomagnification/gui/processing/NativeLibManagerActivity");
    updateProgress(env, 0);

    double itime, etime;
    itime = omp_get_wtime();

    /*
     * ================= VIDEO RECEPTION =================
     * */
    logDebugAndShowUser(env, "Video reception", "Extracting video data");

    string name;
    string delimiter = "/";

    size_t last = 0; size_t next = 0;
    while ((next = inFile.find(delimiter, last)) != string::npos) {
        last = next + 1;
    }

    name = inFile.substr(last);
    name = name.substr(0, name.find("."));

    // Creates the result video name
    string outName = outDir + name + "-heart-from-" + to_string(60 * fl) + "-to-" +
                     to_string(60 * fh) + "-alpha-" + to_string(alpha) + "-level-" + to_string(level) +
                     "-chromAtn-" + to_string(chromAttenuation) + "-roiX-" + to_string(roiX) +
                     "-roiY-" + to_string(roiY) + ".avi";

    string logOutName = outDir + name + "-heart-from-" + to_string(60 * fl) + "-to-" +
                     to_string(60 * fh) + "-alpha-" + to_string(alpha) + "-level-" + to_string(level) +
                     "-chromAtn-" + to_string(chromAttenuation) + "-roiX-" + to_string(roiX) +
                     "-roiY-" + to_string(roiY) + ".txt";

    ofstream bpmLog(logOutName);

    logDebug("Video reception - Output file", outName);

    // Read video
    // Create a VideoCapture object and open the input file
    // If the input is the web camera, pass 0 instead of the video file name
    VideoCapture video(inFile);

    // Check if camera opened successfully
    if (!video.isOpened()) {
        logDebugAndShowUser(env, "Video reception", "Error opening video stream or file");
        updateProgress(env, 100);
        return "error";
    }


    // Extracting video info
    int vidHeight = (int)video.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = (int)video.get(CAP_PROP_FRAME_WIDTH);
    int fr = (int)video.get(CAP_PROP_FPS);
    int len = (int)video.get(CAP_PROP_FRAME_COUNT);
    int startIndex = 0;
    int endIndex = len - 10;

    // Write video
    // Define the codec and create VideoWriter object
    VideoWriter videoOut(outName, VideoWriter::fourcc('M', 'J', 'P', 'G'), fr,
                         Size(vidWidth, vidHeight));

    logDebug("Video reception - Video info (len)", to_string(len));
    logDebug("Video reception - Video info (Start index)", to_string(startIndex));
    logDebug("Video reception - Video info (End index)", to_string(endIndex));
    logDebug("Video reception - Video info (Height)", to_string(vidHeight));
    logDebug("Video reception - Video info (Width)", to_string(vidWidth));
    logDebug("Video reception - Video info (FPS)", to_string(fr));

    updateProgress(env, 15);

    /*
     * ================= SPATIAL FILTERING =================
     * */

    vector<Mat> Gdown_stack = build_GDown_stack(env, inFile, startIndex, endIndex, level);

    updateProgress(env, 40);
    logDebug("Spatial processing - GDown stack", "Finished building!");

    /*
     * ================= TEMPORAL FILTERING =================
     * */

    vector<Mat> filtered_stack = ideal_bandpassing(Gdown_stack, 1, fl, fh, samplingRate);

    updateProgress(env, 70);

    /*
     * ================= VIDEO OUTPUT =================
     * */

    logDebugAndShowUser(env, "Video output", "Processing " + inFile);

    // Amplify color channels in NTSC
    Scalar color_amp(alpha, alpha * chromAttenuation, alpha * chromAttenuation);

#pragma omp parallel for default(none) shared(color_amp, filtered_stack)
    for (int ind_amp = 0; ind_amp < filtered_stack.size(); ind_amp++) {
        Mat frame, frame_result;
        frame = filtered_stack[ind_amp];
        multiply(frame, color_amp, frame_result);
        filtered_stack[ind_amp] = frame_result;
    }

    for (int i = startIndex; i < endIndex; i++) {
        Mat frame;
        video >> frame;
        Gdown_stack[i] = frame;
    }

    updateProgress(env, 80);

    float progress = 0;

    // BPM EXTRACTION DATA
    const int CHUNK_SIZE = 2 * fr; // Every 2 seconds display the BPM
    vector<ld> heartRateData;
    heartRateData.reserve(endIndex);

    for (int i = startIndex; i < endIndex; i++) {

        progress = (float) i / (float) endIndex;

        logDebugAndShowUser(env, "Video output", "Reconstructing frame " +
        to_string(i + 1) + " of " + to_string(endIndex));

        Mat frame, rgbframe, d_frame, ntscframe, filt_ind, filtered, out_frame;
        // Capture frame-by-frame

        // Color conversion GBR 2 NTSC
        cvtColor(Gdown_stack[i], rgbframe, COLOR_BGR2RGB);
        d_frame = im2double(rgbframe);
        ntscframe = rgb2ntsc(d_frame);

        filt_ind = filtered_stack[i];

        Size img_size(vidWidth, vidHeight);//the dst image size,e.g.100x100
        resize(filt_ind, filtered, img_size, 0, 0, INTER_CUBIC);//resize image

        filt_ind = filtered + ntscframe;

        frame = ntsc2rgb(filt_ind);

        cv::threshold(frame, out_frame, 0.0f, 0.0f, THRESH_TOZERO);
        cv::threshold(out_frame, frame, 1.0f, 1.0f, THRESH_TRUNC);

        rgbframe = im2uint8(frame);

        cvtColor(rgbframe, out_frame, COLOR_RGB2BGR);

        filtered_stack[i] = out_frame.clone();
        Mat croppedFrame = cropFrame(out_frame, roiX, roiY);
        ld predominantColor = getPredominantRedColor(croppedFrame);
        heartRateData.push_back(predominantColor);

        updateProgress(env, 80 + 10 * progress);
    }

    updateProgress(env, 90);

    // COMPUTE PEAK FINDING ALGORITHM
    unordered_map<string, vector<ld>> peakFindingOutput =
            z_score_thresholding(heartRateData, LAG, THRESHOLD, INFLUENCE);
    vector<ld> signals = peakFindingOutput["signals"];


    string bpmText;
    int indicator_counter = 0;

    int nSamples = 0;
    bool avgReady = false;
    int lastBpmIndex = 0;
    double bpm = 0;
    double lastBpm = 0;

    for (int i = startIndex; i < endIndex; i++) {
        progress = (float) i / (float) endIndex;

        /* ====================== BPM CALCULATION ALGORITHM ===============================/
         * Author: Eduardo Moya Bello.
         * Organization: Instituto Tecnológico de Costa Rica.
         * ================================================================================
         *
         * Consider this BPM pulse succession
         *
         *                      |<----- FIRST BPM CALCULATION ----->|
         *
         *                      .     .     .                       .     .     .
         *                      |     |     |                       |     |     |
         *                      |     |     |                       |     |     |
         * _________._____._____|_____|_____|_____._____._____._____|_____|_____|_____.____...
         * i =      0     1     2     3     4     5     6     7     8     9     10    11
         *
         * - When i = 0, we do not have enough data to say if there is a positive edge, then
         *   i must be >= 1.
         * - At i = 2, we get the first BPM sample, but we cannot calculate the BPM yet, to do
         *   that, we need to have at least 1 sample (nSamples == true).
         * - If the current BPM is lower than the minimum BPM possible, then it is a false
         *   positive. The same applies if the current BPM is greater than the maximum BPM.
         * - The BPM average needs more than one BPM calculation. Therefore, we use the
         *   averageReady boolean. One can also use nSamples > 1.
         * - The indicator_counter counts how many frames the red BPM indicator will be turned
         *   on.
         *
         * */
        if (i > 1) {
            // New positive edge
            if (signals[i - 1] <= 0 && signals[i] == 1) {
                if (nSamples) {
                    double currBpm = 60.0 * fr / (i - lastBpmIndex);
                    // False positive
                    if (currBpm < fl * 60 || currBpm > fh * 60) {
                        /*
                         * This sample will not count, however BPM index will be updated.
                         * Consider this case:
                         *
                         * If the first BPM pulse is a false positive, the next pulses could
                         * never be updated, because the first pulse would always be correct,
                         * even if it is not. Furthermore, if there is a succession of false
                         * positives and the BPM index is not updated, currBpm could always
                         * be greater than fh * 60, avoiding the algorithm to update the BPM
                         * for the rest of its execution.
                         *
                         * */
                        nSamples--;
                    } else {
                        indicator_counter = INDICATOR_LEN;
                        if (avgReady) {
                            // Using exponential moving average
                            bpm = AVG_ALPHA * currBpm + (1 - AVG_ALPHA) * bpm;
                            lastBpm = bpm;
                        } else {
                            bpm = currBpm;
                            avgReady = true;
                        }
                    }
                } else {
                    indicator_counter = INDICATOR_LEN;
                }
                nSamples++;
                lastBpmIndex = i;
            }
        }

        bpmLog << "Frame " + to_string(i) + ": " + to_string(bpm) + " BPM.\n";

        if (i % CHUNK_SIZE == 0) {
            bpm == 0 ? bpmText = "Calculating..." : bpmText = "BPM: " + to_string(bpm);
        }

        logDebugAndShowUser(env, "Video output", "Writing frame " + to_string(i) +
                                                 " of " + to_string(endIndex - 2));

        if (indicator_counter > 0) {
            printIndicator(filtered_stack[i], vidWidth, true);
            indicator_counter--;
        } else {
            printIndicator(filtered_stack[i], vidWidth, false);
        }

        putText(filtered_stack[i], bpmText, Point(32, 32),
                FONT_HERSHEY_COMPLEX_SMALL, 0.8,
                Scalar(0, 0, 0), 1, LINE_AA);

        putText(filtered_stack[i], bpmText, Point(30, 30),
                FONT_HERSHEY_COMPLEX_SMALL, 0.8,
                Scalar(255, 255, 255), 1, LINE_AA);

        // Write the frame into the file 'outcpp.avi'
        videoOut.write(filtered_stack[i]);
        updateProgress(env, 90 + 10 * progress);
    }

    // When everything done, release the video capture and write object
    video.release();
    videoOut.release();

    bpmLog.close();

    etime = omp_get_wtime();

    updateProgress(env, 100);
    logDebugAndShowUser(env, "Video output", "Finished processing in " +
                                             to_string(etime - itime) + " seconds");

    return outName;
}

/**
* Spatial Filtering: Laplacian pyramid
* Temporal Filtering: substraction of two butterworth lowpass filters
*                     with cutoff frequencies fh and fl
*
* Copyright(c) 2021 Tecnologico de Costa Rica.
*
* BPM calculation author: Eduardo Moya Bello
*
* Optimization authors: Eduardo Moya Bello, Ki - Sung Lim
* Date: April 2021
*
* This work was based on a project EVM
*
* Original copyright(c) 2011 - 2012 Massachusetts Institute of Technology,
* Quanta Research Cambridge, Inc.
*
* Original authors : Hao - yu Wu, Michael Rubinstein, Eugene Shih,
* License : Please refer to the LICENCE file (MIT license)
* Original date : June 2012
**/
string amplify_spatial_lpyr_temporal_butter(JNIEnv *env, string inFile, string outDir, double alpha,
                                         double lambda_c, double fl, double fh, int samplingRate,
                                         double chromAttenuation, int roiX, int roiY) {

    jclass clazz = env->FindClass("com/example/videomagnification/gui/processing/NativeLibManagerActivity");

    double itime, etime;
    itime = omp_get_wtime();

    // Coefficients for IIR butterworth filter
    // Equivalent in Matlab/Otave to:
    //  [low_a, low_b] = butter(1, fl / samplingRate, 'low');
    //  [high_a, high_b] = butter(1, fh / samplingRate, 'low');
    int this_samplingRate = samplingRate * 2;
    butter_coeff(1, 1, this_samplingRate, fl);
    Vec2d low_a(pp[0], pp[1]);
    Vec2d low_b(aa[0], aa[1]);

    butter_coeff(1, 1, this_samplingRate, fh);
    Vec2d high_a(pp[0], pp[1]);
    Vec2d high_b(aa[0], aa[1]);

    /*
     * ================= VIDEO RECEPTION =================
     * */

    logDebugAndShowUser(env, "Video reception", "Extracting videoIn data");

    // Out videoIn preparation
    string name;
    string delimiter = "/";

    size_t last = 0; size_t next = 0;
    while ((next = inFile.find(delimiter, last)) != string::npos) {
        last = next + 1;
    }

    name = inFile.substr(last);
    name = name.substr(0, name.find("."));
    cout << name << endl;

    // Creates the result videoIn name
    string midName = outDir + name + "-breath-from-" + to_string(fl * 60) + "-to-" +
                     to_string(fh * 60) + "-alpha-" + to_string(alpha) + "-lambda_c-" + to_string(lambda_c) +
                     "-chromAtn-" + to_string(chromAttenuation) + "-roiX-" + to_string(roiX) +
                     "-roiY-" + to_string(roiY) + "-tmp" ".avi";
    string outName = outDir + name + "-breath-from-" + to_string(fl * 60) + "-to-" +
                     to_string(fh * 60) + "-alpha-" + to_string(alpha) + "-lambda_c-" + to_string(lambda_c) +
                     "-chromAtn-" + to_string(chromAttenuation) + "-roiX-" + to_string(roiX) +
                     "-roiY-" + to_string(roiY) + ".avi";
    string logOutName = outDir + name + "-breath-from-" + to_string(fl * 60) + "-to-" +
                        to_string(fh * 60) + "-alpha-" + to_string(alpha) + "-lambda_c-" + to_string(lambda_c) +
                        "-chromAtn-" + to_string(chromAttenuation) + "-roiX-" + to_string(roiX) +
                        "-roiY-" + to_string(roiY) + ".txt";

    ofstream bpmLog(logOutName);

    logDebug("Video reception - Output file", outName);

    // Read videoIn
    // Create a VideoCapture object and open the input file
    // If the input is the web camera, pass 0 instead of the videoIn file name
    VideoCapture videoIn(inFile);

    // Check if camera opened successfully
    if (!videoIn.isOpened()) {
        logDebugAndShowUser(env, "Video reception", "Error opening videoIn stream or file");
        updateProgress(env, 100);
        return "error";
    }

    // Extracting videoIn info
    int vidHeight = (int)videoIn.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = (int)videoIn.get(CAP_PROP_FRAME_WIDTH);
    int nChannels = 3;
    int fr = (int)videoIn.get(CAP_PROP_FPS);
    int len = (int)videoIn.get(CAP_PROP_FRAME_COUNT);
    int startIndex = 0;
    int endIndex = len - 10;

    // Video data
    cout << "Video information: Height-" << vidHeight << " Width-" << vidWidth
         << " FrameRate-" << fr << " Frames-" << len << endl;

    logDebug("Video reception - Video info (len)", to_string(len));
    logDebug("Video reception - Video info (Start index)", to_string(startIndex));
    logDebug("Video reception - Video info (End index)", to_string(endIndex));
    logDebug("Video reception - Video info (Height)", to_string(vidHeight));
    logDebug("Video reception - Video info (Width)", to_string(vidWidth));
    logDebug("Video reception - Video info (FPS)", to_string(fr));

    updateProgress(env, 5);

    // Write videoIn
    // Define the codec and create VideoWriter object
    VideoWriter videoMidWrite(midName, VideoWriter::fourcc('M', 'J', 'P', 'G'), fr,
                              Size(vidWidth, vidHeight));

    /*
     * ================= SPATIAL PROCESSING =================
     * */

    // First frame
    Mat frame1, rgbframe1, ntscframe1;
    vector<Mat> frame_stack;
    // Captures first frame
    videoIn >> frame1;

    // BGR to NTSC frame color space
    cvtColor(frame1, rgbframe1, COLOR_BGR2RGB);
    rgbframe1 = im2double(rgbframe1);
    ntscframe1 = rgb2ntsc(rgbframe1);

    // Compute maximum pyramid height for every frame
    int max_ht = 1 + maxPyrHt(vidWidth, vidHeight, MAX_FILTER_SIZE, MAX_FILTER_SIZE);

    // Compute the Laplace pyramid
    vector<Mat> pyr = buildLpyrfromGauss(ntscframe1, max_ht);

    vector<Mat> lowpass1 = pyr;
    vector<Mat> lowpass2 = pyr;
    vector<Mat> pyr_prev = pyr;

    // Writing the first frame (not processed)
    videoMidWrite.write(frame1);

    int nLevels = (int)pyr.size();
    // Scalar vector for color attenuation in YIQ (NTSC) color space
    Scalar color_amp(1.0f, chromAttenuation, chromAttenuation);

    float progress = 0;

    Ptr<BackgroundSubtractorMOG2> bgSub = createBackgroundSubtractorMOG2(200, 100);
    vector<int> contoursCount;
    contoursCount.reserve(endIndex - 1);

    for (int i = startIndex; i < endIndex - 1; i++) {

        progress = (float) i / (float) endIndex;

        logDebugAndShowUser(env, "Video processing", "Processing frame " + to_string(i) +
                            " of " + to_string(endIndex - 2));

        Mat frame, normalizedframe, rgbframe, out_frame, output;
        vector<Mat> filtered(nLevels);
        // Capture frame-by-frame
        videoIn >> frame;

        // Color conversion GBR 2 NTSC
        cvtColor(frame, rgbframe, COLOR_BGR2RGB);
        normalizedframe = im2double(rgbframe);
        frame = rgb2ntsc(normalizedframe);

        // Compute the Laplace pyramid
        pyr = buildLpyrfromGauss(frame, max_ht); // Has information in the upper levels

        // Temporal filtering
        // With OpenCV methods, we are accomplishing this:
        //  lowpass1 = (lowpass1 * -high_b[1] + pyr * high_a[0] + pyr_prev * high_a[1]) /
        //      high_b[0];
        //  lowpass2 = (lowpass2 * -low_b[1] + pyr * low_a[0] + pyr_prev * low_a[1]) /
        //      low_b[0];
#pragma omp parallel for default(none) shared(low_a, low_b, high_a, high_b, lowpass1, lowpass2, pyr_prev, pyr, filtered, nLevels)
        for (int l = 0; l < nLevels; l++) {
            Mat lp1_h, pyr_h, pre_h, lp1_s, lp1_r;
            Mat lp2_l, pyr_l, pre_l, lp2_s, lp2_r;

            lp1_h = -high_b[1] * lowpass1[l].clone();
            pyr_h = high_a[0] * pyr[l].clone();
            pre_h = high_a[1] * pyr_prev[l].clone();
            lp1_s = lp1_h.clone() + pyr_h.clone() + pre_h.clone();
            lp1_r = lp1_s.clone() / high_b[0];
            lowpass1[l] = lp1_r.clone();

            lp2_l = -low_b[1] * lowpass2[l].clone();
            pyr_l = low_a[0] * pyr[l].clone();
            pre_l = low_a[1] * pyr_prev[l].clone();
            lp2_s = lp2_l.clone() + pyr_l.clone() + pre_l.clone();
            lp2_r = lp2_s.clone() / low_b[0];
            lowpass2[l] = lp2_r.clone();

            Mat temp_result = lowpass1[l].clone() - lowpass2[l].clone();
            filtered[l] = temp_result.clone();
        }

        // Storing computed Laplacian pyramid as previous pyramid
        pyr_prev = pyr;

        // Amplify each spatial frecuency bands according to Figure 6 of our (EVM project) paper

        // Compute the representative wavelength lambda for the lowest spatial frecuency
        //  band of Laplacian pyramid

        // The factor to boost alpha above the bound we have in the paper. (for better visualization)
        double exaggeration_factor = 2.0f;

        double delta = lambda_c / 8.0f / (1.0f + alpha);

        double lambda = pow(pow(vidHeight, 2.0f) + pow(vidWidth, 2.0f), 0.5f) / 3.0f; // is experimental constant

#pragma omp parallel for default(none) shared(nLevels, filtered, alpha, exaggeration_factor, delta, lambda)
        for (int l = nLevels - 1; l >= 0; l--) {
            // Go one level down on pyramid each stage

            // Compute modified alpha for this level
            double currAlpha = lambda / delta / 8.0f - 1.0f;
            currAlpha = currAlpha * exaggeration_factor;

            Mat mat_result;

            if (l == nLevels - 1 || l == 0) { // ignore the highest and lowest frecuency band
                Size mat_sz(filtered[l].cols, filtered[l].rows);
                mat_result = Mat::zeros(mat_sz, CV_64FC3);
            }
            else if (currAlpha > alpha) { // representative lambda exceeds lambda_c
                mat_result = alpha * filtered[l].clone();
            }
            else {
                mat_result = currAlpha * filtered[l].clone();
            }
            filtered[l] = mat_result.clone();

            lambda = lambda / 2.0f;
        }

        // Render on the input videoIn

        output = reconLpyr(filtered);

        multiply(output, color_amp, output);

        output = frame.clone() + output.clone();

        rgbframe = ntsc2rgb(output);

        threshold(rgbframe, rgbframe, 0.0f, 0.0f, THRESH_TOZERO);
        threshold(rgbframe, rgbframe, 1.0f, 1.0f, THRESH_TRUNC);

        frame = im2uint8(rgbframe);

        cvtColor(frame, frame, COLOR_RGB2BGR);

        // EXTRACT BPM
        Mat roi = frame(Rect(roiX, roiY, 100, 100));
        Mat mask;
        bgSub->apply(roi, mask);
        vector<vector<Point>> contours;
        findContours(mask, contours, RETR_TREE, CHAIN_APPROX_SIMPLE);
        contoursCount.push_back(0);

#pragma omp parallel for default(none) shared(i, contours, contoursCount)
        for (int cnt = 0; cnt < contours.size(); cnt++) {
            double area = contourArea(contours[cnt]);
            if (area > 100)
                contoursCount[i]++;
        }

        videoMidWrite.write(frame);
        updateProgress(env, 5 + 75 * progress);
    }

    videoIn.release();
    videoMidWrite.release();


    // BPM PROCESSING
    double bpm = 0;
    const int CHUNK_SIZE = 2 * fr; // Every 2 seconds dislpay the BPM
    vector<bool> signal;
    signal.reserve(endIndex);


    VideoCapture videoMidRead(midName);
    VideoWriter videoOut(outName, VideoWriter::fourcc('M', 'J', 'P', 'G'), fr,
                              Size(vidWidth, vidHeight));

    string bpmText;
    int indicator_counter = 0;

    int nSamples = 0;
    bool avgReady = false;
    int lastBpmIndex = 0;
    double lastBpm = 0;



    for (int i = startIndex; i < endIndex - 1; i++) {

        progress = (float) i / (float) endIndex;
        /* ====================== BPM CALCULATION ALGORITHM ===============================/
         * Author: Eduardo Moya Bello.
         * Organization: Instituto Tecnológico de Costa Rica.
         * ================================================================================
         *
         * Consider this BPM pulse succession
         *
         *                      |<----- FIRST BPM CALCULATION ----->|
         *
         *                      .     .     .                       .     .     .
         *                      |     |     |                       |     |     |
         *                      |     |     |                       |     |     |
         * _________._____._____|_____|_____|_____._____._____._____|_____|_____|_____.____...
         * i =      0     1     2     3     4     5     6     7     8     9     10    11
         *
         * - When i = 0, we do not have enough data to say if there is a positive edge, then
         *   i must be >= 1.
         * - At i = 2, we get the first BPM sample, but we cannot calculate the BPM yet, to do
         *   that, we need to have at least 1 sample (nSamples == true).
         * - If the current BPM is lower than the minimum BPM possible, then it is a false
         *   positive. The same applies if the current BPM is greater than the maximum BPM.
         * - The BPM average needs more than one BPM calculation. Therefore, we use the
         *   averageReady boolean. One can also use nSamples > 1.
         * - The indicator_counter counts how many frames the red BPM indicator will be turned
         *   on.
         *
         * */
        if (i >= 1) {
            // New positive edge
            if (contoursCount[i] && !contoursCount[i - 1]) {
                if (nSamples) {
                    double currBpm = 60.0 * fr / (i - lastBpmIndex);
                    // False positive
                    if (currBpm < fl * 60 || currBpm > fh * 60) {
                        /*
                         * This sample will not count, however BPM index will be updated.
                         * Consider this case:
                         *
                         * If the first BPM pulse is a false positive, the next pulses could
                         * never be updated, because the first pulse would always be correct,
                         * even if it is not. Furthermore, if there is a succession of false
                         * positives and the BPM index is not updated, currBpm could always
                         * be greater than fh * 60, avoiding the algorithm to update the BPM
                         * for the rest of its execution.
                         *
                         * */
                        nSamples--;
                    } else {
                        indicator_counter = INDICATOR_LEN;
                        if (avgReady) {
                            // Using exponential moving average
                            bpm = AVG_ALPHA * currBpm + (1 - AVG_ALPHA) * bpm;
                            lastBpm = bpm;
                        } else {
                            bpm = currBpm;
                            avgReady = true;
                        }
                    }
                } else {
                    // First sample
                    indicator_counter = INDICATOR_LEN;
                }
                nSamples++;
                lastBpmIndex = i;
            }
        }

        bpmLog << "Frame " + to_string(i) + ": " + to_string(bpm) + " BPM.\n";

        if (i % CHUNK_SIZE == 0) {
            bpm == 0 ? bpmText = "Calculating..." : bpmText = "BPM: " + to_string(bpm);
        }

        logDebugAndShowUser(env, "Video output", "Writing frame " + to_string(i) +
                                                     " of " + to_string(endIndex - 2));

        Mat frame;
        // Capture frame-by-frame
        videoMidRead >> frame;

        if (indicator_counter > 0) {
            printIndicator(frame, vidWidth, true);
            indicator_counter--;
        } else {
            printIndicator(frame, vidWidth, false);
        }

        putText(frame, bpmText, Point(32, 32),
                FONT_HERSHEY_COMPLEX_SMALL, 0.8,
                Scalar(0, 0, 0), 1, LINE_AA);

        putText(frame, bpmText, Point(30, 30),
                FONT_HERSHEY_COMPLEX_SMALL, 0.8,
                Scalar(255, 255, 255), 1, LINE_AA);

        // Write the frame into the file 'outcpp.avi'
        videoOut.write(frame);
        updateProgress(env, 80 + 20 * progress);

    }

    videoMidRead.release();
    videoOut.release();
    bpmLog.close();

    etime = omp_get_wtime();

    updateProgress(env, 100);
    logDebugAndShowUser(env, "Video output", "Finished processing in " +
                                             to_string(etime - itime) + " seconds");

    return outName;
}
