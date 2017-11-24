package org.anyrtc.hoster;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.BaseActivity;
import org.anyrtc.HybirdApplication;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LiveBean;
import org.anyrtc.rtmpc_hybrid.RTMPCHttpKit;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.PermissionsCheckUtil;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.widgets.CircleImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static org.anyrtc.utils.Constans.LIVEBEAN;
import static org.anyrtc.utils.Constans.REQUECT_CODE_CAMARE;

/**
 * 直播信息输入页面
 */
public class PreStartLiveActivity extends BaseActivity implements MaterialSpinner.OnItemSelectedListener {
    @BindView(R.id.et_live_name)
    EditText etLiveName;
    @BindView(R.id.tv_creat_live)
    TextView tvCreatLive;
    @BindView(R.id.spn_live_type)
    MaterialSpinner spnLiveType;
    @BindView(R.id.spn_live_mode)
    MaterialSpinner spnLiveMode;
    @BindView(R.id.spn_live_direction)
    MaterialSpinner spnLiveDirection;
    @BindView(R.id.iv_icon)
    CircleImageView ivIcon;
    @BindView(R.id.tv_name)
    TextView tvName;
    List<String> live_type = new LinkedList<>(Arrays.asList("视频直播", "音频直播"));
    List<String> live_mode = new LinkedList<>(Arrays.asList("标清", "超清", "流畅"));
    List<String> live_direction = new LinkedList<>(Arrays.asList("竖屏直播","横屏直播"));
    int live_type_pos = 0;
    int live_mode_pos = 0;
    int live_direction_pos = 0;
    String anyrtcId = HybirdApplication.getAnyRTCId();

    @Override
    public int getLayoutId() {
        return R.layout.activity_pre_start_live;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tvName.setText(HybirdApplication.getNickName());
        etLiveName.setText(RTMPCHttpKit.getRandomString(5));
        etLiveName.setSelection(etLiveName.getText().length());
        spnLiveMode.setItems(live_mode);
        spnLiveType.setItems(live_type);
        spnLiveDirection.setItems(live_direction);
        spnLiveMode.setOnItemSelectedListener(this);
        spnLiveType.setOnItemSelectedListener(this);
        spnLiveDirection.setOnItemSelectedListener(this);
    }



    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
        switch (view.getId()) {
            case R.id.spn_live_mode:
                live_mode_pos = position;
                break;
            case R.id.spn_live_type:
                live_type_pos = position;
                break;
            case R.id.spn_live_direction:
                live_direction_pos = position;
                break;
        }
    }

    public void startLive() {
        if (TextUtils.isEmpty(etLiveName.getText().toString().trim())) {
            ToastUtil.show("直播名称不能为空");
            return;
        }
        Bundle bundle = new Bundle();
        LiveBean liveBean = new LiveBean();
        liveBean.setmLiveTopic(etLiveName.getText().toString());
        liveBean.setmAnyrtcId(anyrtcId);
        liveBean.setmPushUrl(String.format(Constans.RTMP_PUSH_URL, anyrtcId));
        liveBean.setIsLiveLandscape(live_direction_pos == 0 ? 0 : 1);
        liveBean.setLiveMode(live_type_pos);
        liveBean.setmHostName(tvName.getText().toString());
        bundle.putSerializable(LIVEBEAN, liveBean);
        bundle.putString(Constans.LIVEINFO, getLiveInfo());
        bundle.putString(Constans.USERINFO, getUserData());
        if (live_type_pos == 0) {
            startAnimActivity(HosterActivity.class, bundle);
        } else {
            startAnimActivity(AudioHosterActivity.class, bundle);
        }
    }


    public String getUserData() {
        JSONObject user = new JSONObject();
        try {
            user.put("isHost", 1);
            user.put("userId", "host");
            user.put("nickName", tvName.getText().toString());
            user.put("headUrl", "www.baidu.com");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user.toString();
    }

    public String getLiveInfo() {
        JSONObject liveInfo = new JSONObject();

        try {
            liveInfo.put("hosterId", "hostID");
            liveInfo.put("rtmpUrl", String.format(Constans.RTMP_PULL_URL, anyrtcId));
            liveInfo.put("hlsUrl", String.format(Constans.HLS_URL, anyrtcId));
            liveInfo.put("liveTopic", etLiveName.getText().toString());
            liveInfo.put("anyrtcId", anyrtcId);
            liveInfo.put("isLiveLandscape", live_direction_pos == 0 ? 0 : 1);
            liveInfo.put("isAudioLive", live_type_pos == 0 ? 0 : 1);
            liveInfo.put("hosterName", tvName.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return liveInfo.toString();
    }



    @OnClick({R.id.tv_creat_live, R.id.tv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_creat_live:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AndPermission.with(this)
                            .requestCode(REQUECT_CODE_CAMARE)
                            .permission(Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO)
                            .callback(new PermissionListener() {
                                @Override
                                public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                    startLive();
                                }

                                @Override
                                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                    if (deniedPermissions.size() == 2) {
                                        PermissionsCheckUtil.showMissingPermissionDialog(PreStartLiveActivity.this, "请先开启录音和相机权限");
                                        return;
                                    }
                                    for (int i = 0; i < deniedPermissions.size(); i++) {
                                        if (deniedPermissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
                                            PermissionsCheckUtil.showMissingPermissionDialog(PreStartLiveActivity.this, "请先开启录音权限");
                                        } else {
                                            PermissionsCheckUtil.showMissingPermissionDialog(PreStartLiveActivity.this, "请先开启相机权限");
                                        }
                                    }
                                }
                            }).start();
                } else {
                    startLive();
                }

                break;
            case R.id.tv_back:
                finishAnimActivity();
                break;
        }
    }
}

