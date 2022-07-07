package com.example.mediacodecencode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.mediacodecencode.data.MediaConstant;
import com.example.mediacodecencode.ipc.MyCClient;
import com.yunxi.ipc.bean.EventData;
import com.yunxi.ipc.bean.EventMsg;

public class MainActivity extends Activity  implements SurfaceHolder.Callback,PreviewCallback{
    private final String TAG = "ENCODE_MainActivity";
	private SurfaceView surfaceview;
	
    private SurfaceHolder surfaceHolder;
	
	private Camera camera;
	
    private Parameters parameters;
    
    int width = 1280;
    
    int height = 720;
    
    int framerate = 30;
    
    int biterate = 8500*1000;
    
//    private static int yuvqueuesize = 10;
    private boolean isRemote = true;
    
//	public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
	
	private AvcEncoder avcCodec;
    private final static int CAMERA_OK = 10001;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private Context context;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        surfaceview = findViewById(R.id.surfaceview);
        SupportAvcCodec();
        if (Build.VERSION.SDK_INT>22) {
            if (!checkPermissionAllGranted(PERMISSIONS_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        PERMISSIONS_STORAGE, CAMERA_OK);
            }else{
                init();
            }
        }else{
            init();
        }

	}

	private void init(){
        context = this.getBaseContext();
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);

        MyCClient.getInstance(this).init(pushCall);
    }


    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode) {
            case CAMERA_OK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //这里已经获取到了摄像头的权限，想干嘛干嘛了可以
                    init();
                } else {
                    showWaringDialog();
                }
                break;
            default:
                break;
        }
    }

    private void showWaringDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setMessage("请前往设置->应用->PermissionDemo->权限中打开相关权限，否则功能无法正常运行！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                        finish();
                    }
                }).show();
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

		
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera = getBackCamera();
        startcamera(camera);
        Log.d(TAG,"startcamera");
        if(isRemote){
            Bundle bundle = new Bundle();
            bundle.putInt(MediaConstant.MSG.WIDTH,this.width);
            bundle.putInt(MediaConstant.MSG.HEIGHT,this.height);
            bundle.putInt(MediaConstant.MSG.FRAMERATE,this.framerate);
            bundle.putInt(MediaConstant.MSG.BITRATE,this.biterate);
            MyCClient.getInstance(context).callCommand(MediaConstant.MSG.START_ENCODE,bundle);
        } else {
            avcCodec = new AvcEncoder(this.width,this.height,framerate,biterate);
            avcCodec.StartEncoderThread();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"stopPreview");
        if (null != camera) {
        	camera.setPreviewCallback(null);
        	camera.stopPreview();
            camera.release();
            camera = null;

            if(isRemote){
                MyCClient.getInstance(context).callCommand(MediaConstant.MSG.STOP_ENCODE,null);
            } else {
                avcCodec.StopThread();
            }
        }
    }


	@Override
	public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
		// TODO Auto-generated method stub
//        if(isRemote){
//            Bundle bundle = new Bundle();
////            bundle.putByteArray(MediaConstant.MSG.FRAME_DATA,data);
//            bundle.putInt(MediaConstant.MSG.FRAME_LENGTH,data.length);
//            EventData eventData = new EventData(MediaConstant.MSG.ON_FRAME);
//            eventData.setData(bundle);
//            eventData.setBigData(data);
//            MyCClient.getInstance(context).callCommand(eventData);
//        } else {
            putYUVData(data,data.length);
//        }
	}
	
	public void putYUVData(byte[] buffer, int length) {
		if (MediaConstant.YUVQueue.size() >= 10) {
            MediaConstant.YUVQueue.poll();
		}
        MediaConstant.YUVQueue.add(buffer);
	}
	
	@SuppressLint("NewApi")
	private boolean SupportAvcCodec(){
		if(Build.VERSION.SDK_INT>=18){
			for(int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--){
				MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
	
				String[] types = codecInfo.getSupportedTypes();
				for (int i = 0; i < types.length; i++) {
				    Log.d(TAG,"encode types:"+types[i]);
					if (types[i].equalsIgnoreCase("video/avc")) {
						return true;
					}
				}
			}
		}
		return false;
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


    private  Intent intent;
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.test1:
                intent = new Intent();
                intent.setClassName(this,"com.example.mediacodecencode.MediaEncodeService");
                intent.setAction("com.wt.stream.media");
//            startService(intent);
                startForegroundService(intent);
                break;
            case R.id.test2:
                Log.d(TAG,"start encode");
                Bundle bundle = new Bundle();
                bundle.putInt(MediaConstant.MSG.WIDTH,this.width);
                bundle.putInt(MediaConstant.MSG.HEIGHT,this.height);
                bundle.putInt(MediaConstant.MSG.FRAMERATE,this.framerate);
                bundle.putInt(MediaConstant.MSG.BITRATE,this.biterate);
                MyCClient.getInstance(context).callCommand(MediaConstant.MSG.START_ENCODE,bundle);

                break;
            case R.id.test3:
                Log.d(TAG,"stop encode");
                MyCClient.getInstance(context).callCommand(MediaConstant.MSG.STOP_ENCODE,null);
                break;
            default:break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }

    public final MyCClient.OnPushCall pushCall = new MyCClient.OnPushCall() {
        @Override
        public void pushMsg(String topic, EventMsg eventMsg) {
            Log.d(TAG, "topic:" + topic);
            if (MediaConstant.MSG.TOPIC.equals(topic)) {
                switch (eventMsg.getMsgId()) {
//                    case MediaConstant.MSG.ON_PREPARE_ENCODE:
//                        break;
                    default:
                        break;
                }

            }

        }
    };
}
