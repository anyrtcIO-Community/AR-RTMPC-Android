package org.anyrtc.live_line;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;

import org.anyrtc.adapter.LiveChatAdapter;
import org.anyrtc.application.HybirdApplication;
import org.anyrtc.rtmpc_hybird.RTMPCAbstractHoster;
import org.anyrtc.rtmpc_hybird.RTMPCHosterKit;
import org.anyrtc.rtmpc_hybird.RTMPCHybird;
import org.anyrtc.rtmpc_hybird.RTMPCVideoView;
import org.anyrtc.utils.ChatMessageBean;
import org.anyrtc.utils.RTMPAudioManager;
import org.anyrtc.utils.RTMPUrlHelper;
import org.anyrtc.utils.ShareHelper;
import org.anyrtc.utils.SoftKeyboardUtil;
import org.anyrtc.utils.ThreadUtil;
import org.anyrtc.widgets.ScrollRecycerView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.List;


/**
 * 主播页面
 */
public class HosterActivity extends AppCompatActivity implements ScrollRecycerView.ScrollPosation {

    private RTMPCHosterKit mHosterKit;
    private RTMPCVideoView mVideoView;
    private boolean mStartRtmp = false;

    private ShareHelper mShareHelper;

    private String mNickname;
    private String mRtmpPushUrl;
    private String mAnyrtcId;
    private String mHlsUrl;
    private String mGuestId;
    private String mUserData;
    private String mTopic;
    private String mHosterId;

    private SoftKeyboardUtil softKeyboardUtil;
    private int duration = 100;//软键盘延迟打开时间

    private Switch mCheckBarrage;
    private DanmakuView mDanmakuView;
    private EditText editMessage;
    private ViewAnimator vaBottomBar;
    private LinearLayout llInputSoft;
    private FrameLayout flChatList;
    private ScrollRecycerView rcLiveChat;
    private RelativeLayout rlTopViews;
    private ImageView btnChat;

    List<ChatMessageBean> mChatMessageList;
    private LiveChatAdapter mChatLiveAdapter;

    private int maxMessageList = 150; //列表中最大 消息数目

