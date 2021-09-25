#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <string>
#include <sstream>

#include "im_conv.h"


Mat im2double(Mat A) {
    Mat B;

    // We need to convert this to a new image of type CV_64FC3
    // CV_64FC3: 3 channels each on 64bit float [-inf, +inf]
    // we need to do this because YIQ result will be in [-1.0, 1.0] (I & Q)
    // so this obviously cannot be stored in CV_8UC3
    // At the same time, we will also divide by 255.0 to put values in [0.0, 1.0]

    // CV_8UC3: 3 channels, each on unsigned char, so [0,255]
    // here is a dummy one, black by default, 256x256
    // Get the data type of the input image
    if (A.type() == CV_8UC3) {
        A.convertTo(B, CV_64FC3, 1.0 / 255.0);  // Divide all values by the largest possible value in the datatype
    }

    return B;
}

Mat im2uint8(Mat A) {
    Mat B;

    A.convertTo(B, CV_8UC3, 255);

    return B;
}

Mat rgb2ntsc(Mat A) {
    Mat B;

    // let's define the matrix for RGB -> YIQ conversion
    Matx33d matYIQ(0.299f, 0.587f, 0.114f,
                   0.596f, -0.274f, -0.322f,
                   0.211f, -0.523f, 0.312f);

    // at this point A contains pixels made of 3 RGB float components in [0-1]
    // so let's convert to YIQ
    // (cv::transform will apply the matrix to each 3 component pixel of A)
    transform(A, B, matYIQ);

    return B;
}

Mat ntsc2rgb(Mat A) {
    Mat B;

    // let's define the matrix for YIQ -> RGB conversion
    Matx33d matRGB(1.000f, 0.956f, 0.621f,
                   1.000f, -0.272f, -0.647f,
                   1.000f, -1.106f, 1.703f);

    transform(A, B, matRGB);

    return B;
}