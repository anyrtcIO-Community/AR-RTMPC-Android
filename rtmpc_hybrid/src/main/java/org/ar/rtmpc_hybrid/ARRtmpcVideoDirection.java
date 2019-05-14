package org.ar.rtmpc_hybrid;

/**
 * Created by Skyline on 2017/11/20.
 */
public enum ARRtmpcVideoDirection {
    horizontal(0),
    vertical(1);

    public final int direction;

    ARRtmpcVideoDirection(int direction) {
        this.direction = direction;
    }
}
