//
// Created by cao on 2017/10/20.
//
#pragma once
#ifndef CAMERA1GLTEST1_IMGPROCESS_H
#define CAMERA1GLTEST1_IMGPROCESS_H

#include <cv.h>
#include <opencv2/opencv.hpp>

extern "C"{
using namespace cv;
void deal(Mat &input, Point center);
};


#endif //CAMERA1GLTEST1_IMGPROCESS_H
