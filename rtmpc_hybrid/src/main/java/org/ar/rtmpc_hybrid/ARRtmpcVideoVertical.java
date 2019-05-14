package org.ar.rtmpc_hybrid;

/**
 * Created by Skyline on 2017/11/20.
 */
public enum ARRtmpcVideoVertical {
    top(0),
    center(1),
    bottom(2);

    public final int gravity;

    ARRtmpcVideoVertical(int gravity) {
        this.gravity = gravity;
    }
}
