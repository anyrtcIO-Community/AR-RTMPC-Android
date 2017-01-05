package org.anyrtc.application;

import com.squareup.leakcanary.LeakCanary;

import org.anyrtc.utils.AbsApplication;
import org.anyrtc.utils.NameUtils;
import org.anyrtc.utils.RTMPCHttpSDK;
import org.anyrtc.utils.SharePrefUtil;

/**
 * Created by Skyline on 2016/8/3.
 */
public class HybirdApplication extends AbsApplication {
    private String mNickname;
    private String mHostId;

    @Override
    public void onCreate() {
        super.onCreate();
        mNickname = SharePrefUtil.getString("nickname");
        mHostId = SharePrefUtil.getString("hostid");
        if(null == mNickname || "".equals(mNickname)) {
            mNickname = NameUtils.getNickName();
            SharePrefUtil.putString("nickname", mNickname);
        }

        if(null == mHostId || "".equals(mHostId)) {
            mHostId = RTMPCHttpSDK.getRandomString(9);
            SharePrefUtil.putString("hostid", mHostId);
        }
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    /**
     * 随机获取用户名
     * @return
     */
    public String getmNickname() {
        return mNickname;
    }

    public String getmHostId() {
        return mHostId;
    }
}
