package org.anyrtc.model;

/**
 * Created by liuxiaozhong on 2017-09-22.
 */

public class MessageBean {
    public int type;
    public String name;
    public String content;
    public String iconUrl;

    public MessageBean(int type, String name, String content, String iconUrl) {
        this.type = type;
        this.name = name;
        this.content = content;
        this.iconUrl = iconUrl;
    }
}
