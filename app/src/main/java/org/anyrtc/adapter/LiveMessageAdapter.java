package org.anyrtc.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.anyrtc.live_line.R;
import org.anyrtc.model.MessageBean;

/**
 * Created by liuxiaozhong on 2017-09-22.
 */

public class LiveMessageAdapter extends BaseQuickAdapter<MessageBean, BaseViewHolder> {
    public LiveMessageAdapter() {
        super(R.layout.item_live_chat);
    }

    @Override
    protected void convert(BaseViewHolder helper, MessageBean item) {
        helper.setText(R.id.txt_chat_name, item.name+":");
        helper.setText(R.id.txt_chat_message, item.content);
        if (item.type == 0) {
            helper.setTextColor(R.id.txt_chat_name, Color.parseColor("#4680FA"));
        } else {
            helper.setTextColor(R.id.txt_chat_name, Color.parseColor("#ff0000"));
        }
    }
}
