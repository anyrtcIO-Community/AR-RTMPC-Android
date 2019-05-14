package org.ar.adapter;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.ar.rtmpc.R;
import org.ar.model.LiveBean;


/**
 * Created by liuxiaozhong on 2017-09-14.
 */

public class LiveListAdapter extends BaseQuickAdapter<LiveBean,BaseViewHolder> {

    public LiveListAdapter() {
        super(R.layout.item_live);
    }

    @Override
    protected void convert(BaseViewHolder helper, LiveBean item) {
        helper.setText(R.id.tv_name,item.getmLiveTopic());
        helper.setText(R.id.tv_num,item.getmMemberNum()+"");
        TextView tvLiveType = helper.getView(R.id.tv_live_type);

        Drawable imgVideo = helper.itemView.getContext().getResources().getDrawable(
                R.drawable.img_video);
        Drawable imgAdudio = helper.itemView.getContext().getResources().getDrawable(
                R.drawable.img_audio);
        if(item.isAudioLive==1) {
            tvLiveType.setCompoundDrawablesWithIntrinsicBounds(imgAdudio,
                    null, null, null);
            tvLiveType.setText("音频直播");
        } else {
            tvLiveType.setCompoundDrawablesWithIntrinsicBounds(imgVideo,
                    null, null, null);
            tvLiveType.setText("视频");
        }
    }
}
