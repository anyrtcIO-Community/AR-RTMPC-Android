package org.anyrtc.live_line;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.anyrtc.utils.RTMPCHttpSDK;
import org.anyrtc.utils.RTMPUrlHelper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 直播信息输入页面
 */
public class PreStartLiveActivity extends AppCompatActivity {

    // UI references.
    private EditText mLiveTopicView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_start_live);
        mLiveTopicView = (EditText) findViewById(R.id.edit_live_topic);

        Button mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startLive();
            }
        });
    }

    private void startLive() {
        String topic = mLiveTopicView.getText().toString().trim();
        if (topic.length() == 0) {
            return;
        } else {
            String anyrtcId = RTMPCHttpSDK.getRandomString(12);
            String rtmpPushUrl = String.format(RTMPUrlHelper.RTMP_PUSH_URL, anyrtcId);
            String rtmpPullUrl = String.format(RTMPUrlHelper.RTMP_PULL_URL, anyrtcId);
            String hlsUrl = String.format(RTMPUrlHelper.HLS_URL, anyrtcId);
            JSONObject item = new JSONObject();
            try {
                item.put("hosterId", "hostID");
                item.put("rtmp_url", rtmpPullUrl);
                item.put("hls_url", hlsUrl);
                item.put("topic", topic);
                item.put("anyrtcId", anyrtcId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Bundle bundle = new Bundle();
            bundle.putString("hosterId", "hostID");
            bundle.putString("rtmp_url", rtmpPushUrl);
            bundle.putString("hls_url", hlsUrl);
            bundle.putString("topic", topic);
            bundle.putString("andyrtcId", anyrtcId);
            bundle.putString("userData", item.toString());
            Intent intent = new Intent(this, HosterActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }
}

