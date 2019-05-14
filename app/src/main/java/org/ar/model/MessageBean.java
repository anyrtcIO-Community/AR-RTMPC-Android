package org.ar.model;

/**
 * Created by liuxiaozhong on 2017-09-22.
 */

public class MessageBean {
    public final static int VIDEO = 1;
    public final static int AUDIO = 0
            ;
    public int type;//0 video 1 audio
    public String name;
    public String content;

    public MessageBean(int type, String name, String content) {
        this.type = type;
        this.name = name;
        this.content = content;
    }
}
