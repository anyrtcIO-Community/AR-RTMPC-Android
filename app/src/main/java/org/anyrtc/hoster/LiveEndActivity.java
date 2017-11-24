package org.anyrtc.hoster;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.anyrtc.BaseActivity;
import org.anyrtc.live_line.R;
import org.anyrtc.utils.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class LiveEndActivity extends BaseActivity {


    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.ll_call)
    LinearLayout llCall;

    @Override
    public int getLayoutId() {
        return R.layout.activity_live_end;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String name = bundle.getString("HostName");
            String livetime = bundle.getString("livetime");
            tvTime.setText(livetime);
            tvName.setText(name+"");
        }

    }


    @OnClick({R.id.ll_back, R.id.ll_call, R.id.ll_qq})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                finishAnimActivity();
                break;
            case R.id.ll_call:
                Uri uri = Uri.parse("tel:021-65650071");
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
                break;
            case R.id.ll_qq:
                boolean result = joinQQGroup("mIxzXC8yPld6sSRRQet5zf38WPiUS1Ea");
                if (result==false){
                    ToastUtil.show("您未安装QQ或版本不支持~");
                }
                break;
        }
    }

    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
