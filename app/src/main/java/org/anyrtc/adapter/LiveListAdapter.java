package org.anyrtc.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.anyrtc.live_line.R;
import org.anyrtc.model.LiveBean;


/**
 * Created by liuxiaozhong on 2017-09-14.
 */

public class LiveListAdapter extends BaseQuickAdapter<LiveBean,BaseViewHolder> {

    public LiveListAdapter() {
        super(R.layout.item_live);
    }

    @Override
    protected void convert(BaseViewHolder helper, LiveBean item) {
        helper.setText(R.id.tv_live_name,item.mLiveTopic);
        helper.setText(R.id.tv_room_id,item.getmAnyrtcId());
        helper.setText(R.id.tv_host_name,item.getmHostName());
        helper.setText(R.id.tv_num,item.getmMemberNum()+"");
        if(item.isAudioLive==1) {
            helper.setImageResource(R.id.img_live_type, R.drawable.audio_living);
        } else {
            helper.setImageResource(R.id.img_live_type, R.drawable.video_living);
        }
    }
}
