#include <vector>
#include <omp.h>
#include "vector_functions.h"

using namespace std;

vector<double> linspace(int startIndex, int endIndex, int N) {
    std::vector<double> linspaced;

    double start = static_cast<double>(startIndex);
    double end = static_cast<double>(endIndex);
    double num = static_cast<double>(N);

    if (num == 0) { return linspaced; }
    if (num == 1)
    {
        linspaced.push_back(start);
        return linspaced;
    }

    double delta = (end - start) / (num - 1);

    for(int i=0; i < num-1; ++i)
    {
        linspaced.push_back(start + delta * i);
    }
    linspaced.push_back(end); // I want to ensure that start and end
    // are exactly the same as the input
    return linspaced;
}

