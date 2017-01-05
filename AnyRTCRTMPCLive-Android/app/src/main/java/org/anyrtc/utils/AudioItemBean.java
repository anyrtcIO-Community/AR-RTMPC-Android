package org.anyrtc.utils;

/**
 * Created by Skyline on 2016/12/19.
 */

public class AudioItemBean {
    private String mStrPeerId;
    private String mStrCustomid;
    private String mStrCustomIcon;
    private int mAudioLevel;

    public String getmStrCustomIcon() {
        return mStrCustomIcon;
    }

    public void setmStrCustomIcon(String mStrCustomIcon) {
        this.mStrCustomIcon = mStrCustomIcon;
    }

    public String getmStrCustomid() {
        return mStrCustomid;
    }

    public void setmStrCustomid(String mStrCustomid) {
        this.mStrCustomid = mStrCustomid;
    }

    public String getmStrPeerId() {
        return mStrPeerId;
    }

    public void setmStrPeerId(String mStrPeerId) {
        this.mStrPeerId = mStrPeerId;
    }

    public int getmAudioLevel() {
        return mAudioLevel;
    }

    public void setmAudioLevel(int mAudioLevel) {
        this.mAudioLevel = mAudioLevel;
    }
}
