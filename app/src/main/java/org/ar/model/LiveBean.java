package org.ar.model;

import java.io.Serializable;

/**
 * Created by liuxiaozhong on 2017-09-22.
 */

public class LiveBean implements Serializable{

    public String mRtmpPullUrl;
    public String mHlsUrl;
    public String mPushUrl;
    public String mLiveTopic;
    public String mAnyrtcId;
    public int isAudioLive;
    public int isLiveLandscape;
    public int liveMode;
    public String mHostName;
    public String mMemberNum="";

    public String getmRtmpPullUrl() {
        return mRtmpPullUrl;
    }

    public void setmRtmpPullUrl(String mRtmpPullUrl) {
        this.mRtmpPullUrl = mRtmpPullUrl;
    }

    public String getmHlsUrl() {
        return mHlsUrl;
    }

    public void setmHlsUrl(String mHlsUrl) {
        this.mHlsUrl = mHlsUrl;
    }

    public String getmPushUrl() {
        return mPushUrl;
    }

    public void setmPushUrl(String mPushUrl) {
        this.mPushUrl = mPushUrl;
    }

    public String getmLiveTopic() {
        return mLiveTopic;
    }

    public void setmLiveTopic(String mLiveTopic) {
        this.mLiveTopic = mLiveTopic;
    }

    public String getmAnyrtcId() {
        return mAnyrtcId;
    }

    public void setmAnyrtcId(String mAnyrtcId) {
        this.mAnyrtcId = mAnyrtcId;
    }

    public int getIsAudioLive() {
        return isAudioLive;
    }

    public void setIsAudioLive(int isAudioLive) {
        this.isAudioLive = isAudioLive;
    }

    public int getIsLiveLandscape() {
        return isLiveLandscape;
    }

    public void setIsLiveLandscape(int isLiveLandscape) {
        this.isLiveLandscape = isLiveLandscape;
    }

    public int getLiveMode() {
        return liveMode;
    }

    public void setLiveMode(int liveMode) {
        this.liveMode = liveMode;
    }

    public String getmHostName() {
        return mHostName;
    }

    public void setmHostName(String mHostName) {
        this.mHostName = mHostName;
    }

    public String getmMemberNum() {
        return mMemberNum;
    }

    public void setmMemberNum(String mMemberNum) {
        this.mMemberNum = mMemberNum;
    }
}
