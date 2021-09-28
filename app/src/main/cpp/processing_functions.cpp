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

#include "jniLogs.h"
#include "im_conv.h"
#include "processing_functions.h"
#include "lpyr_functions.h"
#include "gdown_functions.h"

using namespace std;
using namespace cv;

constexpr auto MAX_FILTER_SIZE = 5;
constexpr auto BAR_WIDTH = 70;

/**
* Spatial Filtering : Laplacian pyramid
* Temporal Filtering : Ideal bandpass
*
* Copyright(c) 2021 Tecnologico de Costa Rica.
*
* Authors: Eduardo Moya Bello, Ki - Sung Lim
* Date : June 2021
*
* This work was based on a project EVM
*
* Original copyright(c) 2011 - 2012 Massachusetts Institute of Technology,
* Quanta Research Cambridge, Inc.
*
* Original authors : Hao - yu Wu, Michael Rubinstein, Eugene Shih,
* License : Please refer to the LICENCE file (MIT license)
* Original date : June 2012
*/
int amplify_spatial_lpyr_temporal_ideal(JNIEnv *env, string inFile, string outDir, double alpha,
                                        double lambda_c, double fl, double fh, double samplingRate,
                                        double chromAttenuation) {

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
    string outName = outDir + name + "-ideal-from-" + to_string(fl) + "-to-" +
                     to_string(fh) + "-alpha-" + to_string(alpha) + "-lambda_c-" + to_string(lambda_c) +
                     "-chromAtn-" + to_string(chromAttenuation) + ".avi";

    logDebug("Video reception - Output file", outName);

    // Create a VideoCapture object and open the input file
    // If the input is the web camera, pass 0 instead of the video file name
    //VideoCapture video(0);
    VideoCapture video(inFile);

    // Check if video opened successfully

    if (!video.isOpened()) {
        logDebugAndShowUser(env, "Video reception", "Error opening video stream or file");
        updateProgress(env, 100);
        return -1;
    }

    // Extract video info
    int len = (int)video.get(CAP_PROP_FRAME_COUNT);
    int startIndex = 0;
    int endIndex = len - 10;
    int vidHeight = (int)video.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = (int)video.get(CAP_PROP_FRAME_WIDTH);
    int fr = (int)video.get(CAP_PROP_FPS);

    // Compute maximum pyramid height for every frame
    int max_ht = 1 + maxPyrHt(vidWidth, vidHeight, MAX_FILTER_SIZE, MAX_FILTER_SIZE);

    logDebug("Video reception - Video info (len)", to_string(len));
    logDebug("Video reception - Video info (Start index)", to_string(startIndex));
    logDebug("Video reception - Video info (End index)", to_string(endIndex));
    logDebug("Video reception - Video info (Height)", to_string(vidHeight));
    logDebug("Video reception - Video info (Width)", to_string(vidWidth));
    logDebug("Video reception - Video info (FPS)", to_string(fr));
    logDebug("Video reception - Maximum pyramid height", to_string(max_ht));

    updateProgress(env, 5);

    /*
     * ================= SPATIAL PROCESSING =================
     * */

    vector<vector<Mat>> pyr_stack = build_Lpyr_stack(env, inFile, startIndex, endIndex);
    updateProgress(env, 15);
    logDebug("Spatial processing - LPYR stack", "Finished building!");

    /*
     * ================= TEMPORAL PROCESSING =================
     * */
    vector<vector<Mat>> filteredStack = ideal_bandpassing_lpyr(env, pyr_stack, 3, fl, fh,
                                                               samplingRate);

    updateProgress(env, 70);

    logDebugAndShowUser(env, "Video output",
                        "Preparing output video");

    /*
     * ================= VIDEO OUTPUT =================
     * */
    Scalar colorAmp(alpha, alpha * chromAttenuation, alpha * chromAttenuation);

    // Amplify color channels in NTSC
#pragma omp parallel for shared(filteredStack, colorAmp)
    for (int frame = 0; frame < filteredStack.size(); frame++) {
#pragma omp parallel for shared(filteredStack, colorAmp)
        for (int levelFrame = 0; levelFrame < filteredStack[frame].size(); levelFrame++) {
            multiply(filteredStack[frame][levelFrame], colorAmp, filteredStack[frame][levelFrame]);
        }
    }

    // Render on the input video to make the output video
    // Define the codec and create VideoWriter object
    VideoWriter videoOut(outName, VideoWriter::fourcc('M', 'J', 'P', 'G'), fr,
                         Size(vidWidth, vidHeight));

    int k = 0;

    int progress = 0;

    for (int i = startIndex; i < endIndex; i++) {
        logDebugAndShowUser(env, "Video output", "Processing " + inFile +
                            " " + to_string(progress * 5) + "%");

        Mat frame, rgbframe, ntscframe, filt_ind, filtered, out_frame;
        // Capture frame-by-frame
        video >> frame;

        // Color conversion GBR 2 NTSC
        cvtColor(frame, rgbframe, COLOR_BGR2RGB);
        rgbframe = im2double(rgbframe);
        ntscframe = rgb2ntsc(rgbframe);

        //imshow("Converted", ntscframe);

        filt_ind = filteredStack[k][0];
        //imshow("Filtered stack", filt_ind);

        Size img_size(vidWidth, vidHeight);//the dst image size,e.g.100x100
        resize(filt_ind, filtered, img_size, 0, 0, INTER_CUBIC);//resize image

        filtered = filtered + ntscframe;
        //imshow("Filtered", filtered);

        frame = ntsc2rgb(filtered);
        //imshow("Frame", frame);

#pragma omp parallel for
        for (int x = 0; x < frame.rows; x++) {
            for (int y = 0; y < frame.cols; y++) {
                Vec3d this_pixel = frame.at<Vec3d>(x, y);
                for (int z = 0; z < 3; z++) {
                    if (this_pixel[z] > 1) {
                        this_pixel[z] = 1;
                    }

                    if (this_pixel[z] < 0) {
                        this_pixel[z] = 0;
                    }
                }

                frame.at<Vec3d>(x, y) = this_pixel;
            }
        }

        rgbframe = im2uint8(frame);
        //imshow("Rgb frame", rgbframe);

        cvtColor(rgbframe, out_frame, COLOR_RGB2BGR);
        //imshow("Out frame", out_frame);

        // Write the frame into the output file
        videoOut.write(out_frame);

        progress = 20 * (i + 1) / endIndex;

        updateProgress(env, 70 + progress);

        k++;
    }

    etime = omp_get_wtime();

    // When everything done, release the video capture and write object
    video.release();
    videoOut.release();

    updateProgress(env, 100);
    logDebugAndShowUser(env, "Video output", "Finished processing in " +
                        to_string(etime - itime) + " seconds");

    return 0;
}

