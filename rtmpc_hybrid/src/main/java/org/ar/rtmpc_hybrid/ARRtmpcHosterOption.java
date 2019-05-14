package org.ar.rtmpc_hybrid;

import org.ar.common.enums.ARVideoCommon;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */

public class ARRtmpcHosterOption {
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
     * anyRTC视频清晰标准；默认：标清（AnyRTC_Video_SD）
     */
    private ARVideoCommon.ARVideoProfile videoProfile = ARVideoCommon.ARVideoProfile.ARVideoProfile360x640;
    /**
     * anyRTC视频帧率；默认：15帧（ARVideoFrameRateFps15）
     */
    private ARVideoCommon.ARVideoFrameRate videoFps = ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps15;
    /**
     * 媒体类型
     */
    private ARVideoCommon.ARMediaType mediaType = ARVideoCommon.ARMediaType.Video;

    private ARRtmpcLineLayoutTemplate lineLayoutTemplate=ARRtmpcLineLayoutTemplate.RTMPC_LINE_V_1big_3small;

    public void setOptionParams(boolean isDefaultFrontCamera, ARVideoCommon.ARVideoOrientation videoOrientation, ARVideoCommon.ARVideoProfile videoProfile, ARVideoCommon.ARVideoFrameRate videoFps, ARVideoCommon.ARMediaType mediaType, ARRtmpcLineLayoutTemplate lineLayoutTemplate) {
        this.isDefaultFrontCamera = isDefaultFrontCamera;
        this.videoOrientation = videoOrientation;
        this.videoProfile = videoProfile;
        this.videoFps = videoFps;
        this.mediaType = mediaType;
        this.lineLayoutTemplate = lineLayoutTemplate;
    }

    public ARRtmpcHosterOption() {
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

    protected ARVideoCommon.ARVideoProfile getVideoProfile() {
        return videoProfile;
    }

    public void setVideoProfile(ARVideoCommon.ARVideoProfile videoProfile) {
        this.videoProfile = videoProfile;
    }

    protected ARVideoCommon.ARVideoFrameRate getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(ARVideoCommon.ARVideoFrameRate videoFps) {
        this.videoFps = videoFps;
    }

    protected ARVideoCommon.ARMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(ARVideoCommon.ARMediaType mediaType) {
        this.mediaType = mediaType;
    }

    protected ARRtmpcLineLayoutTemplate getLineLayoutTemplate() {
        return lineLayoutTemplate;
    }

    public void setLineLayoutTemplate(ARRtmpcLineLayoutTemplate lineLayoutTemplate) {
        this.lineLayoutTemplate = lineLayoutTemplate;
    }
}
