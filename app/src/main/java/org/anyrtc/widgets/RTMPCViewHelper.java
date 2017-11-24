package org.anyrtc.widgets;

import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

/**
 * Created by Eric on 2016/7/26.
 */
public interface RTMPCViewHelper {

    /**
     * VideoView 显示属性。
     * RendererCommon.ScalingType.SCALE_ASPECT_FIT（适应屏幕大小填充）,
     * RendererCommon.ScalingType.SCALE_ASPECT_FILL（根据图像大小填充）,
     * RendererCommon.ScalingType.SCALE_ASPECT_BALANCED（平衡FIT和FILL）
     *
     * @param scalingType RendererCommon.ScalingType 枚举下的有一个属性
     * @return
     */
    public VideoRenderer OnRtcOpenLocalRender(RendererCommon.ScalingType scalingType);

    /**
     * Close main  Renderer
     */
    public void OnRtcRemoveLocalRender();

    /**
     * VideoView 显示属性。
     * RendererCommon.ScalingType.SCALE_ASPECT_FIT（适应屏幕大小填充）,
     * RendererCommon.ScalingType.SCALE_ASPECT_FILL（根据图像大小填充）,
     * RendererCommon.ScalingType.SCALE_ASPECT_BALANCED（平衡FIT和FILL）
     *
     * @param strRtcPeerId RTC连接id
     * @param scalingType  RendererCommon.ScalingType 枚举下的有一个属性
     * @return
     */
    public VideoRenderer OnRtcOpenRemoteRender(String strRtcPeerId, RendererCommon.ScalingType scalingType);

    /**
     * Close  sub  Renderer
     *
     * @param strRtcPeerId RTC连接id
     */
    public void OnRtcRemoveRemoteRender(String strRtcPeerId);
}
