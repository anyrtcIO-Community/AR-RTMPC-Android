package org.ar;

import android.app.Application;

import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.NoHttp;

import org.ar.utils.DeveloperInfo;
import org.ar.utils.NameUtils;
import org.ar.rtmpc_hybrid.ARRtmpcEngine;

/**
 * Created by Skyline on 2016/8/3.
 */
public class ARApplication extends Application {

    public static ARApplication mARApplication;
    private static String NickName="";
    public static String LIVE_ID=(int)((Math.random()*9+1)*100000)+"";//直播间ID

    //如果需要使用你自己的CDN 将useAnyRTCCDN 设置为false  并将PUSH_URL PULL_URL 配置好
    public static boolean useAnyRTCCDN = true;
    public static String PUSH_URL="";
    public static String PULL_URL="";
    @Override
    public void onCreate() {
        super.onCreate();
        mARApplication =this;
        NickName= NameUtils.getNickName();
        ARRtmpcEngine.Inst().initEngineWithARInfo(getApplicationContext(), DeveloperInfo.DEVELOPERID, DeveloperInfo.APPID, DeveloperInfo.APPKEY, DeveloperInfo.APPTOKEN);
        //配置私有云
//        ARRtmpcEngine.Inst().configServerForPriCloud("", 0000);


        InitializationConfig  config = InitializationConfig.newBuilder(this)
                .connectionTimeout(15*1000)
                .readTimeout(15*1000)
                .retry(1).build();
        NoHttp.initialize(config);
//        Logger.setDebug(false);
    }
    public  static Application App(){
        return mARApplication;
    }

    public static String getNickName(){
        return NickName;
    }

}
