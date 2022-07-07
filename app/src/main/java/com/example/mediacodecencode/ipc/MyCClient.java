package com.example.mediacodecencode.ipc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.yunxi.windowview.data.MediaConstant;
import com.yunxi.ipc.ClientPushManager;
import com.yunxi.ipc.IPushCallBack;
import com.yunxi.ipc.IpcClient;
import com.yunxi.ipc.base.ICommand;
import com.yunxi.ipc.bean.EventData;
import com.yunxi.ipc.bean.EventMsg;
import com.yunxi.ipc.bean.RemoteInfo;
import com.yunxi.ipc.utils.LogUtils;


/**
 * @author longyue
 */
public class MyCClient extends IpcClient {
    private String TAG = MyCClient.class.getSimpleName();
    private static final String HOST = "1000";

    private HandlerThread updateHandlerThread;
    private UpdateHandler mH;
    private final long CHECK_DELAY_TIME = 2 * 1000;
    private final int CHECK_REGISTER_SEV_CODE = 800;

    private static final Object object = new Object();
    private static MyCClient instance;
    private final Context mContext;
    private OnPushCall pushCall;


    public static MyCClient getInstance(Context context){
        if (instance == null) {
            synchronized (object){
                if (instance == null) {
                    instance = new MyCClient(context);
                }
            }
        }
        return instance;
    }

    public MyCClient(Context context) {
        super(context);
        LogUtils.setEnv(LogUtils.INFO);
        mContext = context;

        updateHandlerThread = new HandlerThread("update_handler");
        updateHandlerThread.start();
        mH = new UpdateHandler(updateHandlerThread.getLooper());

        ClientPushManager.getInstance().init(context);
        ClientPushManager.getInstance().subTopic(MediaConstant.MSG.TOPIC,pushCallBack);


        mH.sendEmptyMessage(CHECK_REGISTER_SEV_CODE);
    }

    public void init(OnPushCall pushCall){
        this.pushCall = pushCall;
    }

    @Override
    protected RemoteInfo getRemountInfo() {
        String authority = "com.wt.ipc.sev";
        LogUtils.d("MCCclent getRemountInfo:"+authority);
        RemoteInfo remoteInfo = new RemoteInfo(authority, HOST);
        return remoteInfo;
    }

    public void callCommand(int code, Bundle data){
        ICommand.Stub pushChannel = ClientPushManager.getInstance().getRemountChannel();
        this.registerPushChannel(pushChannel);
        Bundle bundle = callRemount(code, data);
        if (bundle != null) {
            LogUtils.d("client callCommand:"+bundle.getString("data"));
        }
    }

    public void callCommand(EventData data){
        ICommand.Stub pushChannel = ClientPushManager.getInstance().getRemountChannel();
        this.registerPushChannel(pushChannel);
        Bundle bundle = callRemount(data);
        if (bundle != null) {
            LogUtils.d("client callCommand:"+bundle.getString("data"));
        }
    }


    private IPushCallBack pushCallBack = new IPushCallBack() {
        @Override
        public void pushMsg(String topic, EventMsg eventMsg) {
            Log.d(TAG,"client receive topic:"+topic+ ";event:"+eventMsg.getMsgId());
            pushCall.pushMsg(topic,eventMsg);
        }
    };




    public interface OnPushCall{
        void pushMsg(String topic, EventMsg eventMsg);
    }


    private class UpdateHandler extends Handler {
        private UpdateHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CHECK_REGISTER_SEV_CODE:
                    ICommand.Stub aiPushChannel = ClientPushManager.getInstance().getRemountChannel();
                    boolean isRegisterAiChannel = false;
                    isRegisterAiChannel = registerPushChannel(aiPushChannel);
                    Log.d("UpdateProxy","isRegisterAiChannel");
                    if(!isRegisterAiChannel){
                        mH.sendEmptyMessageDelayed(CHECK_REGISTER_SEV_CODE,CHECK_DELAY_TIME);
                    }
                    break;
                default:break;
            }
        }
    }
}
