package world.gregs.voidps.tools.icon;/* Class348_Sub40_Sub30 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub30 extends Class348_Sub40 {
    static int anInt9384;
    private int anInt9386;
    private int anInt9389;
    private int anInt9390 = 0;
    private int anInt9392;
    static int anInt9393;
    static int anInt9394;
    private int anInt9396;
    static int anInt9397;
    private int anInt9398 = 0;
    static int anInt9399 = -1;
    private int anInt9400;
    private int anInt9401;
    private int anInt9402 = 0;

    public Class348_Sub40_Sub30() {
        super(1, false);
    }

    final int[][] method3047(int i, int i_8_) {
        anInt9393++;
        int[][] is = this.aClass322_7033.method2557(-105, i);
        if (this.aClass322_7033.aBoolean4035) {
            int[][] is_9_ = this.method3039((byte) 50, i, 0);
            int[] is_10_ = is_9_[0];
            int[] is_11_ = is_9_[1];
            int[] is_12_ = is_9_[2];
            int[] is_13_ = is[0];
            int[] is_14_ = is[1];
            int[] is_15_ = is[2];
            for (int i_16_ = 0; (i_16_ < Class348_Sub40_Sub6.anInt9139); i_16_++) {
                method3129(is_10_[i_16_], is_11_[i_16_], (byte) 82, is_12_[i_16_]);
                anInt9401 += anInt9390;
                anInt9400 += anInt9402;
                anInt9389 += anInt9398;
                for (/**/; anInt9400 < 0; anInt9400 += 4096) {
                    /* empty */
                }
                if (anInt9401 < 0) anInt9401 = 0;
                for (/**/; anInt9400 > 4096; anInt9400 -= 4096) {
                    /* empty */
                }
                if (anInt9389 < 0) anInt9389 = 0;
                if (anInt9401 > 4096) anInt9401 = 4096;
                if (anInt9389 > 4096) anInt9389 = 4096;
                method3130(anInt9389, anInt9400, (byte) -120, anInt9401);
                is_13_[i_16_] = anInt9386;
                is_14_[i_16_] = anInt9396;
                is_15_[i_16_] = anInt9392;
            }
        }
        if (i_8_ != -1564599039) anInt9399 = 25;
        return is;
    }

    private final void method3129(int i, int i_17_, byte i_18_, int i_19_) {
        anInt9394++;
        int i_20_ = Math.max(i_17_, i);
        if (i_18_ == 82) {
            i_20_ = Math.max(i_19_, i_20_);
            int i_21_ = Math.min(i, i_17_);
            i_21_ = Math.min(i_19_, i_21_);
            anInt9389 = (i_20_ + i_21_) / 2;
            int i_22_ = i_20_ - i_21_;
            if (i_22_ > 0) {
                int i_23_ = (i_20_ + -i << 12) / i_22_;
                int i_24_ = (i_20_ + -i_17_ << 12) / i_22_;
                int i_25_ = (-i_19_ + i_20_ << 12) / i_22_;
                if (i == i_20_) anInt9400 = (i_17_ != i_21_ ? 4096 + -i_24_ : i_25_ + 20480);
                else if (i_17_ != i_20_) anInt9400 = i != i_21_ ? -i_23_ + 20480 : 12288 - -i_24_;
                else anInt9400 = (i_21_ == i_19_ ? 4096 + i_23_ : -i_25_ + 12288);
                anInt9400 /= 6;
            } else anInt9400 = 0;
            if (anInt9389 > 0 && anInt9389 < 4096) anInt9401 = (i_22_ << 12) / (anInt9389 > 2048 ? 8192 - anInt9389 * 2 : anInt9389 * 2);
            else anInt9401 = 0;
        }
    }

    private final void method3130(int i, int i_26_, byte i_27_, int i_28_) {
        anInt9397++;
        int i_29_ = 31 / ((i_27_ - -74) / 40);
        int i_30_ = (i > 2048 ? i_28_ + (i - (i * i_28_ >> 12)) : i * (4096 - -i_28_) >> 12);
        if (i_30_ <= 0) anInt9386 = anInt9396 = anInt9392 = i;
        else {
            i_26_ *= 6;
            int i_31_ = -i_30_ + i - -i;
            int i_32_ = (-i_31_ + i_30_ << 12) / i_30_;
            int i_33_ = i_26_ >> 12;
            int i_34_ = i_26_ + -(i_33_ << 12);
            int i_35_ = i_30_;
            i_35_ = i_35_ * i_32_ >> 12;
            i_35_ = i_35_ * i_34_ >> 12;
            int i_36_ = i_35_ + i_31_;
            int i_37_ = i_30_ - i_35_;
            int i_38_ = i_33_;
            while_208_:
            do {
                while_207_:
                do {
                    while_206_:
                    do {
                        while_205_:
                        do {
                            do {
                                if (i_38_ == 0) {
                                    anInt9396 = i_36_;
                                    anInt9386 = i_30_;
                                    anInt9392 = i_31_;
                                    return;
                                } else if (i_38_ != 1) {
                                    if (i_38_ != 2) {
                                        if (i_38_ != 3) {
                                            if (i_38_ != 4) {
                                                if (i_38_ != 5) break while_208_;
                                            } else break while_206_;
                                            break while_207_;
                                        }
                                    } else break;
                                    break while_205_;
                                }
                                anInt9392 = i_31_;
                                anInt9386 = i_37_;
                                anInt9396 = i_30_;
                                return;
                            } while (false);
                            anInt9392 = i_36_;
                            anInt9386 = i_31_;
                            anInt9396 = i_30_;
                            return;
                        } while (false);
                        anInt9396 = i_37_;
                        anInt9386 = i_31_;
                        anInt9392 = i_30_;
                        return;
                    } while (false);
                    anInt9396 = i_31_;
                    anInt9386 = i_36_;
                    anInt9392 = i_30_;
                    return;
                } while (false);
                anInt9386 = i_30_;
                anInt9392 = i_37_;
                anInt9396 = i_31_;
            } while (false);
        }
    }

    final void method3049(Packet packet, int i, int i_39_) {
        while_209_:
        do {
            try {
                anInt9384++;
                if (i_39_ == 31015) {
                    int i_40_ = i;
                    do {
                        if (i_40_ == 0) {
                            anInt9402 = packet.readShort(13638);
                            return;
                        } else if (i_40_ != 1) {
                            if (i_40_ == 2) break;
                            break while_209_;
                        }
                        anInt9390 = (packet.readByte(-83) << 12) / 100;
                        return;
                    } while (false);
                    anInt9398 = (packet.readByte(i_39_ + -31101) << 12) / 100;
                    break;
                }
                break;
            } catch (RuntimeException runtimeexception) {
                throw Class348_Sub17.method2929(runtimeexception, ("vj.F(" + (packet != null ? "{...}" : "null") + ',' + i + ',' + i_39_ + ')'));
            }
        } while (false);
    }
}
