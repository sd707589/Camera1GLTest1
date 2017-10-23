//
// Created by cao on 2017/10/23.
//

#ifndef CAMERA1GLTEST1_EXTRACTCASE_H
#define CAMERA1GLTEST1_EXTRACTCASE_H
#pragma once
//#include <stdio.h>
#include <cv.h>
#include <opencv2/opencv.hpp>
extern "C"{
#define PI 3.1415926
using namespace cv;
class myLine
{
public:
    Vec4i line;
    float angle;
    myLine(){}
    myLine(Vec4i srcline){
        line = srcline;
        angle = lineAngle(srcline);
    }
    Point belowMarginPoint(Size size){
        int y = size.height;
        if (k == 10000.0)
        {
            return Point(line[0], y);
        }
        else{
            int x = (y - b) / k;
            return Point(x, y);
        }
    }
private:
    float k, b;
    float lineAngle(const Vec4i &line){
        float angle_temp;
        float xx, yy;

        xx = line[0] - line[2];
        yy = line[1] - line[3];
        if (xx == 0.0)
        {
            angle_temp = PI / 2.0;
            k = 10000.0;
            b = 0;
        }
        else{
            k = yy / xx;
            angle_temp = atan(k);
            b = line[1] - k*line[0];
        }

        //if (angle_temp<0)
        //{
        //	angle_temp = PI + angle_temp;
        //}

        return angle_temp / PI * 180;
    }
};

class ExtractCase
{
public:
    Mat frame,frame_gray;
    //是否提取成功
    bool whetherExtractSection = false;
    //所提取的图像
    Mat recCutImg;
    //左上角点坐标
    Point upLeftPt, upRightPt;

    ExtractCase();
    ExtractCase(Mat );
    ~ExtractCase();
private:
//    Mat frame_gray;

    void selectColor(Mat& src);

    double lineLength(const Vec4i& line);
    bool sortLength(const Vec4i & line1, const Vec4i & line2);

    std::vector<myLine> topLine, verticalLine;
    void getLine();

    std::vector<Point> mCrossPoint;
    void calculatePoint();
    Point CrossPoint(const Vec4i & line1, const Vec4i & line2);


    bool ExtractSection();
    // 在原图上画菱形
    void drawPolylines(Point2f pts[], int size);
};}



#endif //CAMERA1GLTEST1_EXTRACTCASE_H
