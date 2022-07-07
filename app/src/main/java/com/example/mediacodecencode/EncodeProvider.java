package com.example.mediacodecencode;

import android.util.Log;

import com.yunxi.ipc.IpcSevProvider;

/**
 * @author : longyue
 * @data : 2022/6/14
 * @email : changyl@yunxi.tv
 */
public class EncodeProvider extends IpcSevProvider {

    private String TAG = EncodeProvider.class.getSimpleName();
    private static final String AUTHORITY = "com.wt.ipc.sev";

    public EncodeProvider(){

        Log.d(TAG,"MyProvider const");
    }


    @Override
    protected String registerAuthority() {
        return AUTHORITY;
    }
}