/**
* Spatial Filtering : Gaussian blurand down sample
* Temporal Filtering : Ideal bandpass
*
* Copyright(c) 2021 Tecnologico de Costa Rica.
*
* Authors: Eduardo Moya Bello, Ki - Sung Lim
* Date : April 2021
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
int amplify_spatial_Gdown_temporal_ideal(JNIEnv *env, string inFile, string outDir, double alpha,
                                         int level, double fl, double fh, int samplingRate,
                                         double chromAttenuation) {
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
    string outName = outDir + name + "-ideal-from-" + to_string(fl) + "-to-" +
                     to_string(fh) + "-alpha-" + to_string(alpha) + "-level-" + to_string(level) +
                     "-chromAtn-" + to_string(chromAttenuation) + ".avi";

    logDebug("Video reception - Output file", outName);

    // Read video
    // Create a VideoCapture object and open the input file
    // If the input is the web camera, pass 0 instead of the video file name
    VideoCapture video(inFile);

    // Check if camera opened successfully
    if (!video.isOpened()) {
        logDebugAndShowUser(env, "Video reception", "Error opening video stream or file");
        updateProgress(env, 100);
        return -1;
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

    updateProgress(env, 5);

    /*
     * ================= SPATIAL FILTERING =================
     * */

    vector<Mat> Gdown_stack = build_GDown_stack(env, inFile, startIndex, endIndex, level);
    updateProgress(env, 15);
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

#pragma omp parallel for shared(color_amp, filtered_stack)
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

    int progress = 0;

#pragma omp parallel for shared(video, Gdown_stack, filtered_stack)
    for (int i = startIndex; i < endIndex; i++) {

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

        threshold(frame, out_frame, 0.0f, 0.0f, THRESH_TOZERO);
        threshold(out_frame, frame, 1.0f, 1.0f, THRESH_TRUNC);

        rgbframe = im2uint8(frame);

        cvtColor(rgbframe, out_frame, COLOR_RGB2BGR);

        filtered_stack[i] = out_frame.clone();
    }

    updateProgress(env, 90);

    for (int i = startIndex; i < endIndex; i++) {
        // Write the frame into the file 'outcpp.avi'
        videoOut.write(filtered_stack[i]);
    }

    // When everything done, release the video capture and write object
    video.release();
    videoOut.release();

    etime = omp_get_wtime();

    updateProgress(env, 100);
    logDebugAndShowUser(env, "Video output", "Finished processing in " +
                                             to_string(etime - itime) + " seconds");

    return 0;
}