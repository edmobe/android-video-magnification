// PeakFinder.h
#ifndef PEAKFINDER_H
#define PEAKFINDER_H

#include <vector>
#include <iostream>
#include <unordered_map>

using namespace std;
typedef long double ld;

unordered_map<string, vector<ld>> z_score_thresholding(vector<ld> input,
                                                       int lag, ld threshold, ld influence);

namespace PeakFinder {
    const float EPS = 2.2204e-16f;

    /*
        Inputs
        x0: input signal
        extrema: 1 if maxima are desired, -1 if minima are desired
        includeEndpoints - If true the endpoints will be included as possible extrema otherwise they will not be included
        Output
        peakInds: Indices of peaks in x0
    */
    void findPeaks(std::vector<float> x0, std::vector<int>& peakInds, bool includeEndpoints=true, float extrema=1);
}

#endif
