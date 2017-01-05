package org.anyrtc.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.anyrtc.live_line.R;
import org.anyrtc.utils.AudioItemBean;
import org.anyrtc.widgets.DiffuseView;

import java.util.List;

/**
 * Created by Skyline on 2016/12/20.
 */

public class LiveAudioAdapter extends BaseAdapter {
    List<AudioItemBean> chatMessageList;
    Context context;
    CloseAudioHelper mCloseAudioHelper;
    boolean mIsHost;
    String mHostId;

    public LiveAudioAdapter(List<AudioItemBean> chatMessageList, Context context, CloseAudioHelper closeAudioHelper, boolean isHost, String hostid) {
        this.chatMessageList = chatMessageList;
        this.context = context;
        this.mCloseAudioHelper = closeAudioHelper;
        this.mIsHost = isHost;
        this.mHostId = hostid;
    }

    @Override
    public int getCount() {
        return chatMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final AudioItemBean item = chatMessageList.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_audio_chat, null);
            holder.txtChatName = (TextView) convertView.findViewById(R.id.txt_audio_name);
            holder.txtChatLevel = (TextView) convertView.findViewById(R.id.txt_audio_level);
            holder.wl_content = (DiffuseView) convertView.findViewById(R.id.wl_content);
            holder.imgClose = (ImageView) convertView.findViewById(R.id.img_close);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(mIsHost) {
            holder.imgClose.setVisibility(View.VISIBLE);
        } else if(item.getmStrCustomid().equals(mHostId)) {
            holder.imgClose.setVisibility(View.VISIBLE);
        } else {
            holder.imgClose.setVisibility(View.GONE);
        }

        holder.txtChatLevel.setTextColor(R.color.yellow);
        holder.txtChatLevel.setText(item.getmAudioLevel() + "");
        holder.txtChatName.setText(item.getmStrCustomid());

        holder.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseAudioHelper.onCloseAudioChat(item.getmStrPeerId());
            }
        });

        if(item.getmAudioLevel() == 0) {
            holder.wl_content.stop();
        } else {
            if(!holder.wl_content.isDiffuse()) {
                holder.wl_content.start();
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView txtChatName;
        TextView txtChatLevel;
//        WaveLayout wl_content;
        DiffuseView wl_content;
        ImageView imgClose;
    }

    public interface CloseAudioHelper {
        public void onCloseAudioChat(String strLivePeerid);
    }
}
