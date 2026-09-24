package world.gregs.voidps.tools.icon;/* Class348_Sub25 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub25 {
    int anInt6883;
    boolean aBoolean6882;
    private final int anInt6880;
    private static int[] anIntArray6881;
    private int[] anIntArray6884;

    public static void method2996() {
        anIntArray6881 = null;
    }

    final int[] method2997() {
        return anIntArray6884;
    }

    Class348_Sub25(int i, int i_11_, int[] is, boolean bool) {
        this.anInt6883 = i;
        anInt6880 = i_11_;
        anIntArray6884 = is;
        if (bool) {
            int[] is_12_ = new int[anInt6880];
            int[] is_13_ = new int[anInt6880];
            int[] is_14_ = new int[anInt6880];
            int[] is_15_ = new int[anInt6880];
            if (anIntArray6881 == null || anIntArray6881.length != anIntArray6884.length) anIntArray6881 = new int[anIntArray6884.length];
            int i_16_ = anInt6880;
            int i_17_ = anInt6880;
            int i_18_ = i_16_ - 1;
            int i_19_ = i_17_ - 1;
            int i_20_ = i_16_ * i_17_;
            int i_22_;
            int i_21_ = i_22_ = i_16_;
            for (int i_23_ = 2; i_23_ >= 0; i_23_--) {
                for (int i_24_ = i_18_; i_24_ >= 0; i_24_--) {
                    int i_25_ = anIntArray6884[--i_22_];
                    is_12_[i_24_] += i_25_ >> 24 & 0xff;
                    is_13_[i_24_] += i_25_ >> 16 & 0xff;
                    is_14_[i_24_] += i_25_ >> 8 & 0xff;
                    is_15_[i_24_] += i_25_ & 0xff;
                }
                if (i_22_ == 0) i_22_ = i_20_;
            }
            int i_26_ = i_20_;
            for (int i_27_ = i_19_; i_27_ >= 0; i_27_--) {
                int i_28_ = 1;
                int i_29_ = 1;
                int i_31_;
                int i_32_;
                int i_33_;
                int i_30_ = i_31_ = i_32_ = i_33_ = 0;
                for (int i_34_ = 2; i_34_ >= 0; i_34_--) {
                    i_29_--;
                    i_30_ += is_12_[i_29_];
                    i_31_ += is_13_[i_29_];
                    i_33_ += is_14_[i_29_];
                    i_32_ += is_15_[i_29_];
                    if (i_29_ == 0) i_29_ = i_16_;
                }
                for (int i_35_ = i_18_; i_35_ >= 0; i_35_--) {
                    i_29_--;
                    i_28_--;
                    int i_36_ = i_30_ / 9;
                    int i_37_ = i_31_ / 9;
                    int i_38_ = i_33_ / 9;
                    int i_39_ = i_32_ / 9;
                    anIntArray6881[--i_26_] = i_36_ << 24 | i_37_ << 16 | i_38_ << 8 | i_39_;
                    i_30_ += is_12_[i_29_] - is_12_[i_28_];
                    i_31_ += is_13_[i_29_] - is_13_[i_28_];
                    i_32_ += is_15_[i_29_] - is_15_[i_28_];
                    i_33_ += is_14_[i_29_] - is_14_[i_28_];
                    if (i_29_ == 0) i_29_ = i_16_;
                    if (i_28_ == 0) i_28_ = i_16_;
                }
                for (int i_40_ = i_18_; i_40_ >= 0; i_40_--) {
                    int i_41_ = anIntArray6884[--i_22_];
                    int i_42_ = anIntArray6884[--i_21_];
                    is_12_[i_40_] += (i_41_ >> 24 & 0xff) - (i_42_ >> 24 & 0xff);
                    is_13_[i_40_] += (i_41_ >> 16 & 0xff) - (i_42_ >> 16 & 0xff);
                    is_14_[i_40_] += (i_41_ >> 8 & 0xff) - (i_42_ >> 8 & 0xff);
                    is_15_[i_40_] += (i_41_ & 0xff) - (i_42_ & 0xff);
                }
                if (i_22_ == 0) i_22_ = i_20_;
                if (i_21_ == 0) i_21_ = i_20_;
            }
            int[] is_43_ = anIntArray6884;
            anIntArray6884 = anIntArray6881;
            anIntArray6881 = is_43_;
        }
    }
}
