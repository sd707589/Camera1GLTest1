//
// Created by cao on 2017/10/20.
//

#include "imgProcess.h"

extern "C"

void deal(Mat &input, Point center){
    static int cnt=0;
    Mat gray, canny, temp;
    cvtColor(input, gray, CV_BGRA2GRAY);
    Canny(gray, canny, 50, 100);
    temp = canny.clone();
    temp = Scalar(0);
    circle(temp,center,cnt,Scalar::all(255),80);
    if ((cnt+=70) >350)
        cnt=0;
    uchar* ptrCanny = canny.ptr(0);
    uchar* ptrTemp = temp.ptr(0);
    uchar* ptrColor = input.ptr(0);
    int r=rand() % 255;
    int g=rand() % 255;
    int b=rand() % 255;
    for (int i = 0; i < canny.size().area(); i++){
        if (ptrTemp[i] == 0)
        {
            ptrCanny[i] = 0;
        }
        if (ptrCanny[i] == 255)
        {
            ptrColor[4 * i] = r;
            ptrColor[4 * i + 1] = g;
            ptrColor[4 * i + 2] = b;
            ptrColor[4 * i + 3] =255;
        }
    }
    circle(input,Point(0,0),5,Scalar(r,g,b),-1);
    circle(input,Point(100,0),10,Scalar(r,g,b),-1);
    circle(input,Point(0,300),50,Scalar(r,g,b),-1);
    gray.release();
    canny.release();
    temp.release();
    return;
}