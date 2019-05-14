package org.ar.rtmpc_hybrid;

import org.ar.common.enums.ARVideoCommon;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */

public class ARRtmpcGuestOption {
    /**
     /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean isDefaultFrontCamera = true;
    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private ARVideoCommon.ARVideoOrientation videoOrientation = ARVideoCommon.ARVideoOrientation.Portrait;
    /**
     * 媒体类型
     */
    private ARVideoCommon.ARMediaType mediaType = ARVideoCommon.ARMediaType.Video;


    public void setOptionParams(boolean isDefaultFrontCamera, ARVideoCommon.ARVideoOrientation videoOrientation,  ARVideoCommon.ARMediaType mediaType) {
        this.isDefaultFrontCamera = isDefaultFrontCamera;
        this.videoOrientation = videoOrientation;
        this.mediaType = mediaType;
    }

    public ARRtmpcGuestOption() {
    }

    protected boolean isDefaultFrontCamera() {
        return isDefaultFrontCamera;
    }

    public void setDefaultFrontCamera(boolean defaultFrontCamera) {
        isDefaultFrontCamera = defaultFrontCamera;
    }

    public ARVideoCommon.ARVideoOrientation getVideoOrientation() {
        return videoOrientation;
    }

    public void setVideoOrientation(ARVideoCommon.ARVideoOrientation videoOrientation) {
        this.videoOrientation = videoOrientation;
    }

    protected ARVideoCommon.ARMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(ARVideoCommon.ARMediaType mediaType) {
        this.mediaType = mediaType;
    }

}
