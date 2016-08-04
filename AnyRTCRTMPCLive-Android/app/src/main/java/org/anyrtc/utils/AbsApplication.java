package org.anyrtc.utils;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;

/**
 * Created by Xiao_Bailong on 2016/5/26 0026.
 */
public class AbsApplication extends Application {
    private static AbsApplication sInstance;
    private static Context context;
    private static Handler handler;

    private static int mainThreadId;
    public static AbsApplication app() {
        return sInstance;
    }


    @Override
    public void onCreate() {
        sInstance = this;
        super.onCreate();
        //1. 获取Context
        context = getApplicationContext();

        //2. 创建handler
        handler = new Handler(Looper.getMainLooper());

        //3. 获取主线程id
        mainThreadId = android.os.Process.myTid();

        SharePrefUtil.init(this);
    }

    @Override
    public File getCacheDir() {
        Log.i("getCacheDir", "cache sdcard state: " + Environment.getExternalStorageState());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File cacheDir = getExternalCacheDir();
            if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs())) {
                Log.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());
                return cacheDir;
            }
        }

        File cacheDir = super.getCacheDir();
        Log.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());

        return cacheDir;
    }


    public static Context getContext() {
        return context;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static int getMainThreadId() {
        return mainThreadId;
    }


}
