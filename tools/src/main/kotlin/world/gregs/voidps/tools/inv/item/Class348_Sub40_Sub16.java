package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub16 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub16 extends Class348_Sub40 {
    static int anInt9222;
    static int anInt9223;
    static int anInt9224;
    private int anInt9226 = 6;
    static int anInt9227;

    final void method3049(Class348_Sub49 class348_sub49, int i, int i_0_) {
        if (i_0_ != 31015) anInt9226 = -83;
        int i_1_ = i;
        do {
            if (i_1_ == 0) {
                anInt9226 = class348_sub49.readUnsignedByte(255);
                break;
            } else if (i_1_ != 1) break;
            this.aBoolean7045 = class348_sub49.readUnsignedByte(255) == 1;
        } while (false);
        anInt9227++;
    }

    final int[][] method3047(int i, int i_2_) {
        if (i_2_ != -1564599039) method3089(-88);
        anInt9223++;
        int[][] is = this.aClass322_7033.method2557(i_2_ ^ 0x5d41e2a7, i);
        while_168_:
        do {
            if (this.aClass322_7033.aBoolean4035) {
                int[][] is_3_ = this.method3039((byte) -51, i, 0);
                int[][] is_4_ = this.method3039((byte) -66, i, 1);
                int[] is_5_ = is[0];
                int[] is_6_ = is[1];
                int[] is_7_ = is[2];
                int[] is_8_ = is_3_[0];
                int[] is_9_ = is_3_[1];
                int[] is_10_ = is_3_[2];
                int[] is_11_ = is_4_[0];
                int[] is_12_ = is_4_[1];
                int[] is_13_ = is_4_[2];
                int i_14_ = anInt9226;
                while_167_:
                do {
                    while_166_:
                    do {
                        while_165_:
                        do {
                            while_164_:
                            do {
                                while_163_:
                                do {
                                    while_162_:
                                    do {
                                        while_161_:
                                        do {
                                            while_160_:
                                            do {
                                                while_159_:
                                                do {
                                                    do {
                                                        if (i_14_ == 1) {
                                                            for (i_14_ = 0; ((Class348_Sub40_Sub6.anInt9139) > i_14_); i_14_++) {
                                                                is_5_[i_14_] = ((is_11_[i_14_]) + (is_8_[i_14_]));
                                                                is_6_[i_14_] = ((is_9_[i_14_]) + (is_12_[i_14_]));
                                                                is_7_[i_14_] = ((is_10_[i_14_]) + (is_13_[i_14_]));
                                                            }
                                                            break while_168_;
                                                        } else if (i_14_ != 2) {
                                                            if (i_14_ != 3) {
                                                                if (i_14_ != 4) {
                                                                    if (i_14_ != 5) {
                                                                        if (i_14_ != 6) {
                                                                            if (i_14_ != 7) {
                                                                                if (i_14_ != 8) {
                                                                                    if (i_14_ != 9) {
                                                                                        if (i_14_ != 10) {
                                                                                            if (i_14_ != 11) {
                                                                                                if (i_14_ != 12) break while_168_;
                                                                                            } else break while_166_;
                                                                                            break while_167_;
                                                                                        }
                                                                                    } else break while_164_;
                                                                                    break while_165_;
                                                                                }
                                                                            } else break while_162_;
                                                                            break while_163_;
                                                                        }
                                                                    } else break while_160_;
                                                                    break while_161_;
                                                                }
                                                            } else break;
                                                            break while_159_;
                                                        }
                                                        for (i_14_ = 0; ((Class348_Sub40_Sub6.anInt9139) > i_14_); i_14_++) {
                                                            is_5_[i_14_] = (is_8_[i_14_] - (is_11_[i_14_]));
                                                            is_6_[i_14_] = (is_9_[i_14_] + -(is_12_[i_14_]));
                                                            is_7_[i_14_] = (-(is_13_[i_14_]) + (is_10_[i_14_]));
                                                        }
                                                        break while_168_;
                                                    } while (false);
                                                    for (i_14_ = 0; (i_14_ < (Class348_Sub40_Sub6.anInt9139)); i_14_++) {
                                                        is_5_[i_14_] = ((is_11_[i_14_] * is_8_[i_14_]) >> 12);
                                                        is_6_[i_14_] = ((is_12_[i_14_] * is_9_[i_14_]) >> 12);
                                                        is_7_[i_14_] = ((is_10_[i_14_] * (is_13_[i_14_])) >> 12);
                                                    }
                                                    break while_168_;
                                                } while (false);
                                                for (i_14_ = 0; (i_14_ < (Class348_Sub40_Sub6.anInt9139)); i_14_++) {
                                                    int i_15_ = is_11_[i_14_];
                                                    int i_16_ = is_13_[i_14_];
                                                    int i_17_ = is_12_[i_14_];
                                                    is_5_[i_14_] = (i_15_ != 0 ? ((is_8_[i_14_] << 12) / i_15_) : 4096);
                                                    is_6_[i_14_] = (i_17_ == 0 ? 4096 : ((is_9_[i_14_] << 12) / i_17_));
                                                    is_7_[i_14_] = (i_16_ == 0 ? 4096 : ((is_10_[i_14_] << 12) / i_16_));
                                                }
                                                break while_168_;
                                            } while (false);
                                            for (i_14_ = 0; (i_14_ < (Class348_Sub40_Sub6.anInt9139)); i_14_++) {
                                                is_5_[i_14_] = (4096 + -(((-is_11_[i_14_] + 4096) * (4096 - is_8_[i_14_])) >> 12));
                                                is_6_[i_14_] = (4096 + -(((4096 + -is_12_[i_14_]) * (4096 - is_9_[i_14_])) >> 12));
                                                is_7_[i_14_] = (4096 - (((-is_13_[i_14_] + 4096) * (4096 - is_10_[i_14_])) >> 12));
                                            }
                                            break while_168_;
                                        } while (false);
                                        for (i_14_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_14_); i_14_++) {
                                            int i_18_ = is_11_[i_14_];
                                            int i_19_ = is_13_[i_14_];
                                            int i_20_ = is_12_[i_14_];
                                            is_5_[i_14_] = ((i_18_ >= 2048) ? -(((4096 - is_8_[i_14_]) * (-i_18_ + 4096)) >> 11) + 4096 : (is_8_[i_14_] * i_18_ >> 11));
                                            is_6_[i_14_] = (i_20_ >= 2048 ? -(((-i_20_ + 4096) * (4096 - is_9_[i_14_])) >> 11) + 4096 : (i_20_ * is_9_[i_14_] >> 11));
                                            is_7_[i_14_] = (i_19_ < 2048 ? (i_19_ * is_10_[i_14_] >> 11) : -(((-is_10_[i_14_] + 4096) * (-i_19_ + 4096)) >> 11) + 4096);
                                        }
                                        break while_168_;
                                    } while (false);
                                    for (i_14_ = 0; Class348_Sub40_Sub6.anInt9139 > i_14_; i_14_++) {
                                        int i_21_ = is_10_[i_14_];
                                        int i_22_ = is_9_[i_14_];
                                        int i_23_ = is_8_[i_14_];
                                        is_5_[i_14_] = (i_23_ == 4096 ? 4096 : ((is_11_[i_14_] << 12) / (-i_23_ + 4096)));
                                        is_6_[i_14_] = (i_22_ != 4096 ? ((is_12_[i_14_] << 12) / (-i_22_ + 4096)) : 4096);
                                        is_7_[i_14_] = (i_21_ == 4096 ? 4096 : ((is_13_[i_14_] << 12) / (4096 - i_21_)));
                                    }
                                    break while_168_;
                                } while (false);
                                for (i_14_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_14_); i_14_++) {
                                    int i_24_ = is_9_[i_14_];
                                    int i_25_ = is_8_[i_14_];
                                    int i_26_ = is_10_[i_14_];
                                    is_5_[i_14_] = (i_25_ == 0 ? 0 : -((-is_11_[i_14_] + 4096 << 12) / i_25_) + 4096);
                                    is_6_[i_14_] = i_24_ != 0 ? -((4096 + -is_12_[i_14_] << 12) / i_24_) + 4096 : 0;
                                    is_7_[i_14_] = (i_26_ == 0 ? 0 : -((-is_13_[i_14_] + 4096 << 12) / i_26_) + 4096);
                                }
                                break while_168_;
                            } while (false);
                            for (i_14_ = 0; i_14_ < Class348_Sub40_Sub6.anInt9139; i_14_++) {
                                int i_27_ = is_9_[i_14_];
                                int i_28_ = is_12_[i_14_];
                                int i_29_ = is_13_[i_14_];
                                int i_30_ = is_11_[i_14_];
                                int i_31_ = is_10_[i_14_];
                                int i_32_ = is_8_[i_14_];
                                is_5_[i_14_] = (Math.min(i_30_, i_32_));
                                is_6_[i_14_] = (Math.min(i_28_, i_27_));
                                is_7_[i_14_] = Math.min(i_29_, i_31_);
                            }
                            break while_168_;
                        } while (false);
                        for (i_14_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_14_); i_14_++) {
                            int i_33_ = is_9_[i_14_];
                            int i_34_ = is_10_[i_14_];
                            int i_35_ = is_12_[i_14_];
                            int i_36_ = is_11_[i_14_];
                            int i_37_ = is_13_[i_14_];
                            int i_38_ = is_8_[i_14_];
                            is_5_[i_14_] = Math.max(i_36_, i_38_);
                            is_6_[i_14_] = (Math.max(i_33_, i_35_));
                            is_7_[i_14_] = (Math.max(i_37_, i_34_));
                        }
                        break while_168_;
                    } while (false);
                    for (i_14_ = 0; Class348_Sub40_Sub6.anInt9139 > i_14_; i_14_++) {
                        int i_39_ = is_13_[i_14_];
                        int i_40_ = is_9_[i_14_];
                        int i_41_ = is_10_[i_14_];
                        int i_42_ = is_11_[i_14_];
                        int i_43_ = is_8_[i_14_];
                        int i_44_ = is_12_[i_14_];
                        is_5_[i_14_] = (i_43_ > i_42_ ? -i_42_ + i_43_ : i_42_ - i_43_);
                        is_6_[i_14_] = (i_44_ >= i_40_ ? i_44_ + -i_40_ : -i_44_ + i_40_);
                        is_7_[i_14_] = i_41_ <= i_39_ ? i_39_ - i_41_ : -i_39_ + i_41_;
                    }
                    break while_168_;
                } while (false);
                for (i_14_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_14_); i_14_++) {
                    int i_45_ = is_8_[i_14_];
                    int i_46_ = is_10_[i_14_];
                    int i_47_ = is_13_[i_14_];
                    int i_48_ = is_11_[i_14_];
                    int i_49_ = is_9_[i_14_];
                    int i_50_ = is_12_[i_14_];
                    is_5_[i_14_] = -(i_45_ * i_48_ >> 11) + (i_48_ + i_45_);
                    is_6_[i_14_] = -(i_49_ * i_50_ >> 11) + (i_49_ + i_50_);
                    is_7_[i_14_] = i_47_ + i_46_ - (i_46_ * i_47_ >> 11);
                }
            }
        } while (false);
        return is;
    }

    public Class348_Sub40_Sub16() {
        super(2, false);
    }

    final int[] method3042(int i, int i_51_) {
        if (i_51_ != 255) method3047(50, -18);
        anInt9224++;
        int[] is = this.aClass191_7032.method1433(0, i);
        while_178_:
        do {
            if (this.aClass191_7032.aBoolean2570) {
                int[] is_52_ = this.method3048(i, 633706337, 0);
                int[] is_53_ = this.method3048(i, 633706337, 1);
                int i_54_ = anInt9226;
                while_177_:
                do {
                    while_176_:
                    do {
                        while_175_:
                        do {
                            while_174_:
                            do {
                                while_173_:
                                do {
                                    while_172_:
                                    do {
                                        while_171_:
                                        do {
                                            while_170_:
                                            do {
                                                while_169_:
                                                do {
                                                    do {
                                                        if (i_54_ == 1) {
                                                            for (i_54_ = 0; ((Class348_Sub40_Sub6.anInt9139) > i_54_); i_54_++)
                                                                is[i_54_] = ((is_52_[i_54_]) + (is_53_[i_54_]));
                                                            break while_178_;
                                                        } else if (i_54_ != 2) {
                                                            if (i_54_ != 3) {
                                                                if (i_54_ != 4) {
                                                                    if (i_54_ != 5) {
                                                                        if (i_54_ != 6) {
                                                                            if (i_54_ != 7) {
                                                                                if (i_54_ != 8) {
                                                                                    if (i_54_ != 9) {
                                                                                        if (i_54_ != 10) {
                                                                                            if (i_54_ != 11) {
                                                                                                if (i_54_ != 12) break while_178_;
                                                                                            } else break while_176_;
                                                                                            break while_177_;
                                                                                        }
                                                                                    } else break while_174_;
                                                                                    break while_175_;
                                                                                }
                                                                            } else break while_172_;
                                                                            break while_173_;
                                                                        }
                                                                    } else break while_170_;
                                                                    break while_171_;
                                                                }
                                                            } else break;
                                                            break while_169_;
                                                        }
                                                        for (i_54_ = 0; ((Class348_Sub40_Sub6.anInt9139) > i_54_); i_54_++)
                                                            is[i_54_] = ((is_52_[i_54_]) + -(is_53_[i_54_]));
                                                        break while_178_;
                                                    } while (false);
                                                    for (i_54_ = 0; ((Class348_Sub40_Sub6.anInt9139) > i_54_); i_54_++)
                                                        is[i_54_] = ((is_53_[i_54_] * (is_52_[i_54_])) >> 12);
                                                    break while_178_;
                                                } while (false);
                                                for (i_54_ = 0; ((Class348_Sub40_Sub6.anInt9139) > i_54_); i_54_++) {
                                                    int i_55_ = is_53_[i_54_];
                                                    is[i_54_] = (i_55_ != 0 ? ((is_52_[i_54_] << 12) / i_55_) : 4096);
                                                }
                                                break while_178_;
                                            } while (false);
                                            for (i_54_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_54_); i_54_++)
                                                is[i_54_] = -(((4096 - is_52_[i_54_]) * (4096 - is_53_[i_54_])) >> 12) + 4096;
                                            break while_178_;
                                        } while (false);
                                        for (i_54_ = 0; (i_54_ < Class348_Sub40_Sub6.anInt9139); i_54_++) {
                                            int i_56_ = is_53_[i_54_];
                                            is[i_54_] = (i_56_ >= 2048 ? (4096 + -(((-i_56_ + 4096) * (4096 - is_52_[i_54_])) >> 11)) : (i_56_ * is_52_[i_54_] >> 11));
                                        }
                                        break while_178_;
                                    } while (false);
                                    for (i_54_ = 0; i_54_ < Class348_Sub40_Sub6.anInt9139; i_54_++) {
                                        int i_57_ = is_52_[i_54_];
                                        is[i_54_] = (i_57_ != 4096 ? ((is_53_[i_54_] << 12) / (-i_57_ + 4096)) : 4096);
                                    }
                                    break while_178_;
                                } while (false);
                                for (i_54_ = 0; i_54_ < Class348_Sub40_Sub6.anInt9139; i_54_++) {
                                    int i_58_ = is_52_[i_54_];
                                    is[i_54_] = (i_58_ == 0 ? 0 : -((4096 - is_53_[i_54_] << 12) / i_58_) + 4096);
                                }
                                break while_178_;
                            } while (false);
                            for (i_54_ = 0; i_54_ < Class348_Sub40_Sub6.anInt9139; i_54_++) {
                                int i_59_ = is_53_[i_54_];
                                int i_60_ = is_52_[i_54_];
                                is[i_54_] = (Math.min(i_59_, i_60_));
                            }
                            break while_178_;
                        } while (false);
                        for (i_54_ = 0; Class348_Sub40_Sub6.anInt9139 > i_54_; i_54_++) {
                            int i_61_ = is_53_[i_54_];
                            int i_62_ = is_52_[i_54_];
                            is[i_54_] = Math.max(i_62_, i_61_);
                        }
                        break while_178_;
                    } while (false);
                    for (i_54_ = 0; i_54_ < Class348_Sub40_Sub6.anInt9139; i_54_++) {
                        int i_63_ = is_53_[i_54_];
                        int i_64_ = is_52_[i_54_];
                        is[i_54_] = (i_63_ >= i_64_ ? i_63_ + -i_64_ : i_64_ - i_63_);
                    }
                    break while_178_;
                } while (false);
                for (i_54_ = 0; Class348_Sub40_Sub6.anInt9139 > i_54_; i_54_++) {
                    int i_65_ = is_53_[i_54_];
                    int i_66_ = is_52_[i_54_];
                    is[i_54_] = i_66_ - -i_65_ - (i_66_ * i_65_ >> 11);
                }
            }
        } while (false);
        return is;
    }

    /* NOTE: method3089 is NOT in the genuine-methods list (0 JaCoCo hits)
     * for this renderer, so it is stubbed rather than pulling in
     * Class248/Class97/Class334/Class259's world-state fields to compile a
     * path that never executes here. */
    static final void method3089(int i) {
        anInt9222++;
        throw new IllegalStateException(); // unreachable per JaCoCo coverage
    }
}
