package org.ar.common.enums;

/**
 *
 * @author Skyline
 * @date 2017/11/10
 */

public class ARVideoCommon {
    public enum ARVideoLayout {
        AR_V_1X3(0),
        AR_V_3X3_auto(1);

        public final int level;

        private ARVideoLayout(int level) {
            this.level = level;
        }
    }

    public enum ARVideoOrientation {
        /**
         * 竖屏
         */
        Portrait(0),
        /**
         * 横屏
         */
        Landscape(1);

        public final int orientation;

        private ARVideoOrientation(int orientation) {
            this.orientation = orientation;
        }
    }

    public enum ARVideoProfile {
        /**
         * 120x160
         */
        ARVideoProfile120x160(0),
        /**
         * 120x120
         */
        ARVideoProfile120x120(1),
        /**
         * 144x192
         */
        ARVideoProfile144x192(2),
        /**
         * 144x176
         */
        ARVideoProfile144x176(3),

        //180P
        /**
         * 180x320
         */
        ARVideoProfile180x320(4),
        /**
         * 180x180
         */
        ARVideoProfile180x180(5),
        /**
         * 180x240
         */
        ARVideoProfile180x240(6),

        //240P
        /**
         * 240x320
         */
        ARVideoProfile240x320(7),
        /**
         * 240x240
         */
        ARVideoProfile240x240(8),
        /**
         * 240x424
         */
        ARVideoProfile240x424(9),
        /**
         * 288x352
         */
        ARVideoProfile288x352(10),

        //360P
        /**
         * 360x640
         */
        ARVideoProfile360x640(11),
        /**
         * 360x360
         */
        ARVideoProfile360x360(12),
        /**
         * 360x480
         */
        ARVideoProfile360x480(13),

        //480P
        /**
         * 480x640
         */
        ARVideoProfile480x640(14),
        /**
         * 480x480
         */
        ARVideoProfile480x480(15),
        /**
         * 480x848
         */
        ARVideoProfile480x848(16),
        //720P
        /**
         * 540x960
         */
        ARVideoProfile540x960(17),
        /**
         * 720x960
         */
        ARVideoProfile720x960(18),
        /**
         * 720x1280
         */
        ARVideoProfile720x1280(19),

        //1080P
        /**
         * 1080x1920
         */
        ARVideoProfile1080x1920(20),

        //1440P
        /**
         * 1440x2560
         */
        ARVideoProfile1440x2560(21),

        //4k
        /**
         * 2160×3840
         */
        ARVideoProfile2160x3840(22);

        public final int level;

        private ARVideoProfile(int level) {
            this.level = level;
        }
    }

    public enum ARVideoFrameRate {
        /**
         * 1 fps.
         */
        ARVideoFrameRateFps1(1),
        /**
         * 7 fps.
         */
        ARVideoFrameRateFps7(2),
        /**
         * 10 fps.
         */
        ARVideoFrameRateFps10(3),
        /**
         * 15 fps.
         */
        ARVideoFrameRateFps15(4),
        /**
         * 20 fps.
         */
        ARVideoFrameRateFps20(5),
        /**
         * 24 fps.
         */
        ARVideoFrameRateFps24(6),
        /**
         * 30 fps.
         */
        ARVideoFrameRateFps30(7),
        /**
         * 60 fps.
         */
        ARVideoFrameRateFps60(8);
        public final int level;

        private ARVideoFrameRate(int level) {
            this.level = level;
        }
    }

    public enum ARMediaType {
        /**
         * 视频
         */
        Video(0),
        /**
         * 音频
         */
        Audio(1);
        public final int type;

        private ARMediaType(int type) {
            this.type = type;
        }
    }
}
