package opencv4unity.camera1gltest1;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout previewLayout;
    private MyGLSurfaceView myGLSurfaceView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        requestPermission();
        previewLayout=(RelativeLayout)findViewById(R.id.previewLayout);
        myGLSurfaceView=new MyGLSurfaceView(this);
        previewLayout.addView(myGLSurfaceView);

        textView = (TextView) findViewById(R.id.sample_text);
//        ResultFromJni resultFromJni =MyNDKOpencv.structFromNative();
//        textView.setText(resultFromJni.text + resultFromJni.num);

    }
//    private Handler mHandler=new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case 0:
//                    String data=(String)msg.obj;
//                    textView.setText("LeftUp Point is "+data);
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    void requestPermission(){
        final int REQUEST_CODE = 1;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
    }

    public void clickText(View view) {
        if (myGLSurfaceView.model ==2)
            myGLSurfaceView.model=0;
        else myGLSurfaceView.model++;
        Toast.makeText(this,"Change Effect",Toast.LENGTH_SHORT).show();
    }
}
