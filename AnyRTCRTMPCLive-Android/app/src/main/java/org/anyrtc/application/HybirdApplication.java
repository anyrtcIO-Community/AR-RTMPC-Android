package org.anyrtc.application;

import org.anyrtc.utils.AbsApplication;
import org.anyrtc.utils.NameUtils;

/**
 * Created by Skyline on 2016/8/3.
 */
public class HybirdApplication extends AbsApplication {
    private String mNickname;

    @Override
    public void onCreate() {
        super.onCreate();
        mNickname = NameUtils.getNickName();
    }

    /**
     * 随机获取用户名
     * @return
     */
    public String getmNickname() {
        return mNickname;
    }
}
