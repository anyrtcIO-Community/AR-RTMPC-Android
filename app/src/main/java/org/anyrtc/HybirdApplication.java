package org.anyrtc;

import android.app.Application;

import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;

import org.anyrtc.rtmpc_hybrid.RTMPCHybrid;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.NameUtils;

/**
 * Created by Skyline on 2016/8/3.
 */
public class HybirdApplication extends Application {

    public static HybirdApplication mHybirdApplication;
    private static String NickName="";

    @Override
    public void onCreate() {
        super.onCreate();
        mHybirdApplication=this;
        NickName= NameUtils.getNickName();
         // 初始化RTMPC引擎 并配置开发者信息
        RTMPCHybrid.Inst().initEngineWithAnyrtcInfo(getApplicationContext(), Constans.DEVELOPERID, Constans.APPID, Constans.APPKEY, Constans.APPTOKEN);

        //配置私有云
//        RTMPCHybrid.Inst().configServerForPriCloud("", 0);
        RTMPCHybrid.disableHWDecode();
        InitializationConfig config = InitializationConfig.newBuilder(this)
                .connectionTimeout(15*1000)
                .readTimeout(15*1000)
                .retry(1).build();
        NoHttp.initialize(config);
        Logger.setDebug(true); // 开启NoHttp调试模式。
        Logger.setTag("HttpInfo"); // 设置NoHttp打印Log的TAG。
    }
    public  static Application App(){
        return mHybirdApplication;
    }

    public static String getAnyRTCId(){
        return (int)((Math.random()*9+1)*100000)+"";
    }
    public static String getNickName(){
        return NickName;
    }

}
