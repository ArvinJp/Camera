package com.example.arvin.demo_camera;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public class CameraActivity extends Activity implements SurfaceHolder.Callback,View.OnClickListener{

    private SurfaceView mPreview;
    private ImageView mIv_capture;
    private ImageView mIv_no;
    private ImageView mIv_yes;
    private ImageView mIv_flash;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private int mOrientation;
    private OrientationEventListener mOrientationEventListener;
//    private SimpleDateFormat mSimpleDateFormat;
    private byte[] mData;
    private boolean mIsBestPictureSize=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
//        Log.i("Arvin","mainactivity oncreate");
//        mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        mOrientationEventListener = new OrientationEventListener(this) {

            @Override
            public void onOrientationChanged(int orientation) {
                // TODO Auto-generated method stub
                if (orientation == ORIENTATION_UNKNOWN) return;
                Camera.CameraInfo info =
                        new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {  // back-facing camera
                    rotation = (info.orientation + orientation) % 360;
                }
                mOrientation = rotation;
            }
        };
        initViews();
        initPreview();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
//        Log.i("Arvin","mainactivity onresume");

    }

    private void initCamera() {
        if(Camera.getNumberOfCameras()>0)
        {

            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if(mCamera!=null)
            {
                mOrientationEventListener.enable();
                mParameters = mCamera.getParameters();
                initPictureSize();
                List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
                if(supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                {
                    mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                mCamera.setParameters(mParameters);

            }
        }
    }

    private void initPreview() {
//        Log.i("Arvin","initPreview");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width=metrics.widthPixels;
        int height= (int) (width*(4f/3f));
        ViewGroup.LayoutParams layoutParams = mPreview.getLayoutParams();
        layoutParams.height=height;
        mPreview.setLayoutParams(layoutParams);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

    }

    private void initViews() {
        mPreview = (SurfaceView) findViewById(R.id.preview);
        mIv_capture = (ImageView) findViewById(R.id.capture);
        mIv_no = (ImageView) findViewById(R.id.no);
        mIv_yes = (ImageView) findViewById(R.id.yes);
        mIv_flash = (ImageView) findViewById(R.id.flash);
        mIv_yes.setOnClickListener(this);
        mIv_no.setOnClickListener(this);
        mIv_flash.setOnClickListener(this);
        mIv_capture.setOnClickListener(this);
        mIv_yes.setVisibility(View.INVISIBLE);
        mIv_no.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        Log.i("Arvin","surfaceCreated");


            if(mCamera!=null)
            {
                setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK,mCamera);
                initPreviewSize();
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


    }

    private void initPreviewSize() {
        Camera.Size bestSize = getBestSize(mParameters.getSupportedPreviewSizes());
        mParameters.setPreviewSize(bestSize.width,bestSize.height);
        mCamera.setParameters(mParameters);
    }

    private Camera.Size getBestSize(List<Camera.Size> sizes)
    {
        for(Camera.Size size:sizes)
        {
            if(((float)size.width/(float)size.height)==(4f/3f))
            {
                return size;
            }
        }

        return sizes.get(0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.i("Arvin","surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("Arvin","surfaceDestroyed");
    }

    private void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public String setFlashMode()
    {
        String flashMode = mParameters.getFlashMode();
        if(flashMode!=null)
        {
            if(TextUtils.equals(flashMode, Camera.Parameters.FLASH_MODE_OFF))
            {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            if(TextUtils.equals(flashMode, Camera.Parameters.FLASH_MODE_AUTO))
            {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
            if(TextUtils.equals(flashMode, Camera.Parameters.FLASH_MODE_ON))
            {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            if(TextUtils.equals(flashMode, Camera.Parameters.FLASH_MODE_TORCH))
            {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }

            mCamera.setParameters(mParameters);
            return mParameters.getFlashMode();
        }else
        {
            return null;
        }

    }

    public void setFlashIcon(String mode)
    {
        if(TextUtils.equals(mode, Camera.Parameters.FLASH_MODE_OFF))
        {
            mIv_flash.setImageResource(R.drawable.main_top_flash_off);
        }
        if(TextUtils.equals(mode, Camera.Parameters.FLASH_MODE_AUTO))
        {
            mIv_flash.setImageResource(R.drawable.main_top_flash_auto);
        }
        if(TextUtils.equals(mode, Camera.Parameters.FLASH_MODE_ON))
        {
            mIv_flash.setImageResource(R.drawable.main_top_flash_on);
        }
        if(TextUtils.equals(mode, Camera.Parameters.FLASH_MODE_TORCH))
        {
            mIv_flash.setImageResource(R.drawable.main_top_flash_torch);
        }
        if(mode==null)
        {
            mIv_flash.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
//        Log.i("Arvin","mainactivity onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.i("Arvin","mainactivity onStop");
        if(mCamera!=null)
        {
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOrientationEventListener!=null)
        {
            mOrientationEventListener.disable();
        }
//        Log.i("Arvin","mainactivity onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.flash:
                setFlashIcon(setFlashMode());
                break;
            case R.id.capture:
                doCapture();
                break;
            case R.id.yes:
                Intent intent = new Intent();
                intent.putExtra("isBestPictureSize",mIsBestPictureSize);
                intent.putExtra("data",mData);
                setResult(1,intent);
//                savePicture(mData);
                finish();
                break;
            case R.id.no:
                mData=null;
                mIv_no.setVisibility(View.INVISIBLE);
                mIv_yes.setVisibility(View.INVISIBLE);
                mIv_capture.setVisibility(View.VISIBLE);
                if(mCamera!=null)
                    mCamera.startPreview();
                break;
            default:
                break;
        }
    }

    private void doCapture() {

        mParameters.setRotation(mOrientation);
        mCamera.setParameters(mParameters);
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                mIv_capture.setVisibility(View.INVISIBLE);
            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mIv_no.setVisibility(View.VISIBLE);
                mIv_yes.setVisibility(View.VISIBLE);
                mData=data;
            }
        });

    }

//    private void savePicture(byte[] data) {
//        File root = new File(Environment.getExternalStorageDirectory().toString()+"/Demo_camera");
//        if(!root.exists())
//        {
//            root.mkdirs();
//        }
//        File file = new File(root, mSimpleDateFormat.format(new Date())+".jpg");
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(data);
//            fos.close();
//            mCamera.startPreview();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    private Camera.Size getBestPictureSize() {
        Camera.Size betterSize=null;
        List<Camera.Size> supportedPictureSizes = mParameters.getSupportedPictureSizes();
        for(Camera.Size size:supportedPictureSizes)
        {
            if(size.width==640&&size.height==480)
            {
                mIsBestPictureSize=true;
                return size;
            }else if(((float)size.width/(float)size.height)==((float)640/(float)480))
            {
                betterSize=size;
            }

        }
        if(betterSize!=null)
        {
            mIsBestPictureSize=false;
            return betterSize;
        }
        //照片比例不是4:3暂不作特殊处理
        return supportedPictureSizes.get(0);

    }
    private void initPictureSize()
    {
        Camera.Size bestPictureSize = getBestPictureSize();
        mParameters.setPictureSize(bestPictureSize.width,bestPictureSize.height);
    }



}
