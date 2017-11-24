package org.anyrtc.model;

/**
 * Created by liuxiaozhong on 2017/9/24.
 */

public class LineBean {
    public String peerId;
    public String name;
    public boolean isSelf;
    public boolean startAnim;

    public LineBean(String peerId, String name,boolean isSelf) {
        this.peerId = peerId;
        this.name = name;
        this.isSelf=isSelf;
    }

    public boolean isStartAnim() {
        return startAnim;
    }

    public void setStartAnim(boolean startAnim) {
        this.startAnim = startAnim;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }
}
