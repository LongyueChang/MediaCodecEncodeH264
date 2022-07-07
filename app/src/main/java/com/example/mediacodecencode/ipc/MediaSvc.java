package com.example.mediacodecencode.ipc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.yunxi.windowview.encode.AvcEncoder;
import com.yunxi.windowview.data.MediaConstant;
import com.yunxi.ipc.IpcSev;
import com.yunxi.ipc.PushSevManager;
import com.yunxi.ipc.bean.EventData;
import com.yunxi.ipc.utils.LogUtils;
import com.yunxi.windowview.VideoUtils;

import java.util.concurrent.ArrayBlockingQueue;


/**
 * @author longyue
 */
public class MediaSvc extends IpcSev {
    private final Context mContext;
    private String TAG = MediaSvc.class.getSimpleName();
    private String HOST = "1000";
    private static final Object object = new Object();
    private static MediaSvc instance;
    private Handler mH;
    private AvcEncoder avcCodec;
    private final static int yuvqueuesize = 10;
    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);

    public static MediaSvc getInstance(Context context) {
        if (instance == null) {
            synchronized (object) {
                if (instance == null) {
                    instance = new MediaSvc(context, 1);
                }
            }
        }
        return instance;
    }

    public MediaSvc(Context context, int version) {
        super(context, version);
        LogUtils.setEnv(LogUtils.INFO);
        mContext = context;

    }


    @Override
    public void init() {
        super.init();
        mH = new Handler(Looper.getMainLooper());

//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                Log.d(TAG,"定时 推送消息到客户端");
//                EventMsg eventMsg = new EventMsg();
//                eventMsg.setMsgId(11);
//                Bundle bundle = new Bundle();
//                bundle.putString("data","我来自推送消息");
//                eventMsg.setEventData(bundle);
//                PushSevManager.getInstance().pushEvent(MediaConstant.MSG.TOPIC,eventMsg);
//            }
//        },5000,2000);

    }

    @Override
    public String getHost() {
        Log.d(TAG, "MyService getHost");
        return HOST;
    }

    @Override
    protected void onPushChannel(IBinder channel) {
        PushSevManager.getInstance().registerPushChannel(mContext, channel);
    }

    @Override
    protected Bundle onCommand(EventData eventData) {
        int code = eventData.getCode();
        Log.d(TAG, "onCommand code:" + code);
        switch (code) {
            case MediaConstant.MSG.ON_FRAME:
                Bundle data = eventData.getData();
                int dataLength = data.getInt(MediaConstant.MSG.FRAME_LENGTH);
                byte[] frameData = eventData.getBigData();
//                byte[] frameData = data.getByteArray(MediaConstant.MSG.FRAME_DATA);
                putYUVData(frameData,dataLength);
                break;
            default:break;
        }

        return super.onCommand(eventData);
    }

    @Override
    protected Bundle onCommand(int code, Bundle data) {
        Log.d(TAG, "code:" + code);
        switch (code) {
            case MediaConstant.MSG.START_ENCODE:
                Log.d(TAG,"data:"+data.toString());
//                int width = data.getInt(MediaConstant.MSG.WIDTH);
//                int height = data.getInt(MediaConstant.MSG.HEIGHT);
//                int framerate = data.getInt(MediaConstant.MSG.FRAMERATE);
//                int biterate = data.getInt(MediaConstant.MSG.BITRATE);
//                avcCodec = new AvcEncoder(width, height, framerate, biterate);
//                avcCodec.StartEncoderThread();
                Log.d(TAG,"START_ENCODE tid:"+Thread.currentThread().getId());
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"START_ENCODE post tid:"+Thread.currentThread().getId());
                        VideoUtils.showVideo(mContext);
                    }
                });

                break;
            case MediaConstant.MSG.STOP_ENCODE:
//                avcCodec.StopThread();
                Log.d(TAG,"STOP_ENCODE tid:"+Thread.currentThread().getId());
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"STOP_ENCODE post tid:"+Thread.currentThread().getId());
                        VideoUtils.hideVideo();
                    }
                });

                break;


            default:
                break;
        }
        Bundle replyData = new Bundle();
        replyData.putString("data", "收到客户端请求200");
        return replyData;
    }

    public void putYUVData(byte[] buffer, int length) {
        if (MediaConstant.YUVQueue.size() >= 10) {
            MediaConstant.YUVQueue.poll();
        }
        MediaConstant.YUVQueue.add(buffer);
    }
}
