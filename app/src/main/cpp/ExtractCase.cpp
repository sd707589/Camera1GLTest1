//
// Created by cao on 2017/10/23.
//

#include "ExtractCase.h"
extern "C"{
ExtractCase::ExtractCase(){}

ExtractCase::ExtractCase(Mat src)
{
    frame = src.clone();
    recCutImg = Mat(700, 300, frame.type());
    selectColor(frame);
    getLine();
    calculatePoint();
    whetherExtractSection = ExtractSection();
}


ExtractCase::~ExtractCase()
{
    frame.release();
    frame_gray.release();
}

void ExtractCase::selectColor(Mat& src)
{
    int mH[2]={35, 77};
    int mS[2]={43, 255};
    int mV[2]={0, 255};
    Mat frameHSV;
    std::vector<Mat> Channels;
    cvtColor(src, frameHSV, CV_RGB2HSV);
    split(frameHSV, Channels);
    equalizeHist(Channels[2], Channels[2]);
    merge(Channels, frameHSV);
    inRange(frameHSV, Scalar(mH[0], mS[0], mV[0]), Scalar(mH[1], mS[1], mV[1]), frame_gray);
}


double ExtractCase::lineLength(const Vec4i& line)
{
    double xLen = (line[0] - line[2]) ^ 2;
    double yLen = (line[1] - line[3]) ^ 2;
    return xLen + yLen;
}


bool ExtractCase::sortLength(const Vec4i & line1, const Vec4i & line2)
{
    return lineLength(line1) > lineLength(line2);
}


void ExtractCase::getLine()
{
    int lineSize = 50;
    //获取线段集
    std::vector<Vec4i> lines;
    HoughLinesP(frame_gray, lines, 1, CV_PI / 180, lineSize, 70, 100);
    //按长度排序
    //std::sort(lines.begin(), lines.end(), &ExtractCase::sortLength);
    //线段赛选
    for (size_t i = 0; i < lines.size(); i++)
    {
        Vec4i tempL = lines[i];
        myLine tempLINE = myLine(tempL);
        if (((tempL[1] + tempL[3])<frame_gray.rows) &&
            abs(tempLINE.angle)<30)
        {
            topLine.push_back(tempLINE);
        }
        if (abs(tempLINE.angle)>50)
        {
            verticalLine.push_back(tempLINE);
        }
    }
}


void ExtractCase::calculatePoint()
{
    Mat pointCircle = Mat(frame_gray.size().height, frame_gray.size().width, frame_gray.type(), Scalar(0));

    if (topLine.size()>0 || verticalLine.size()>0)
    {
        for (size_t i = 0; i < topLine.size(); i++)
        {
            Point tempPoint;
            for (size_t j = 0; j < verticalLine.size(); j++)
            {
                tempPoint = CrossPoint(topLine[i].line,verticalLine[j].line);
                if (tempPoint.x>0 && tempPoint.x< frame_gray.size().width
                    && tempPoint.y>0 && tempPoint.y< frame_gray.size().height)
                {
                    if (tempPoint != Point(0, 0)
                        && pointCircle.at<uchar>(tempPoint.y, tempPoint.x) != 255)
                    {
                        circle(pointCircle, tempPoint, 100, Scalar(255), CV_FILLED);
                        mCrossPoint.push_back(tempPoint);
                        Point marginPoint = verticalLine[j].belowMarginPoint(frame_gray.size());
                        mCrossPoint.push_back(marginPoint);
                        //circle(frame, tempPoint, 5, Scalar(0, 0, 255), 2);
                        //circle(frame, marginPoint, 5, Scalar(0, 0, 255), 2);
                    }
                }
            }
        }
    }
}


Point ExtractCase::CrossPoint(const Vec4i & line1, const Vec4i & line2)
{
    Point pt;
    // line1's cpmponent
    double X1 = line1[2] - line1[0];//b1
    double Y1 = line1[3] - line1[1];//a1
    // line2's cpmponent
    double X2 = line2[2] - line2[0];//b2
    double Y2 = line2[3] - line2[1];//a2
    // distance of 1,2
    double X21 = line2[0] - line1[0];
    double Y21 = line2[1] - line1[1];
    // determinant
    double D = Y1*X2 - Y2*X1;// a1b2-a2b1
    //
    if (D == 0)
        return Point(0, 0);
    // cross point
    pt.x = (X1*X2*Y21 + Y1*X2*line1[0] - Y2*X1*line2[0]) / D;
    // on screen y is down increased !
    pt.y = -(Y1*Y2*X21 + X1*Y2*line1[1] - X2*Y1*line2[1]) / D;
    return pt;
}


bool ExtractCase::ExtractSection()
{

    //截取区域
    if (mCrossPoint.size()>3)
    {

        Point2f srcTri[4], dstTri[4];

        /// 设置源图像和目标图像上的三组点以计算仿射变换
        if (mCrossPoint[0].x < mCrossPoint[2].x)
        {
            upLeftPt = Point(mCrossPoint[0].x, mCrossPoint[0].y);
            upRightPt = Point(mCrossPoint[2].x, mCrossPoint[2].y);
            srcTri[0] = mCrossPoint[1];
            srcTri[1] = mCrossPoint[0];
            srcTri[2] = mCrossPoint[2];
            srcTri[3] = mCrossPoint[3];
        }
        else{
            upLeftPt = Point(mCrossPoint[2].x, mCrossPoint[2].y);
            upRightPt = Point(mCrossPoint[0].x, mCrossPoint[0].y);
            srcTri[0] = mCrossPoint[3];
            srcTri[1] = mCrossPoint[2];
            srcTri[2] = mCrossPoint[0];
            srcTri[3] = mCrossPoint[1];
        }

        dstTri[0] = Point2f(0, recCutImg.rows - 1);//left bottom
        dstTri[1] = Point2f(0, 0);//left top
        dstTri[2] = Point2f(recCutImg.cols - 1, 0);//right top
        dstTri[3] = Point2f(recCutImg.cols - 1, recCutImg.rows - 1);//right bottom
        /// 求得仿射变换
        //Mat warp_mat = getAffineTransform(srcTri, dstTri);
        Mat warp_mat = getPerspectiveTransform(srcTri, dstTri);

        /// 对源图像应用上面求得的仿射变换
        //warpAffine(frame, recCutImg, warp_mat, recCutImg.size());
        warpPerspective(frame, recCutImg, warp_mat, recCutImg.size());
        drawPolylines(srcTri,4);
        return true;
    }
    else{
        return false;
    }
}


// 在原图上画菱形
void ExtractCase::drawPolylines(Point2f pts[],int size)
{
    //只画三条边
    for (size_t i = 0; i < size-1; i++)
    {
        line(frame, pts[i], pts[i + 1], Scalar(0, 0, 255,255), 4);
    }
}
};