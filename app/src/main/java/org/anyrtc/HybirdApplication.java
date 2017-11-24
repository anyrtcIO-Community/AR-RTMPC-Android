package org.anyrtc;

import android.app.Application;

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
