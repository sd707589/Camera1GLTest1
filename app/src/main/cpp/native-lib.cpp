#pragma once

#include <jni.h>
#include <string>
#include <cv.h>
#include <opencv2/opencv.hpp>
#include "imgProcess.h"
#include "ExtractCase.h"
extern "C"{



void deal(cv::Mat &input, cv::Point center);

JNIEXPORT jstring JNICALL
Java_opencv4unity_camera1gltest1_MyNDKOpencv_stringFromJNI(JNIEnv *env, jobject instance) {

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jintArray JNICALL
Java_opencv4unity_camera1gltest1_MyNDKOpencv_getScannerEffect(JNIEnv *env, jclass type,
                                                              jintArray pixels_, jint w, jint h) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    if(pixels==NULL){
        return NULL;
    }
    cv::Mat imgData(h, w, CV_8UC4, pixels);

    //imgData是获取的Camera图像，h/ w分别是height/ width，接下来用openCV做图像处理
    deal(imgData, cv::Point(imgData.size().width / 2, imgData.size().height / 2));
//    ExtractCase mExtractImg= ExtractCase(imgData);
//    mExtractImg.frame_gray.copyTo(imgData);

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, pixels);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return result;
}


}

