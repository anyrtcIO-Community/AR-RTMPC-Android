package org.anyrtc.live_line;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;

import org.anyrtc.adapter.LiveAudioAdapter;
import org.anyrtc.adapter.LiveChatAdapter;
import org.anyrtc.application.HybirdApplication;
import org.anyrtc.rtmpc_hybrid.RTMPCAbstractHoster;
import org.anyrtc.rtmpc_hybrid.RTMPCHosterKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHybird;
import org.anyrtc.rtmpc_hybrid.RTMPCVideoView;
import org.anyrtc.utils.AnyRTCUtils;
import org.anyrtc.utils.AudioItemBean;
import org.anyrtc.utils.ChatMessageBean;
import org.anyrtc.utils.RTMPAudioManager;
import org.anyrtc.utils.RTMPCHttpSDK;
import org.anyrtc.utils.RTMPUrlHelper;
import org.anyrtc.utils.ShareHelper;
import org.anyrtc.utils.SoftKeyboardUtil;
import org.anyrtc.utils.ThreadUtil;
import org.anyrtc.widgets.ScrollRecycerView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 音频主播页面
 */
public class AudioHosterActivity extends AppCompatActivity implements ScrollRecycerView.ScrollPosation{

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
    private String mVodSvrId;
    private String mVodResTag;

    private RTMPCHosterKit.RTMPVideoMode mVideoMode;

    private SoftKeyboardUtil softKeyboardUtil;
    private int duration = 100;//软键盘延迟打开时间

    private Switch mCheckBarrage;
    private DanmakuView mDanmakuView;
    private EditText editMessage;
    private ViewAnimator vaBottomBar;
    private LinearLayout llInputSoft;
    private FrameLayout flChatList;
    private ScrollRecycerView rcLiveChat;

    private ListView rcAudioChat;
    private RelativeLayout rlTopViews;
    private ImageView btnChat;

    private List<AudioItemBean> mAudioChatList;
    private List<ChatMessageBean> mChatMessageList;
    private LiveChatAdapter mChatLiveAdapter;
    private LiveAudioAdapter mAudioChatLiveAdapter;

    private int maxMessageList = 150; //列表中最大 消息数目

