package org.anyrtc.hoster;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.BaseActivity;
import org.anyrtc.HybirdApplication;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LiveBean;
import org.anyrtc.rtmpc_hybrid.RTMPCHttpKit;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.MD5;
import org.anyrtc.utils.NetHttp;
import org.anyrtc.utils.PermissionsCheckUtil;
import org.anyrtc.utils.ResultListener;
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
    private void getRTMPCUrl(){
        showProgressDialog();
        String random=(int)((Math.random()*9+1)*100000)+"";
        long timestamp=System.currentTimeMillis();
        StringRequest request=new StringRequest("https://vdn.anyrtc.cc/oauth/anyapi/v1/vdnUrlSign/getAppVdnUrl", RequestMethod.POST);
        request.add("appid",Constans.APPID);
        request.add("stream",anyrtcId);
        request.add("random",random);
        request.add("signature", MD5.getMD5(Constans.APPID+String.valueOf(timestamp)+Constans.APP_V_TOKEN+random));
        request.add("timestamp",timestamp);
        request.add("appBundleIdPkgName",Constans.APP_PACKAGE);
        NetHttp.getInstance().request(1, request, new ResultListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                Log.d("rtmpcUrl",response.get());
                hiddenProgressDialog();
                try {
                    JSONObject jsonObject=new JSONObject(response.get());
                    int code=jsonObject.getInt("code");
                    if (code==200){
                        String pushUrl=jsonObject.getString("push_url");
                        String pullUrl=jsonObject.getString("pull_url");
                        String hlsURL=jsonObject.getString("hls_url");
                        if (!TextUtils.isEmpty(pushUrl)){
                            startLive(pushUrl,pullUrl,hlsURL);
                        }else {
                            ToastUtil.show("获取推流地址失败");
                        }
                    }else {
                        String result=jsonObject.getString("message");
                        ToastUtil.show("Error:"+result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtil.show("解析数据失败");
                }

            }

            @Override
            public void onFailed(int what, Response<String> response) {
            hiddenProgressDialog();
            }
        });

    }
    public void startLive(String pushUrl,String pullUrl,String hlsUrl) {
        if (TextUtils.isEmpty(etLiveName.getText().toString().trim())) {
            ToastUtil.show("直播名称不能为空");
            return;
        }
        Bundle bundle = new Bundle();
        LiveBean liveBean = new LiveBean();
        liveBean.setmLiveTopic(etLiveName.getText().toString());
        liveBean.setmAnyrtcId(anyrtcId);
        liveBean.setmPushUrl(pushUrl);
        liveBean.setmRtmpPullUrl(pullUrl);
        liveBean.setIsLiveLandscape(live_direction_pos == 0 ? 0 : 1);
        liveBean.setLiveMode(live_type_pos);
        liveBean.setmHostName(tvName.getText().toString());
        bundle.putSerializable(LIVEBEAN, liveBean);
        bundle.putString(Constans.LIVEINFO, getLiveInfo(pullUrl,hlsUrl));
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

    public String getLiveInfo(String pullUrl,String hlsUrl) {
        JSONObject liveInfo = new JSONObject();

        try {
            liveInfo.put("hosterId", "hostID");
            liveInfo.put("rtmpUrl", pullUrl);
            liveInfo.put("hlsUrl", hlsUrl);
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
                                  getRTMPCUrl();
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
                    getRTMPCUrl();
                }

                break;
            case R.id.tv_back:
                finishAnimActivity();
                break;
        }
    }
}