    private RTMPAudioManager mRtmpAudioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoster);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mChatMessageList = new ArrayList<ChatMessageBean>();
        mShareHelper = new ShareHelper(this);
        mHosterId = getIntent().getExtras().getString("hosterId");
        mRtmpPushUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("andyrtcId");
        mUserData = getIntent().getExtras().getString("userData");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mTopic = getIntent().getExtras().getString("topic");
        mNickname = ((HybirdApplication)HybirdApplication.app()).getmNickname();
        setTitle(mTopic);
        ((TextView) findViewById(R.id.txt_title)).setText(mTopic);
        rlTopViews = (RelativeLayout) findViewById(R.id.rl_top_views);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        mCheckBarrage = (Switch) findViewById(R.id.check_barrage);
        editMessage = (EditText) findViewById(R.id.edit_message);
        vaBottomBar = (ViewAnimator) findViewById(R.id.va_bottom_bar);
        llInputSoft = (LinearLayout) findViewById(R.id.ll_input_soft);
        flChatList = (FrameLayout) findViewById(R.id.fl_chat_list);
        btnChat = (ImageView) findViewById(R.id.iv_host_text);

        rcLiveChat = (ScrollRecycerView) findViewById(R.id.rc_live_chat);
        mChatLiveAdapter = new LiveChatAdapter(mChatMessageList, this);
        rcLiveChat.setLayoutManager(new LinearLayoutManager(this));
        rcLiveChat.setAdapter(mChatLiveAdapter);
        rcLiveChat.addScrollPosation(this);
        setEditTouchListener();
        vaBottomBar.setAnimateFirstView(true);

        mVideoView = new RTMPCVideoView((RelativeLayout) findViewById(R.id.rl_rtmpc_videos), RTMPCHybird.Inst().Egl(), true);
        mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);
        mHosterKit = new RTMPCHosterKit(this, mHosterListener);

        {
            VideoRenderer render = mVideoView.OnRtcOpenLocalRender();
            mHosterKit.SetVideoCapturer(render.GetRenderPointer(), true);
        }

        {
            // Create and audio manager that will take care of audio routing,
            // audio modes, audio device enumeration etc.
            mRtmpAudioManager = RTMPAudioManager.create(this, new Runnable() {
                // This method will be called each time the audio state (number
                // and
                // type of devices) has been changed.
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            // Store existing audio settings and change audio mode to
            // MODE_IN_COMMUNICATION for best possible VoIP performance.
            mRtmpAudioManager.init();
            mRtmpAudioManager.setAudioDevice(RTMPAudioManager.AudioDevice.SPEAKER_PHONE);
        }

        mStartRtmp = true;
        /**
         * 设置自适应码流
         */
        mHosterKit.SetNetAdjustMode(RTMPCHosterKit.RTMPNetAdjustMode.RTMP_NA_Fast);
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
    protected void onResume() {
        super.onResume();
        mDanmakuView.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.hide();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            ShowExitDialog();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuView.clear();
        softKeyboardUtil.removeGlobalOnLayoutListener(this);

        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        if (mHosterKit != null) {
            mVideoView.OnRtcRemoveLocalRender();
            mHosterKit.Clear();
            mHosterKit = null;
        }
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    public void OnBtnClicked(View btn) {
        if (btn.getId() == R.id.btn_close) {
            mStartRtmp = false;
            mHosterKit.StopRtmpStream();
            finish();
        } else if (btn.getId() == R.id.btn_copy_hls) {
            int index = mHlsUrl.lastIndexOf("/");
            int lastIndex = mHlsUrl.lastIndexOf(".");
            String shareUrl = String.format(RTMPUrlHelper.SHARE_WEB_URL, mHlsUrl.substring(index + 1, lastIndex));
            mShareHelper.shareWeiXin(mTopic, shareUrl);
        } else if (btn.getId() == R.id.btn_switch_camera) {
            mHosterKit.SwitchCamera();
        } else if (btn.getId() == R.id.btn_send_message) {
            String message = editMessage.getText().toString();
            editMessage.setText("");
            if (message.equals("")) {
                return;
            }
            if (mCheckBarrage.isChecked()) {
                mHosterKit.SendBarrage(mNickname, "", message);
                IDanmakuItem item = new DanmakuItem(HosterActivity.this, new SpannableString(message), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
                mDanmakuView.addItemToHead(item);
            } else {
                mHosterKit.SendUserMsg(mNickname, "", message);
            }

            addChatMessageList(new ChatMessageBean(mNickname, mNickname, "", message));
        } else if (btn.getId() == R.id.iv_host_text) {
            btnChat.clearFocus();
            vaBottomBar.setDisplayedChild(1);
            editMessage.requestFocus();
            softKeyboardUtil.showKeyboard(HosterActivity.this, editMessage);
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
     * 更细列表
     * @param chatMessageBean
     */
    private void addChatMessageList(ChatMessageBean chatMessageBean) {
        // 150 条 修改；

        if (mChatMessageList == null) {
            return;
        }

        if (mChatMessageList.size() < maxMessageList) {
            mChatMessageList.add(chatMessageBean);
        } else {
            mChatMessageList.remove(0);
            mChatMessageList.add(chatMessageBean);
        }
        mChatLiveAdapter.notifyDataSetChanged();
        rcLiveChat.smoothScrollToPosition(mChatMessageList.size() - 1);
    }

    /**
     * 设置 键盘的监听事件
     */
    private void setEditTouchListener() {
        softKeyboardUtil = new SoftKeyboardUtil();

        softKeyboardUtil.observeSoftKeyboard(HosterActivity.this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
            @Override
            public void onSoftKeyBoardChange(int softKeybardHeight, boolean isShow) {
                if (isShow) {
                    ThreadUtil.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            llInputSoft.animate().translationYBy(-editMessage.getHeight() / 2).setDuration(100).start();
                            flChatList.animate().translationYBy(-editMessage.getHeight() / 2).setDuration(100).start();
                        }
                    }, duration);
                } else {
                    btnChat.requestFocus();
                    vaBottomBar.setDisplayedChild(0);
                    llInputSoft.animate().translationYBy(editMessage.getHeight() / 2).setDuration(100).start();
                    flChatList.animate().translationYBy(editMessage.getHeight() / 2).setDuration(100).start();
                }
            }
        });
    }

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

    private void ShowExitDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle(R.string.str_exit);
        build.setMessage(R.string.str_live_stop);
        build.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                mStartRtmp = false;
                mHosterKit.StopRtmpStream();
                finish();
            }
        });
        build.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        build.show();
    }


    @Override
    public void ScrollButtom() {

    }

    @Override
    public void ScrollNotButtom() {

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
                    ((TextView) findViewById(R.id.txt_rtmp_connection_status)).setText(R.string.str_rtmp_connect_success);
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
                    ((TextView) findViewById(R.id.txt_rtmp_connection_status)).setText(String.format(getString(R.string.str_reconnect_times), times));
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
                    ((TextView) findViewById(R.id.txt_rtmp_connection_status)).setTextColor(R.color.yellow);
                    ((TextView) findViewById(R.id.txt_rtmp_connection_status)).setText(R.string.str_rtmp_connect_failed);
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
                        ((TextView) findViewById(R.id.txt_rtc_connection_status)).setTextColor(R.color.yellow);
                        ((TextView) findViewById(R.id.txt_rtc_connection_status)).setText(R.string.str_rtmp_connect_failed);
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
        public void OnRTCLineClosedCallback(final int code, final String strReason) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 207) {
                        Toast.makeText(HosterActivity.this, getString(R.string.str_apply_anyrtc_account), Toast.LENGTH_LONG).show();
                        finish();
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

        /**
         * 消息回调
         * @param strCustomID 消息的发送者id
         * @param strCustomName 消息的发送者昵称
         * @param strCustomHeader 消息的发送者头像url
         * @param strMessage 消息内容
         */
        @Override
        public void OnRTCUserMessageCallback(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strMessage) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, "", strMessage));
                }
            });
        }

        /**
         * 弹幕回调
         * @param strCustomID 弹幕的发送者id
         * @param strCustomName 弹幕的发送者昵称
         * @param strCustomHeader 弹幕的发送者头像url
         * @param strBarrage 弹幕的内容
         */
        @Override
        public void OnRTCUserBarrageCallback(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strBarrage) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, "", strBarrage));
                    IDanmakuItem item = new DanmakuItem(HosterActivity.this, new SpannableString(strBarrage), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
                    mDanmakuView.addItemToHead(item);
                }
            });
        }

        /**
         * 直播观看总人数回调
         * @param totalMembers 观看总人数
         */
        @Override
        public void OnRTCMemberListWillUpdateCallback(final int totalMembers) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_watcher_number)).setText(String.format(getString(R.string.str_live_watcher_number), totalMembers));
                }
            });
        }

        /**
         * 人员上下线回调
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCMemberCallback(final String strCustomID, final String strUserData) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject userData = new JSONObject(strUserData);
                        addChatMessageList(new ChatMessageBean(userData.getString("nickName"), "", userData.getString("headUrl"), ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void OnRTCMemberListUpdateDoneCallback() {

        }
    };
}
