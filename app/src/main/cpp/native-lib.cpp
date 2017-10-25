#pragma once

#include <jni.h>
#include <string>
#include <cv.h>
#include <opencv2/opencv.hpp>
#include "imgProcess.h"
#include "ExtractCase.h"

extern "C"
JNIEXPORT jobject JNICALL
Java_opencv4unity_camera1gltest1_MyNDKOpencv_structFromNative(JNIEnv *env, jclass type) {

    // 读取java类结构
    jclass cSructInfo=env->FindClass("opencv4unity/camera1gltest1/ResultFromJni");
    jfieldID text=env->GetFieldID(cSructInfo,"text","Ljava/lang/String;");
    jfieldID number=env->GetFieldID(cSructInfo,"num","I");

    //新建Jni类对象
    jobject oStructInfo=env->AllocObject(cSructInfo);
    //给Jni类对象赋值，并放回该对象
    env->SetIntField(oStructInfo,number,888);
    std::string fuc = "fuck the Jni: ";
    jstring jstrn=env->NewStringUTF(fuc.c_str());
    env->SetObjectField(oStructInfo,text,jstrn);
    return oStructInfo;

}

extern "C" {


void deal(cv::Mat &input, cv::Point center);

JNIEXPORT jobject JNICALL
Java_opencv4unity_camera1gltest1_MyNDKOpencv_getScannerEffect(JNIEnv *env, jclass type,
                                                              jintArray pixels_, jint w, jint h, jint model) {
    // 读取java类结构
    jclass cSructInfo=env->FindClass("opencv4unity/camera1gltest1/ResultFromJni2");
    jfieldID cXLoc=env->GetFieldID(cSructInfo,"x","I");
    jfieldID cYLoc=env->GetFieldID(cSructInfo,"y","I");
    jfieldID cResultInt=env->GetFieldID(cSructInfo,"resultInt","[I");
    //新建Jni类对象
    jobject oStructInfo=env->AllocObject(cSructInfo);


    jint *pixels = env->GetIntArrayElements(pixels_, false);
    if(pixels==NULL){
        return NULL;
    }
    cv::Mat imgData(h, w, CV_8UC4, pixels);

    //imgData是获取的Camera图像，h/ w分别是height/ width，接下来用openCV做图像处理

    ExtractCase mExtractImg= ExtractCase(imgData);
    switch (model){
        case 0:
            mExtractImg.frame.copyTo(imgData);
            break;
        case 1:
            mExtractImg.frame_gray.copyTo(imgData);
            break;
        case 2:
            deal(imgData, cv::Point(imgData.size().width / 2, imgData.size().height / 2));
            break;
        default:
            break;
    }
    //把左上角点坐标赋给结果对象
    Point leftPoint=mExtractImg.upLeftPt;
    int x=leftPoint.x;
    int y=leftPoint.y;
    env->SetIntField(oStructInfo,cXLoc,x);
    env->SetIntField(oStructInfo,cYLoc,y);

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, pixels);
    env->SetObjectField(oStructInfo,cResultInt,result);

    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return oStructInfo;
}


}

