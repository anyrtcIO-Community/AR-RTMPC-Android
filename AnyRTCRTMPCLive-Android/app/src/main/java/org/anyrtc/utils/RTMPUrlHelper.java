package org.anyrtc.utils;

/**
 * Created by Skyline on 2016/7/29.
 */
public class RTMPUrlHelper {
    /**
     * rtmp 推流地址
     */
    public static final String RTMP_PUSH_URL = "rtmp://192.168.7.207:1935/live/%s";
    /**
     * rtmp 拉流地址
     */
    public static final String RTMP_PULL_URL = "rtmp://192.168.7.207:1935/live/%s";
    /**
     * hls 地址
     */
    public static final String HLS_URL = "http://192.169.7.207/live/%s.m3u8";
}
