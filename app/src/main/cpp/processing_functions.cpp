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

        //imshow("Original", frame);

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
* Spatial Filtering : Laplacian pyramid
* Temporal Filtering : substraction of two IIR lowpass filters
*
* y1[n] = r1*x[n] + (1-r1)*y1[n-1]
* y2[n] = r2*x[n] + (1-r2)*y2[n-1]
* (r1 > r2)
*
* y[n] = y1[n] - y2[n]
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
int amplify_spatial_lpyr_temporal_iir(JNIEnv *env, string inFile, string outDir, double alpha,
                                      double lambda_c, double r1, double r2,
                                      double chromAttenuation) {

    double itime, etime;
    itime = omp_get_wtime();

    /*
     * ================= VIDEO RECEPTION =================
     * */

    logDebugAndShowUser(env, "Video reception",
                        "Extracting video information");

    string name;
    string delimiter = "/";

    size_t last = 0; size_t next = 0;
    while ((next = inFile.find(delimiter, last)) != string::npos) {
        last = next + 1;
    }

    name = inFile.substr(last);
    name = name.substr(0, name.find("."));

    // Creates the result video name
    string outName = outDir + name + "-iir-r1-" + to_string(r1) + "-r2-" +
                     to_string(r2) + "-alpha-" + to_string(alpha) + "-lambda_c-" + to_string(lambda_c) +
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
    int startIndex = 1;
    int endIndex = len - 10;
    int vidHeight = (int)video.get(CAP_PROP_FRAME_HEIGHT);
    int vidWidth = (int)video.get(CAP_PROP_FRAME_WIDTH);
    int fr = (int)video.get(CAP_PROP_FPS);

    // Define the codec and create VideoWriter object
    VideoWriter videoOut(outName, VideoWriter::fourcc('M', 'J', 'P', 'G'), fr,
                         Size(vidWidth, vidHeight));

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
     * ================= FIRST FRAME PROCESSING =================
     * */

    // Variables to be used
    Mat frame, rgbframe, rgbframebackup, ntscframe, output,
        lowpass1, lowpass2, pyr, filtered, tmp1_1, tmp1_2, tmp2_1, tmp2_2;
    vector<Mat> pyrVector, filteredVector;

    video >> frame;

    // If the frame is empty, throw an error
    if (frame.empty())
        return -1;

    // Color conversion GBR 2 NTSC
    cvtColor(frame, rgbframe, COLOR_BGR2RGB);
    rgbframe = im2double(rgbframe);
    ntscframe = rgb2ntsc(rgbframe);

    pyrVector = buildLpyrfromGauss(ntscframe, max_ht);
    pyr = pyrVectorToMat(pyrVector);
    lowpass1 = pyr;
    lowpass2 = pyr;

    output = im2uint8(rgbframe);
    cvtColor(output, output, COLOR_RGB2BGR);
    videoOut.write(output);

    /*
     * ================= REST OF THE FRAMES PROCESSING =================
     * */

    // Temporal filtering variables
    static double delta = (double)(lambda_c / 8) / (double) (1 + alpha);
    static double exaggeration_factor = 2;
    static double lambda = (double) sqrt(vidHeight * vidHeight + vidWidth * vidWidth) / (double) 3;
    static double coefficient1 = (1 - r1);
    static double coefficient2 = (1 - r2);

    int progress;
    int nLevels = (int)pyrVector.size();

    for (int i = startIndex; i < endIndex; i++) {

        progress = 95 * (i + 1) / endIndex;
        updateProgress(env, 5 + progress);

        logDebugAndShowUser(env, "Processing video", "Frame " +
                                                     to_string(i + 1) + " of " +
                                                     to_string(endIndex));

        // Capture frame-by-frame
        video >> frame;

        // If the frame is empty, break immediately
        if (frame.empty())
            break;

        // Color conversion GBR 2 NTSC
        cvtColor(frame, rgbframe, COLOR_BGR2RGB);
        rgbframe = im2double(rgbframe);
        ntscframe = rgb2ntsc(rgbframe);

        // Compute the laplacian pyramid
        pyrVector = buildLpyrfromGauss(ntscframe, max_ht);
        pyr = pyrVectorToMat(pyrVector);

        multiply(Scalar(coefficient1), lowpass1, tmp1_1);
        multiply(Scalar(r1), pyr, tmp1_2);
        add(tmp1_1, tmp1_2, lowpass1);

        multiply(Scalar(coefficient2), lowpass2, tmp2_1);
        multiply(Scalar(r2), pyr, tmp2_2);
        add(tmp2_1, tmp2_2, lowpass2);

        subtract(lowpass1, lowpass2, filtered, noArray(), CV_64FC3);

        vector<Vec3d> ntscframe_vector;
        ntscframe.reshape(0, (int)ntscframe.total()).copyTo(ntscframe_vector);

        vector<Vec3d> tmp1_1_vector;
        tmp1_1.copyTo(tmp1_1_vector);

        vector<Vec3d> tmp1_2_vector;
        tmp1_2.copyTo(tmp1_2_vector);

        vector<Vec3d> tmp2_1_vector;
        tmp2_1.copyTo(tmp2_1_vector);

        vector<Vec3d> tmp2_2_vector;
        tmp2_2.copyTo(tmp2_2_vector);

        vector<Vec3d> lowpass1_vector;
        lowpass1.copyTo(lowpass1_vector);

        vector<Vec3d> lowpass2_vector;
        lowpass2.copyTo(lowpass2_vector);

        vector<Vec3d> pyr_vector;
        pyr.copyTo(pyr_vector);

        vector<Vec3d> filtered_vector;
        filtered.copyTo(filtered_vector);

        filteredVector = pyrMatToVector(filtered, pyrVector);

        // Go one level down on pyramid each stage
//#pragma omp parallel for shared(lowpass1, lowpass2, pyr, filtered)
        for (int level = nLevels - 1; level >= 0; level--) {
            // Compute modified alpha for this level
            double currAlpha = lambda / delta / 8.0 - 1.0;
            currAlpha = currAlpha * exaggeration_factor;
            if (level == max_ht - 1 || level == 0) { // ignore the highest and lowest frecuency band
                Size mat_sz(filteredVector[level].cols, filteredVector[level].rows);
                filteredVector[level] = Mat::zeros(mat_sz, CV_64FC3);
            } else if (currAlpha > alpha) { // representative lambda exceeds lambda_c
                filteredVector[level] = alpha * filteredVector[level];
            } else {
                filteredVector[level] = currAlpha * filteredVector[level];
            }

            lambda = lambda / (double) 2;
        }


        // Render on the input video
        output = reconLpyr(filteredVector);
//        std::vector<Vec3d> output_vector;
//        Mat output_reshaped;
//        output_reshaped = output.reshape(0, 1);
//        output_reshaped.copyTo(output_vector);
//        Vec3d at_696285 = output_vector[696285];

//        vector<Mat> channels(3);
//        split(output, channels);
//        multiply(channels[1], chromAttenuation, channels[1]);
//        multiply(channels[2], chromAttenuation, channels[2]);
//        merge(channels, output);

        // Scalar vector for color attenuation in YIQ (NTSC) color space
        Scalar color_amp(0, chromAttenuation, chromAttenuation);
        multiply(output, color_amp, output);

        output = ntscframe + output;
        //add(ntscframe, output, output, noArray(), DataType<double>::type);

        rgbframe = ntsc2rgb(output);

        threshold(rgbframe, rgbframe, 0.0f, 0.0f, THRESH_TOZERO);
        threshold(rgbframe, rgbframe, 1.0f, 1.0f, THRESH_TRUNC);

//        logDebug("Validating matrix changes - (0 0)",
//                 to_string(rgbframebackup.at<double>(0, 0)));
//        logDebug("Validating matrix changes - (0 1)",
//                 to_string(rgbframebackup.at<double>(0, 1)));
//        logDebug("Validating matrix changes - (0 2)",
//                 to_string(rgbframebackup.at<double>(0, 2)));
//        logDebug("Validating matrix changes - (0 3)",
//                 to_string(rgbframebackup.at<double>(0, 3)));
//        logDebug("Validating matrix changes - (0 4)",
//                 to_string(rgbframebackup.at<double>(0, 4)));
//        logDebug("Validating matrix changes - (0 5)",
//                 to_string(rgbframebackup.at<double>(0, 5)));
//        logDebug("Validating matrix changes - (0 6)",
//                 to_string(rgbframebackup.at<double>(0, 6)));
//        logDebug("Validating matrix changes - (0 7)",
//                 to_string(rgbframebackup.at<double>(0, 7)));


        output = im2uint8(rgbframe);

        cvtColor(output, output, COLOR_RGB2BGR);

        videoOut.write(output);

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
