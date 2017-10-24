package opencv4unity.camera1gltest1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by chenyan on 2015/10/21.
 */
public class MyGLSurfaceView extends GLSurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    //OpenGLES相关
    private int srcFrameWidth  = 640;// 源视频帧宽/高
    private int srcFrameHeight = 480;
    private int srcDensity=3;
    // 纹理id
    private boolean mbpaly = false;
    private FloatBuffer squareVertices = null;
    private FloatBuffer coordVertices = null;
    private Bitmap grayImg=null;
    private static float squareVertices_[] = {
            -1.0f, -1.0f,0.0f,
            1.0f, -1.0f,0.0f,
            -1.0f,  1.0f,0.0f,
            1.0f,  1.0f,0.0f,
    };
    private static float coordVertices_[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f,  1.0f,
            1.0f,  1.0f,
    };

    //Camera相关
    private int mCameraIndex=Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera camera;
    public Bitmap resultImg;
    private SurfaceHolder surfaceHolder;
    private boolean printCharacters=true;
    private static final String TAG = "MyGLSurfaceView";
    private SurfaceTexture surfaceTexture;

    public MyGLSurfaceView(Context context)
    {
        super(context);
        DisplayMetrics displayMetrics=context.getResources().getDisplayMetrics();//需要在Mainactivity中新增context变量
        srcFrameWidth=displayMetrics.widthPixels;//屏幕为1920
        srcFrameHeight=displayMetrics.heightPixels;//屏幕为1080
        srcDensity=(int)displayMetrics.density;//屏幕为3


        //设置Renderer到GLSurfaceView
        setRenderer(new MyGL20Renderer());
        // 只有在绘制数据改变时才绘制view
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // 顶点坐标
        squareVertices = floatBufferUtil(squareVertices_);
        //纹理坐标
        coordVertices = floatBufferUtil(coordVertices_);

        surfaceTexture=new SurfaceTexture(2);
        surfaceHolder=getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    // 定义一个工具方法，将float[]数组转换为OpenGL ES所需的FloatBuffer
    private FloatBuffer floatBufferUtil(float[] arr)
    {
        FloatBuffer mBuffer;
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        startCamera(mCameraIndex);
        printCharacters=false;
    }
    private void startCamera(int mCameraIndex) {
        // 初始化并打开摄像头
        if (camera == null)
        {

            try
            {
                camera = Camera.open(mCameraIndex);
            }
            catch (Exception e)
            {
                return;
            }
            Camera.Parameters params = camera.getParameters();
            if (printCharacters){
                List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
                for(int i=0;i<previewSizes.size();i++){
                    Log.i(TAG,"SupportedPreviewSizes:"+
                            String.valueOf(previewSizes.get(i).width)+", "+
                            String.valueOf(previewSizes.get(i).height));

                }

            }

            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            {
                // 自动对焦
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            try
            {
                //眼镜的尺寸为1280x720
                srcFrameWidth=640;
                srcFrameHeight=360;

                params.setPreviewFormat(ImageFormat.NV21);
                params.setPreviewSize(srcFrameWidth, srcFrameHeight);
                params.setPreviewFpsRange(15*1000, 30*1000);
                camera.setParameters(params);
            }
            catch (Exception e)
            {
            }

            try
            {
                camera.setPreviewTexture(surfaceTexture);
                camera.startPreview();
                camera.setPreviewCallback(this);
                camera.setDisplayOrientation(90);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                camera.release();
                camera=null;
            }
        }
    }

    private void stopCamera() {
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera=null;
            printCharacters=true;
        }

    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        stopCamera();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        synchronized (this){//开启后台进程，也可通过AsyncTask
            //通过Callback，将前一帧图像通过，byte[] data 形式传递出去
            onSaveFrames(bytes,bytes.length,srcFrameWidth,srcFrameHeight);
        }
    }

    public class MyGL20Renderer implements Renderer
    {
        private int texture;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            mbpaly = false;
            // 关闭抗抖动
            gl.glDisable(GL10.GL_DITHER);
            //设置背景的颜色
            gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
            //启动纹理
            gl.glEnable(GL10.GL_TEXTURE_2D);

            loadTexture(gl);
            mbpaly = true;
        }
        private void loadTexture(GL10 gl)
        {
            try
            {
                // 加载位图
                int[] textures = new int[1];
                // 指定生成N个纹理（第一个参数指定生成一个纹理）
                // textures数组将负责存储所有纹理的代号
                gl.glGenTextures(1, textures, 0);
                // 获取textures纹理数组中的第一个纹理
                texture = textures[0];
                // 通知OpenGL将texture纹理绑定到GL10.GL_TEXTURE_2D目标中
                gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
                // 设置纹理被缩小（距离视点很远时被缩小）时的滤波方式
                gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                        GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
                // 设置纹理被放大（距离视点很近时被方法）时的滤波方式
                gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                        GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
                // 设置在横向、纵向上都是平铺纹理
                gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                        GL10.GL_REPEAT);
                gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                        GL10.GL_REPEAT);
                // 加载位图生成纹理
                if (grayImg!=null)
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, grayImg, 0);
            }
            finally
            {
                // 生成纹理之后，回收位图
                if (grayImg != null)
                    grayImg.recycle();
            }
        }

        public void onDrawFrame(GL10 gl)
        {
            // 清除屏幕缓存和深度缓存
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            // 启用顶点坐标数据
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            // 启用贴图坐标数组数据
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glLoadIdentity();
            gl.glRotatef(90.0f, 0, 0,1);
            gl.glRotatef(180.0f, 0, 1,0);
            //绑定贴图
            if (grayImg != null){
                gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
                GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, grayImg, 0);
                // 设置贴图的坐标数据
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordVertices);  // ②
                // 执行纹理贴图
                gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
            }

            // 设置顶点的位置数据
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareVertices);
            // 按cubeFacetsBuffer指定的面绘制三角形
//            gl.glDrawElements(GL10.GL_TRIANGLES, cubeFacetsBuffer.remaining(),
//                    GL10.GL_UNSIGNED_BYTE, cubeFacetsBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,4);
            // 绘制结束
            gl.glFinish();
            // 禁用顶点、纹理坐标数组
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            if (grayImg != null)
                grayImg.recycle();
        }
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height)
        {
            // 初始化单位矩阵
            gl.glLoadIdentity();
            // 计算透视视窗的宽度、高度比
            float ratio = (float) width / height;
            // 调用此方法设置透视视窗的空间大小。
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }
    }

    public void onSaveFrames(byte[] data, int length, int srcWidth, int srcHeight)//读取图像数据
    {
        if (  length != 0 && mbpaly )
        {
            MyNDKOpencv myNDKOpencv=new MyNDKOpencv();
            //OpenCV Processing
            grayImg = myNDKOpencv.scanfEffect(data,srcFrameWidth,srcFrameHeight);
            requestRender();
        }
    }
}
