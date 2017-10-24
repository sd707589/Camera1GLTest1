package opencv4unity.camera1gltest1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by cao on 2017/8/18.
 */

public class MyNDKOpencv {
    private Bitmap resultImg;
    private int dispWidth,disHeight,disDensity;
    MyNDKOpencv(){
    }
    public Bitmap scanfEffect(byte[] data, int srcWidth, int srcHeight){
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,srcWidth,srcHeight,null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, srcWidth, srcHeight), 100, baos);// 80--JPG图片的质量[0-100],100最高
        byte[] rawImage = baos.toByteArray();
        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        int tmp_width,tmp_height;
        tmp_width=bitmap.getWidth();
        tmp_height=bitmap.getHeight();
        int []pixels=new int[tmp_width*tmp_height];
        bitmap.getPixels(pixels,0,tmp_width,0,0,tmp_width,tmp_height);
        //Dealing
        int [] resultInt=getScannerEffect(pixels,tmp_width,tmp_height);
        resultImg= Bitmap.createBitmap(tmp_width,tmp_height, Bitmap.Config.ARGB_8888);
        resultImg.setPixels(resultInt,0,tmp_width,0,0,tmp_width,tmp_height);
        bitmap.recycle();
        bitmap=null;
        return resultImg;
    }

    static {
        System.loadLibrary("native-lib");
    }
    public static native String stringFromJNI();
    private static native int[] getScannerEffect(int[] pixels, int w, int h);
}
