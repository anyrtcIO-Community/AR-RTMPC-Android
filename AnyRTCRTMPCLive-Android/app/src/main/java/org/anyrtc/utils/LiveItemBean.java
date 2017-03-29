package org.anyrtc.utils;

/**
 * Created by Skyline on 2016/7/28.
 */
public class LiveItemBean {
    private String mHosterId;
    private String mRtmpPushUrl;
    private String mRtmpPullUrl;
    private String mHlsUrl;
    private String mLiveTopic;
    private String mAnyrtcId;
    private boolean mIsAudioOnly;
    private boolean mIsVideoAudio;
    private int mScreenMode = 1; //0/1: landscape/portrait
    private int mMemNumber;

    public String getmAnyrtcId() {
        return mAnyrtcId;
    }

    public void setmAnyrtcId(String mAnyrtcId) {
        this.mAnyrtcId = mAnyrtcId;
    }

    public String getmHlsUrl() {
        return mHlsUrl;
    }

    public void setmHlsUrl(String mHlsUrl) {
        this.mHlsUrl = mHlsUrl;
    }

    public String getmHosterId() {
        return mHosterId;
    }

    public void setmHosterId(String mHosterId) {
        this.mHosterId = mHosterId;
    }

    public String getmLiveTopic() {
        return mLiveTopic;
    }

    public void setmLiveTopic(String mLiveTopic) {
        this.mLiveTopic = mLiveTopic;
    }

    public String getmRtmpPushUrl() {
        return mRtmpPushUrl;
    }

    public void setmRtmpPushUrl(String mRtmpPushUrl) {
        this.mRtmpPushUrl = mRtmpPushUrl;
    }

    public String getmRtmpPullUrl() {
        return mRtmpPullUrl;
    }

    public void setmRtmpPullUrl(String mRtmpPullUrl) {
        this.mRtmpPullUrl = mRtmpPullUrl;
    }

    public int getmMemNumber() {
        return mMemNumber;
    }

    public void setmMemNumber(int mMemNumber) {
        this.mMemNumber = mMemNumber;
    }

    public boolean ismIsAudioOnly() {
        return mIsAudioOnly;
    }

    public void setmIsAudioOnly(boolean mIsAudioOnly) {
        this.mIsAudioOnly = mIsAudioOnly;
    }

    public int getmScreenMode() {
        return mScreenMode;
    }

    public void setmScreenMode(int mScreenMode) {
        this.mScreenMode = mScreenMode;
    }

    public boolean ismIsVideoAudio() {
        return mIsVideoAudio;
    }

    public void setmIsVideoAudio(boolean mIsVideoAudio) {
        this.mIsVideoAudio = mIsVideoAudio;
    }
}
