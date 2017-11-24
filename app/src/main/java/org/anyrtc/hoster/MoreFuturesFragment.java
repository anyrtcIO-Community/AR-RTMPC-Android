package org.anyrtc.hoster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.anyrtc.live_line.R;
import org.anyrtc.rtmpc_hybrid.RTMPCHosterKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHybrid;

/**
 * Created by liuxiaozhong on 2017/9/24.
 */

public class MoreFuturesFragment extends Fragment implements View.OnClickListener,HosterActivity.MoreFutureListener{
    private Button more_Jingxiang, more_Camera, more_audio, more_video;
    private RTMPCHosterKit rtmpcHosterKit;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_more_futures, container, false);
        more_Jingxiang = (Button) view.findViewById(R.id.btn_jingxiang);
        more_Camera = (Button) view.findViewById(R.id.btn_camare);
        more_video = (Button) view.findViewById(R.id.btn_video);
        more_audio = (Button) view.findViewById(R.id.btn_audio);
        more_Jingxiang.setOnClickListener(this);
        more_video.setOnClickListener(this);
        more_audio.setOnClickListener(this);
        more_Camera.setOnClickListener(this);
        HosterActivity hosterActivity= (HosterActivity) getActivity();
        hosterActivity.SetMoreFutureListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_jingxiang:
                if (more_Jingxiang.isSelected()) {
                    more_Jingxiang.setSelected(false);
                    more_Jingxiang.setText("打开镜像");
                    RTMPCHybrid.Inst().setFrontCameraMirrorEnable(false);
                } else {
                    RTMPCHybrid.Inst().setFrontCameraMirrorEnable(true);
                    more_Jingxiang.setSelected(true);
                    more_Jingxiang.setText("关闭镜像");
                }
                break;
            case R.id.btn_camare:
                if (rtmpcHosterKit != null) {
                    rtmpcHosterKit.switchCamera();
                    if (more_Camera.isSelected()){
                        more_Camera.setSelected(false);
                    }else {
                        more_Camera.setSelected(true);
                    }
                }
                break;
            case R.id.btn_audio:
                if (rtmpcHosterKit != null) {
                    if (more_audio.isSelected()) {
                        more_audio.setSelected(false);
                        rtmpcHosterKit.setLocalAudioEnable(true);
                        more_audio.setText("关闭麦克风");
                    } else {
                        rtmpcHosterKit.setLocalAudioEnable(false);
                        more_audio.setSelected(true);
                        more_audio.setText("打开麦克风");
                    }
                }
                break;
            case R.id.btn_video:
                if (rtmpcHosterKit != null) {
                    if (more_video.isSelected()) {
                        more_video.setSelected(false);
                        rtmpcHosterKit.setLocalVideoEnable(true);
                        more_video.setText("关闭摄像头");
                    } else {
                        rtmpcHosterKit.setLocalVideoEnable(false);
                        more_video.setSelected(true);
                        more_video.setText("打开摄像头");
                    }
                }
                break;
        }
    }

    @Override
    public void setRTMPCHosterKit(RTMPCHosterKit hosterKit) {
        this.rtmpcHosterKit=hosterKit;
    }
}
