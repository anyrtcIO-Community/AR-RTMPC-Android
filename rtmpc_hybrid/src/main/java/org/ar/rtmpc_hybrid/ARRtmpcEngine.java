package org.ar.rtmpc_hybrid;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.compat.BuildConfig;
import android.util.Log;

import org.ar.common.enums.ARLogLevel;
import org.ar.common.utils.DeviceUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.ar.common.utils.NetworkUtils;
import org.ar.common.utils.PackageUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public class ARRtmpcEngine {
    private static String tag = "ARRtmpcEngine";

    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("rtmpc-jni");
    }

    private final LooperExecutor executor;
    private final EglBase eglBase;
    private Context context;
    private String developerId, appId, appKey, appToken;
    private String strSvrAddr = "cloud.anyrtc.io";
    private ARRtmpcHosterOption hosterOption=new ARRtmpcHosterOption();
    private ARRtmpcGuestOption guestOption=new ARRtmpcGuestOption();

    private static class SingletonHolder {
        private static final ARRtmpcEngine INSTANCE = new ARRtmpcEngine();
    }

    public static final ARRtmpcEngine Inst() {
        return SingletonHolder.INSTANCE;
    }

    private ARRtmpcEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
        executor.requestStart();
    }

    public Context getContext() {
        return context;
    }

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    public LooperExecutor executor() {
        return executor;
    }

    /**
     * 获取EglBase对象
     *
     * @return EglBase对象
     */
    public EglBase egl() {
        return eglBase;
    }


    /**
     * 初始化应用信息
	 * 
     * @param ctx 应用上下文环境
     * @param strAppId anyRTC平台应用的appId
     * @param strToken anyRTC平台应用的appToken
     */
    public void initEngineWithAppInfo(final Context ctx, final String strAppId, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                appId = strAppId;
                appToken = strToken;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                context = ctx;
                nativeInitEngineWithAppInfo(strAppId, strToken);
            }
        });
    }
    public void initEngineWithARInfo(final Context ctx, final String strDeveloperId, final String strAppId,
                                         final String strAESKey, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                developerId = strDeveloperId;
                appId = strAppId;
                appKey = strAESKey;
                appToken = strToken;
                context = ctx;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                nativeInitEngineWithARInfo(strDeveloperId, strAppId, strAESKey, strToken,getPackageName());
            }
        });
    }
    /**
     * 配置私有云
     *
     * @param strAddress 私有云IP地址或者域名
     * @param nPort      私有云端口
     */
    public void configServerForPriCloud(final String strAddress, final int nPort) {
        strSvrAddr = strAddress;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeConfigServerForPriCloud(strAddress, nPort);
            }
        });
    }

    public ARRtmpcHosterOption getHosterOption() {
        return hosterOption;
    }

    public ARRtmpcGuestOption getGuestOption() {
        return guestOption;
    }

    /**
     * 获取RTC服务器地址
     *
     * @return RTC服务器地址
     */
    protected String getHttpAddr() {
        return String.format("%s:9090", strSvrAddr);
    }

    /**
     * 获取RTC服务器地址
     *
     * @return RTC服务器地址
     */
    protected String getHttpAuthAddr() {
        return String.format("%s:9080", strSvrAddr);
    }

    /**
     * 获取anyRTC平台的appId
     *
     * @return anyRTC平台的appId
     */
    protected String getStrAppId() {
        return appId;
    }

    /**
     * 获取anyRTC平台的developerId
     *
     * @return anyRTC平台的developerId
     */
    protected String getStrDeveloperId() {
        return developerId;
    }

    /**
     * 获取anyRTC平台的appKey
     *
     * @return anyRTC平台的appKey
     */
    protected String getStrAppKey() {
        return appKey;
    }

    /**
     * 获取anyRTC平台的AppToken
     *
     * @return anyRTC平台的AppToken
     */
    protected String getStrAppToken() {
        return appToken;
    }

    /**
     * 获取应用包名
     *
     * @return 应用包名
     */
    protected String getPackageName() {
        return context.getPackageName();
    }

    /**
     * 关闭设备硬件编码
     */
    public  void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    /**
     * 关闭设备硬件解码
     */
    public  void disableHWDecode() {
        MediaCodecVideoDecoder.disableVp8HwCodec();
        MediaCodecVideoDecoder.disableVp9HwCodec();
        MediaCodecVideoDecoder.disableH264HwCodec();
    }

    /**
     * 获取sdk版本号
     *
     * @return RTMPC版本号
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取设备信息
     * @return
     */
    protected String getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operatorName", NetworkUtils.getNetworkOperatorName());
            jsonObject.put("devType", DeviceUtils.getModel());
            jsonObject.put("networkType", NetworkUtils.getNetworkType().toString().replace("NETWORK_", ""));
            jsonObject.put("osType", "Android");
            jsonObject.put("sdkVer", getSdkVersion());
            jsonObject.put("rtcVer", 60);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 设置日志显示级别
     *
     * @param logLevel 日志显示级别
     */
    public void setLogLevel(final ARLogLevel logLevel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLogLevel(logLevel.type);
            }
        });
    }

    /**
     * 权限检测
     *
     * @param permissions 权限的名称
     * @return
     */
    protected boolean checkPremission(String... permissions) {
        boolean allHave = true;
        PackageManager pm = context.getPackageManager();
        for (String permission : permissions) {
            switch (pm.checkPermission(permission, context.getPackageName())) {
                case PERMISSION_GRANTED:
                    allHave = allHave && true;
                    Log.e(tag, permission + "  PERMISSION_GRANTED");
                    continue;
                case PERMISSION_DENIED:
                    allHave = allHave && false;
                    Log.e(tag, permission + "  PERMISSION_DENIED, please allow the " + permission);
                    continue;
            }
        }
        return allHave;
    }

    private void dispose() {
        executor.requestStop();
    }

    /**
     * Jni interface
     */
    private static native void nativeInitCtx(Context ctx, EglBase.Context context);

    private static native void nativeInitEngineWithARInfo(String strDeveloperId, String strAppId,
                                                              String strKey, String strToken, String strPackageName);

    private static native void nativeInitEngineWithAppInfo(String strAppId, String strToken);

    private static native void nativeConfigServerForPriCloud(String strAddr, int nPort);

    private static native void nativeSetLogLevel(int logLevel);



    /**
     * 检查接口是否可用
     *
     * @param sdkid
     */
    public void checkSdk(int sdkid) {
        final String url = "https://www.anyrtc.io/anyrtcwebapi/collectionRtmpcSdk";
        //final String url = "http://192.168.7.218:8066/anyrtctest/anyrtcwebapi/collectionRtmpcSdk";
        HttpPost httpPost = new HttpPost(url);

        // 设置HTTP POST请求参数必须用NameValuePair对象
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("sdk_userid", developerId));
        params.add(new BasicNameValuePair("sdk_appid", appId));
        params.add(new BasicNameValuePair("sdk_sess_type", "1"));
        params.add(new BasicNameValuePair("sdk_package", context.getPackageName()));
        params.add(new BasicNameValuePair("sdk_sdkid", String.valueOf(sdkid)));
        params.add(new BasicNameValuePair("sdk_app_name", PackageUtils.getInstance().getApplicationName(context)));

        try {
            // 设置httpPost请求参数
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + new String(Base64.encodeBase64((appKey + ":" + appToken).getBytes())));
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("success");
            } else {
                System.out.println("failed");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
