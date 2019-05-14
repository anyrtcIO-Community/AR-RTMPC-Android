package org.ar.common.utils;

import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * RTKUtils 提供了辅助函数来管理线程安全 说明：工具类 By AnyRTC.inc - 2016/9/18
 *
 * @author Ming
 */
public final class ARUtils {

    private ARUtils() {
    }

    /**
     * NonThreadSafe 是一个助手类用于帮助验证类的方法被称为从相同的线程。
     *
     * @author Maozongwu
     */
    public static class NonThreadSafe {
        private final Long threadId;

        public NonThreadSafe() {
            // Store thread ID of the creating thread.
            threadId = Thread.currentThread().getId();
        }

        /**
         * 检查是否有效的方法/创建线程。
         *
         * @return 是否是同一个线程
         */
        public boolean calledOnValidThread() {
            return threadId.equals(Thread.currentThread().getId());
        }
    }

    /**
     * 当断言失败时抛出一个异常
     *
     * @param condition 断言条件 true/false
     */
    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    /**
     * 建立一个线程信息的字符串
     *
     * @return 生成的字符串
     */
    public static String getThreadInfo() {
        return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId() + "]";
    }

    /**
     * 来自系统属性的信息
     *
     * @param tag 日志标识符
     */
    public static void logDeviceInfo(String tag) {
        Log.d(tag,
                "Android SDK: " + Build.VERSION.SDK_INT + ", " + "Release: " + Build.VERSION.RELEASE + ", " + "Brand: "
                        + Build.BRAND + ", " + "Device: " + Build.DEVICE + ", " + "Id: " + Build.ID + ", "
                        + "Hardware: " + Build.HARDWARE + ", " + "Manufacturer: " + Build.MANUFACTURER + ", "
                        + "Model: " + Build.MODEL + ", " + "Product: " + Build.PRODUCT);
    }

    /**
     * 字节流转化为字符串
     *
     * @param buffer 字节流
     * @return 转过后的字符串
     */
    public static String byteBufferToString(ByteBuffer buffer) {
        CharBuffer charBuffer = null;
        try {
            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer);
            buffer.flip();
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
