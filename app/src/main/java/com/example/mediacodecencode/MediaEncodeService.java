package com.example.mediacodecencode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


import com.example.mediacodecencode.ipc.MediaSvc;

/**
 * @author : longyue
 * @data : 2022/6/14
 * @email : changyl@yunxi.tv
 */
public class MediaEncodeService extends Service {
    private final String TAG = MediaEncodeService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        MediaSvc.getInstance(this).init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String chanId = "f-channel";
            NotificationChannel chan =new NotificationChannel(chanId, "前台服务channel",
                    NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);
            Log.d(TAG, "服务调用startForeground");

            Notification notification =
                    new Notification.Builder(this, chanId)
                            .setContentTitle("RustFisher前台服务")
                            .setContentText("https://an.rustfisher.com")
                            .setSmallIcon(R.drawable.abc_vector_test)

                            .build();
            startForeground(1, notification);
        } else {
            Log.d(TAG, "${Build.VERSION.SDK_INT} < O(API 26) ");
        }


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
