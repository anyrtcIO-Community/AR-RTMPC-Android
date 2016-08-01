package org.anyrtc.live_line;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.anyrtc.rtmpc_hybird.RTMPCAbstractHoster;
import org.anyrtc.rtmpc_hybird.RTMPCHosterKit;
import org.anyrtc.rtmpc_hybird.RTMPCHybird;
import org.anyrtc.rtmpc_hybird.RTMPCVideoView;
import org.anyrtc.utils.ShareHelper;
import org.webrtc.VideoRenderer;

/**
 * 主播页面
 */
public class HosterActivity extends AppCompatActivity {

    private RTMPCHosterKit mHosterKit;
    private RTMPCVideoView mVideoView;
    private boolean mStartRtmp = false;

    private ShareHelper mShareHelper;

    private String mRtmpPushUrl;
    private String mAnyrtcId;
    private String mHlsUrl;
    private String mGuestId;
    private String mUserData;
    private String mTopic;
    private String mHosterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoster);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mShareHelper = new ShareHelper(this);
        mHosterId = getIntent().getExtras().getString("hosterId");
        mRtmpPushUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("andyrtcId");
        mUserData = getIntent().getExtras().getString("userData");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mTopic = getIntent().getExtras().getString("topic");
        setTitle(mTopic);
        ((TextView) findViewById(R.id.txt_title)).setText(mTopic);
        mVideoView = new RTMPCVideoView((RelativeLayout) findViewById(R.id.rl_rtmpc_videos), RTMPCHybird.Inst().Egl(), true);

        mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);
        mHosterKit = new RTMPCHosterKit(this, mHosterListener);

        {
            VideoRenderer render = mVideoView.OnRtcOpenLocalRender();
            mHosterKit.SetVideoCapturer(render.GetRenderPointer(), true);
        }

        mStartRtmp = true;
        /**
         * 开始推流
         */
        mHosterKit.StartPushRtmpStream(mRtmpPushUrl);
        /**
         * 建立RTC连线连接
         */
        mHosterKit.OpenRTCLine(mAnyrtcId, mHosterId, mUserData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHosterKit != null) {
            mVideoView.OnRtcRemoveLocalRender();
            mHosterKit.Clear();
            mHosterKit = null;
        }
    }

    public void OnBtnClicked(View btn) {
        if (btn.getId() == R.id.btn_close) {
            mStartRtmp = false;
            mHosterKit.StopRtmpStream();
            finish();
        } else if (btn.getId() == R.id.btn_copy_hls) {
            ClipboardManager cmb = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(mHlsUrl.trim());
//            mShareHelper.shareWeiXin(mHlsUrl);
            Toast.makeText(HosterActivity.this, getString(R.string.str_copy_success), Toast.LENGTH_LONG).show();
        } else if (btn.getId() == R.id.btn_switch_camera) {
            mHosterKit.SwitchCamera();
        }
    }

    /**
     * 连线时小图标的关闭连接按钮及切换摄像头按钮
     */
    private RTMPCVideoView.BtnVideoCloseEvent mBtnVideoCloseEvent = new RTMPCVideoView.BtnVideoCloseEvent() {

        @Override
        public void CloseVideoRender(View view, String strPeerId) {
            /**
             * 挂断连线
             */
            mHosterKit.HangupRTCLine(strPeerId);
        }

        @Override
        public void OnSwitchCamera(View view) {
            /**
             * 切换摄像头
             */
            mHosterKit.SwitchCamera();
        }
    };

    /**
     * 连线弹窗
     *
     * @param context
     * @param strLivePeerID
     * @param strCustomID
     */
    private void ShowDialog(Context context, final String strLivePeerID, final String strCustomID) {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle(getString(R.string.str_connect_hoster));
        build.setMessage(String.format(getString(R.string.str_apply_connect_line), strCustomID));
        build.setPositiveButton(getString(R.string.str_agree), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /**
                 * 主播接受连线请求
                 */
                mHosterKit.AcceptRTCLine(strLivePeerID);
            }
        });
        build.setNegativeButton(getString(R.string.str_refused), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /**
                 * 主播拒绝连线请求
                 */
                mHosterKit.RejectRTCLine(strLivePeerID, true);
            }
        });

        build.show();
    }

    /**
     * 主播回调信息接口
     */
    private RTMPCAbstractHoster mHosterListener = new RTMPCAbstractHoster() {
        /**
         * rtmp连接成功
         */
        @Override
        public void OnRtmpStreamOKCallback() {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_connection_status)).setText(R.string.str_rtmp_connect_success);
                }
            });
        }

        /**
         * rtmp 重连次数
         * @param times 重连次数
         */
        @Override
        public void OnRtmpStreamReconnectingCallback(final int times) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_connection_status)).setText(String.format(getString(R.string.str_reconnect_times), times));
                }
            });
        }

        /**
         * rtmp 推流状态
         * @param delayMs 推流延时
         * @param netBand 推流码流
         */
        @Override
        public void OnRtmpStreamStatusCallback(final int delayMs, final int netBand) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_rtmp_status)).setText(String.format(getString(R.string.str_rtmp_status), delayMs, netBand));
                }
            });
        }

        /**
         * rtmp推流失败回调
         * @param code
         */
        @Override
        public void OnRtmpStreamFailedCallback(int code) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_connection_status)).setTextColor(R.color.yellow);
                    ((TextView) findViewById(R.id.txt_connection_status)).setText(R.string.str_rtmp_connect_failed);
                }
            });
        }

        /**
         * rtmp 推流关闭回调
         */
        @Override
        public void OnRtmpStreamClosedCallback() {
            finish();
        }

        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         * @param strErr 原因
         */
        @Override
        public void OnRTCOpenLineResultCallback(final int code, String strErr) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        ((TextView) findViewById(R.id.txt_rtc_connection_status)).setText(R.string.str_rtc_connect_success);
                    } else {
                        ((TextView) findViewById(R.id.txt_connection_status)).setTextColor(R.color.yellow);
                        ((TextView) findViewById(R.id.txt_connection_status)).setText(R.string.str_rtmp_connect_failed);
                    }
                }
            });

        }

        /**
         * 游客有申请连线回调
         *
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCApplyToLineCallback(final String strLivePeerID, final String strCustomID, final String strUserData) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowDialog(HosterActivity.this, strLivePeerID, strCustomID);
                }
            });
        }

        /**
         * 视频连线超过4人时回调
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCLineFullCallback(final String strLivePeerID, String strCustomID, String strUserData) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HosterActivity.this, getString(R.string.str_connect_full), Toast.LENGTH_LONG).show();
                    mHosterKit.RejectRTCLine(strLivePeerID, true);
                }
            });
        }

        /**
         * 游客挂断连线回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCancelLineCallback(String strLivePeerID) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HosterActivity.this, getString(R.string.str_line_disconnect), Toast.LENGTH_LONG).show();
                }
            });
        }

        /**
         * RTC 连接关闭回调
         * @param code 207：请去AnyRTC官网申请账号,如有疑问请联系客服!
         * @param strReason
         */
        @Override
        public void OnRTCLineClosedCallback(final int code, String strReason) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(code == 207) {
                        Toast.makeText(HosterActivity.this, getString(R.string.str_apply_anyrtc_account), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        /**
         * 连线接通时的视频图像回调；
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOpenVideoRenderCallback(final String strLivePeerID) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strLivePeerID);
                    if (null != render) {
                        mHosterKit.SetRTCVideoRender(strLivePeerID, render.GetRenderPointer());
                    }
                }
            });
        }

        /**
         * 连线关闭时的视频图像回调；
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCloseVideoRenderCallback(final String strLivePeerID) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHosterKit.SetRTCVideoRender(strLivePeerID, 0);
                    mVideoView.OnRtcRemoveRemoteRender(strLivePeerID);
                }
            });
        }

        @Override
        public void OnRTCUserMessageCallback(String strCustomID, String strCustomName, String strMessage) {

        }

        @Override
        public void OnRTCUserBarrageCallback(String strCustomID, String strCustomName, String strBarrage) {

        }

        @Override
        public void OnRTCMemberListWillUpdateCallback(int totelMembers) {

        }

        @Override
        public void OnRTCMemberCallback(String strCustomID, String strUserData) {

        }

        @Override
        public void OnRTCMemberListUpdateDoneCallback() {

        }
    };
}
