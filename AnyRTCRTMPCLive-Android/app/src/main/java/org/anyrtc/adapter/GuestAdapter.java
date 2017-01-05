package org.anyrtc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.anyrtc.live_line.R;
import org.anyrtc.utils.AudioItemBean;
import org.anyrtc.widgets.DiffuseView;

import java.util.List;

/**
 * Created by Skyline on 2016/12/23.
 */

public class GuestAdapter extends BaseAdapter {
    private List<AudioItemBean> chatMessageList;
    private Context context;

    public GuestAdapter(List<AudioItemBean> chatMessageList, Context context) {
        this.chatMessageList = chatMessageList;
        this.context = context;
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
        final GuestAdapter.ViewHolder holder;
        final AudioItemBean item = chatMessageList.get(position);
        if (convertView == null) {
            holder = new GuestAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_guest, null);
            holder.wave_view = (DiffuseView) convertView.findViewById(R.id.wave_view);
            convertView.setTag(holder);
        } else {
            holder = (GuestAdapter.ViewHolder) convertView.getTag();
        }

        if(item.getmAudioLevel() == 0) {
            if(holder.wave_view.isDiffuse()) {
                holder.wave_view.stop();
            }
        } else {
            if(!holder.wave_view.isDiffuse()) {
                holder.wave_view.start();
            }
        }

        if(!holder.wave_view.isDiffuse()) {
            holder.wave_view.start();
        }

        return convertView;
    }

    class ViewHolder {
        DiffuseView wave_view;
    }
}
