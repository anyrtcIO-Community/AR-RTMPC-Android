package org.ar.rtmpc_hybrid;

/**
 * Created by Skyline on 2017/11/20.
 */
public enum ARRtmpcVideoHorizontal {
    left(0),
    center(1),
    right(2);

    public final int gravity;

    ARRtmpcVideoHorizontal(int gravity) {
        this.gravity = gravity;
    }
}