    private RTMPAudioManager mRtmpAudioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_hoster);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mChatMessageList = new ArrayList<ChatMessageBean>();
        mAudioChatList = new ArrayList<AudioItemBean>();
        mShareHelper = new ShareHelper(this);
        mHosterId = getIntent().getExtras().getString("hosterId");
        mRtmpPushUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("andyrtcId");
        mUserData = getIntent().getExtras().getString("userData");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mTopic = getIntent().getExtras().getString("topic");
        String videoMode = getIntent().getExtras().getString("video_mode");
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

        if(videoMode.equals(RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_HD.toString())) {
            mVideoMode = RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_HD;
        } else if(videoMode.equals(RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_QHD.toString())) {
            mVideoMode = RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_QHD;
        } else if(videoMode.equals(RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_SD.toString())) {
            mVideoMode = RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_SD;
        } else if(videoMode.equals(RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_Low.toString())) {
            mVideoMode = RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_Low;
        }

        rcLiveChat = (ScrollRecycerView) findViewById(R.id.rc_live_chat);
        rcAudioChat = (ListView) findViewById(R.id.rc_audio_chat);
        mChatLiveAdapter = new LiveChatAdapter(mChatMessageList, this);
        mAudioChatLiveAdapter = new LiveAudioAdapter(mAudioChatList, this, mCloseAudioHelper, true, mHosterId);
        rcAudioChat.setAdapter(mAudioChatLiveAdapter);

        rcLiveChat.setLayoutManager(new LinearLayoutManager(this));
        rcLiveChat.setAdapter(mChatLiveAdapter);
        rcLiveChat.addScrollPosation(this);
        setEditTouchListener();
        vaBottomBar.setAnimateFirstView(true);

        /**
         * 设置音频模式并且监测音频大小
         */
        RTMPCHybird.Inst().SetLiveToAuidoOnly(true, true);
        mVideoView = new RTMPCVideoView((RelativeLayout) findViewById(R.id.rl_rtmpc_videos), RTMPCHybird.Inst().Egl(), true);
        mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);
        mHosterKit = new RTMPCHosterKit(this, mHosterListener);

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
        }

        mStartRtmp = true;

        /**
         * 设置推流视频质量
         * RTMPC_Video_Low  流畅
         * RTMPC_Video_SD   标清
         * RTMPC_Video_QHD  高清
         * RTMPC_Video_HD   超高清
         */
        mHosterKit.SetVideoMode(mVideoMode);

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
         * 最后一个参数为RTC服务器的区域：默认为“”；如果需要海外服务器，请与公司商务联系（021-65650071）
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

        if(mVodSvrId != null && mVodSvrId.length() > 0 && mVodResTag.length() > 0) {
            //关闭录像
            RTMPCHttpSDK.CloseRecRtmpStream(getApplicationContext(), RTMPCHybird.Inst().GetHttpAddr(), "16864513", "RTMPC_Line",
                    "fd990746efad2f63016d31b3a68f4cf6", mVodSvrId, mVodResTag);
        }

        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        if (mHosterKit != null) {
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
        } else if (btn.getId() == R.id.btn_send_message) {
            String message = editMessage.getText().toString();
            editMessage.setText("");
            if (message.equals("")) {
                return;
            }
            if (mCheckBarrage.isChecked()) {
                mHosterKit.SendBarrage(mNickname, "", message);
                IDanmakuItem item = new DanmakuItem(AudioHosterActivity.this, new SpannableString(message), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
                mDanmakuView.addItemToHead(item);
            } else {
                mHosterKit.SendUserMsg(mNickname, "", message);
            }

            addChatMessageList(new ChatMessageBean(mNickname, mNickname, "", message));
        } else if (btn.getId() == R.id.iv_host_text) {
            btnChat.clearFocus();
            vaBottomBar.setDisplayedChild(1);
            editMessage.requestFocus();
            softKeyboardUtil.showKeyboard(AudioHosterActivity.this, editMessage);
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
        }
    };

    /**
     * 更新文字信息列表
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

        softKeyboardUtil.observeSoftKeyboard(AudioHosterActivity.this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
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
                addAudioChatList(strLivePeerID, strCustomID);
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

    /**
     * 音频连麦人员添加到连麦列表中
     * @param strLivePeerID
     * @param strCustomID
     */
    private void addAudioChatList(String strLivePeerID, String strCustomID) {
        if(!strCustomID.equals("RTMPC_Hoster")) {
            AudioItemBean audioBean = new AudioItemBean();
            audioBean.setmStrCustomid(strCustomID);
            audioBean.setmStrPeerId(strLivePeerID);
            mAudioChatList.add(audioBean);
            mAudioChatLiveAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新语音连麦人员信息
     * @param customid
     * @param level
     */
    private void updateAudioChatLevel(String customid, int level) {
        if(null == mAudioChatList) {
            mAudioChatList = new ArrayList<AudioItemBean>();
        }
        for(int i = 0; i < mAudioChatList.size(); i ++) {
            if(customid.equals(mAudioChatList.get(i).getmStrCustomid())) {
                mAudioChatList.get(i).setmAudioLevel(level);
                break;
            }
        }
        mAudioChatLiveAdapter.notifyDataSetChanged();
    }

    /**
     * 连麦挂断后更新连麦人数列表
     * @param strPeerid
     */
    private void removeAudioChatLevel(String strPeerid) {
        for(int i = 0; i < mAudioChatList.size(); i ++) {
            if(strPeerid.equals(mAudioChatList.get(i).getmStrPeerId())) {
                mAudioChatList.remove(i);
                break;
            }
        }
        mAudioChatLiveAdapter.notifyDataSetChanged();
    }


    @Override
    public void ScrollButtom() {

    }

    @Override
    public void ScrollNotButtom() {

    }

    /**
     * 音频连麦时的关闭连麦回调
     */
    private LiveAudioAdapter.CloseAudioHelper mCloseAudioHelper = new LiveAudioAdapter.CloseAudioHelper() {
        @Override
        public void onCloseAudioChat(String strLivePeerid) {
            mHosterKit.HangupRTCLine(strLivePeerid);
            removeAudioChatLevel(strLivePeerid);
        }
    };

    /**
     * 主播回调信息接口
     */
    private RTMPCAbstractHoster mHosterListener = new RTMPCAbstractHoster() {
        /**
         * rtmp连接成功
         */
        @Override
        public void OnRtmpStreamOKCallback() {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_rtmp_connection_status)).setText(R.string.str_rtmp_connect_success);
                    //开始录像
                    RTMPCHttpSDK.RecordRtmpStream(AudioHosterActivity.this, RTMPCHybird.Inst().GetHttpAddr(), "16864513", "RTMPC_Line",
                            "fd990746efad2f63016d31b3a68f4cf6", mAnyrtcId, mRtmpPushUrl, mAnyrtcId, new RTMPCHttpSDK.RTMPCHttpCallback(){
                                @Override
                                public void OnRTMPCHttpOK(String strContent) {
                                    try {
                                        JSONObject recJson = new JSONObject(strContent);
                                        mVodSvrId = recJson.getString("VodSvrId");
                                        mVodResTag = recJson.getString("VodResTag");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void OnRTMPCHttpFailed(int code) {

                                }
                            });
                }
            });
        }

        /**
         * rtmp 重连次数
         * @param times 重连次数
         */
        @Override
        public void OnRtmpStreamReconnectingCallback(final int times) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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
         * rtmp 音频模式下，音频实时检测
         */
        @Override
        public void OnRtmpAudioLevelCallback(final String strCustomId, final int level) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateAudioChatLevel(strCustomId, level);
                }
            });
        }

        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         * @param strErr 原因
         */
        @Override
        public void OnRTCOpenLineResultCallback(final int code, String strErr) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        ((TextView) findViewById(R.id.txt_rtc_connection_status)).setText(R.string.str_rtc_connect_success);
                    } else {
                        ((TextView) findViewById(R.id.txt_rtc_connection_status)).setTextColor(R.color.yellow);
                        ((TextView) findViewById(R.id.txt_rtc_connection_status)).setText(AnyRTCUtils.getErrString(code));
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowDialog(AudioHosterActivity.this, strLivePeerID, strCustomID);
                }
            });
        }

        /**
         * 音频连线超过4人时回调
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCLineFullCallback(final String strLivePeerID, String strCustomID, String strUserData) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AudioHosterActivity.this, getString(R.string.str_connect_full), Toast.LENGTH_LONG).show();
                    mHosterKit.RejectRTCLine(strLivePeerID, true);
                }
            });
        }

        /**
         * 游客挂断连线回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCancelLineCallback(final String strLivePeerID) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeAudioChatLevel(strLivePeerID);
                    Toast.makeText(AudioHosterActivity.this, getString(R.string.str_line_disconnect), Toast.LENGTH_LONG).show();
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 207) {
                        Toast.makeText(AudioHosterActivity.this, getString(R.string.str_apply_anyrtc_account), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }

        /**
         * 连线接通时的视频图像回调；音频模式时不用进行操作；
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOpenVideoRenderCallback(final String strLivePeerID, final String strCustomID) {

        }

        /**
         * 连线关闭时的视频图像回调；音频模式时不用进行操作；
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCloseVideoRenderCallback(final String strLivePeerID, final String strCustomID) {

        }

        /**
         * 音频连麦接通时回调
         * @param strLivePeerID
         * @param strCustomID
         */
        @Override
        public void OnRTCOpenAudioLineCallback(final String strLivePeerID, final String strCustomID) {

        }

        /**
         * 音频连麦挂断时回调
         * @param strLivePeerID
         * @param strCustomID
         */
        @Override
        public void OnRTCCloseAudioLineCallback(final String strLivePeerID, final String strCustomID) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeAudioChatLevel(strLivePeerID);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, "", strBarrage));
                    IDanmakuItem item = new DanmakuItem(AudioHosterActivity.this, new SpannableString(strBarrage), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_watcher_number)).setText(mHosterId);
                    //((TextView) findViewById(R.id.txt_watcher_number)).setText(String.format(getString(R.string.str_live_watcher_number), totalMembers));
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject userData = new JSONObject(strUserData);
                        addChatMessageList(new ChatMessageBean(userData.getString("NickName"), "", userData.getString("IconUrl"), ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * 直播观看总人数回调结束
         */
        @Override
        public void OnRTCMemberListUpdateDoneCallback() {

        }
    };
}
