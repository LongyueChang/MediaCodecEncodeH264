package com.yunxi.windowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yunxi.windowview.data.MediaConstant;
import com.yunxi.windowview.encode.AvcEncoder;

import java.io.IOException;

/**
 * @author : longyue
 * @data : 2022/7/7
 * @email : changyl@yunxi.tv
 */
public class CameraProxy implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final String TAG = CameraProxy.class.getSimpleName();
    private SurfaceView surfaceview;

    private SurfaceHolder surfaceHolder;

    private Camera camera;

    private Camera.Parameters parameters;

    int width = 1280;

    int height = 720;

    int framerate = 30;

    int biterate = 8500*1000;
    private AvcEncoder avcCodec;



    public void init(Context context,SurfaceView surfaceview){
        this.surfaceview=surfaceview;
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        putYUVData(bytes,bytes.length);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera = getBackCamera();
        startcamera(camera);
        Log.d(TAG,"startcamera");

        avcCodec = new AvcEncoder(this.width,this.height,framerate,biterate);
        avcCodec.StartEncoderThread();

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.d(TAG,"stopPreview");
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;

            avcCodec.StopThread();
        }
    }


    @TargetApi(9)
    private Camera getBackCamera() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private void startcamera(Camera mCamera){
        if(mCamera != null){
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);
                if(parameters == null){
                    parameters = mCamera.getParameters();
                }
                parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewSize(width, height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void putYUVData(byte[] buffer, int length) {
        if (MediaConstant.YUVQueue.size() >= 10) {
            MediaConstant.YUVQueue.poll();
        }
        MediaConstant.YUVQueue.add(buffer);
    }
}
