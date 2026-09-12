package world.gregs.voidps.tools.inv.item;/* Class212 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class212 {
    private static Class40 aClass40_2750 = new Class40();

    private static final int method1545(int i, Class40 class40) {
        for (; ; ) {
            if (class40.anInt546 >= i) {
                int i_0_ = ((class40.anInt554 >> class40.anInt546 - i) & (1 << i) - 1);
                class40.anInt546 -= i;
                return i_0_;
            }
            class40.anInt554 = (class40.anInt554 << 8 | (class40.aByteArray534[class40.anInt522]) & 0xff);
            class40.anInt546 += 8;
            class40.anInt522++;
            class40.anInt536++;
        }
    }

    private static final void method1546(int[] is, int[] is_1_, int[] is_2_, byte[] is_3_, int i, int i_4_, int i_5_) {
        int i_6_ = 0;
        for (int i_7_ = i; i_7_ <= i_4_; i_7_++) {
            for (int i_8_ = 0; i_8_ < i_5_; i_8_++) {
                if (is_3_[i_8_] == i_7_) {
                    is_2_[i_6_] = i_8_;
                    i_6_++;
                }
            }
        }
        for (int i_9_ = 0; i_9_ < 23; i_9_++)
            is_1_[i_9_] = 0;
        for (int i_10_ = 0; i_10_ < i_5_; i_10_++)
            is_1_[is_3_[i_10_] + 1]++;
        for (int i_11_ = 1; i_11_ < 23; i_11_++)
            is_1_[i_11_] += is_1_[i_11_ - 1];
        for (int i_12_ = 0; i_12_ < 23; i_12_++)
            is[i_12_] = 0;
        int i_13_ = 0;
        for (int i_14_ = i; i_14_ <= i_4_; i_14_++) {
            i_13_ += is_1_[i_14_ + 1] - is_1_[i_14_];
            is[i_14_] = i_13_ - 1;
            i_13_ <<= 1;
        }
        for (int i_15_ = i + 1; i_15_ <= i_4_; i_15_++)
            is_1_[i_15_] = (is[i_15_ - 1] + 1 << 1) - is_1_[i_15_];
    }

    static final int method1547(byte[] is, int i, byte[] is_16_, int i_17_, int i_18_) {
        synchronized (aClass40_2750) {
            aClass40_2750.aByteArray534 = is_16_;
            aClass40_2750.anInt522 = i_18_;
            aClass40_2750.aByteArray527 = is;
            aClass40_2750.anInt548 = 0;
            aClass40_2750.anInt538 = i;
            aClass40_2750.anInt546 = 0;
            aClass40_2750.anInt554 = 0;
            aClass40_2750.anInt536 = 0;
            aClass40_2750.anInt524 = 0;
            method1552(aClass40_2750);
            i -= aClass40_2750.anInt538;
            aClass40_2750.aByteArray534 = null;
            aClass40_2750.aByteArray527 = null;
            return i;
        }
    }

    private static final byte method1548(Class40 class40) {
        return (byte) method1545(8, class40);
    }

    private static final void method1549(Class40 class40) {
        byte i = class40.aByte539;
        int i_19_ = class40.anInt533;
        int i_20_ = class40.anInt555;
        int i_21_ = class40.anInt537;
        int[] is = Class286_Sub3.anIntArray6228;
        int i_22_ = class40.anInt552;
        byte[] is_23_ = class40.aByteArray527;
        int i_24_ = class40.anInt548;
        int i_25_ = class40.anInt538;
        int i_26_ = i_25_;
        int i_27_ = class40.anInt550 + 1;
        while_75_:
        for (; ; ) {
            if (i_19_ > 0) {
                for (; ; ) {
                    if (i_25_ == 0) break while_75_;
                    if (i_19_ == 1) break;
                    is_23_[i_24_] = i;
                    i_19_--;
                    i_24_++;
                    i_25_--;
                }
                if (i_25_ == 0) {
                    i_19_ = 1;
                    break;
                }
                is_23_[i_24_] = i;
                i_24_++;
                i_25_--;
            }
            for (; ; ) {
                if (i_20_ == i_27_) {
                    i_19_ = 0;
                    break while_75_;
                }
                i = (byte) i_21_;
                i_22_ = is[i_22_];
                int i_28_ = (byte) i_22_;
                i_22_ >>= 8;
                i_20_++;
                if (i_28_ == i_21_) {
                    if (i_20_ != i_27_) break;
                    if (i_25_ == 0) {
                        i_19_ = 1;
                        break while_75_;
                    }
                    is_23_[i_24_] = i;
                    i_24_++;
                    i_25_--;
                } else {
                    i_21_ = i_28_;
                    if (i_25_ == 0) {
                        i_19_ = 1;
                        break while_75_;
                    }
                    is_23_[i_24_] = i;
                    i_24_++;
                    i_25_--;
                }
            }
            i_19_ = 2;
            i_22_ = is[i_22_];
            int i_29_ = (byte) i_22_;
            i_22_ >>= 8;
            if (++i_20_ != i_27_) {
                if (i_29_ == i_21_) {
                    i_19_ = 3;
                    i_22_ = is[i_22_];
                    i_29_ = (byte) i_22_;
                    i_22_ >>= 8;
                    if (++i_20_ != i_27_) {
                        if (i_29_ == i_21_) {
                            i_22_ = is[i_22_];
                            i_29_ = (byte) i_22_;
                            i_22_ >>= 8;
                            i_20_++;
                            i_19_ = (i_29_ & 0xff) + 4;
                            i_22_ = is[i_22_];
                            i_21_ = (byte) i_22_;
                            i_22_ >>= 8;
                            i_20_++;
                        } else i_21_ = i_29_;
                    }
                } else i_21_ = i_29_;
            }
        }
        int i_30_ = class40.anInt524;
        class40.anInt524 += i_26_ - i_25_;
        class40.aByte539 = i;
        class40.anInt533 = i_19_;
        class40.anInt555 = i_20_;
        class40.anInt537 = i_21_;
        Class286_Sub3.anIntArray6228 = is;
        class40.anInt552 = i_22_;
        class40.aByteArray527 = is_23_;
        class40.anInt548 = i_24_;
        class40.anInt538 = i_25_;
    }

    private static final void method1550(Class40 class40) {
        class40.anInt541 = 0;
        for (int i = 0; i < 256; i++) {
            if (class40.aBooleanArray523[i]) {
                class40.aByteArray528[class40.anInt541] = (byte) i;
                class40.anInt541++;
            }
        }
    }

    public static void method1551() {
        aClass40_2750 = null;
    }

    private static final void method1552(Class40 class40) {
        boolean bool = false;
        boolean bool_31_ = false;
        boolean bool_32_ = false;
        boolean bool_33_ = false;
        boolean bool_34_ = false;
        boolean bool_35_ = false;
        boolean bool_36_ = false;
        boolean bool_37_ = false;
        boolean bool_38_ = false;
        boolean bool_39_ = false;
        boolean bool_40_ = false;
        boolean bool_41_ = false;
        boolean bool_42_ = false;
        boolean bool_43_ = false;
        boolean bool_44_ = false;
        boolean bool_45_ = false;
        boolean bool_46_ = false;
        boolean bool_47_ = false;
        int i = 0;
        int[] is = null;
        int[] is_48_ = null;
        int[] is_49_ = null;
        class40.anInt526 = 1;
        if (Class286_Sub3.anIntArray6228 == null) Class286_Sub3.anIntArray6228 = new int[class40.anInt526 * 100000];
        boolean bool_50_ = true;
        while (bool_50_) {
            byte i_51_ = method1548(class40);
            if (i_51_ == 23) break;
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1548(class40);
            i_51_ = method1553(class40);
            class40.anInt530 = 0;
            int i_52_ = method1548(class40);
            class40.anInt530 = class40.anInt530 << 8 | i_52_ & 0xff;
            i_52_ = method1548(class40);
            class40.anInt530 = class40.anInt530 << 8 | i_52_ & 0xff;
            i_52_ = method1548(class40);
            class40.anInt530 = class40.anInt530 << 8 | i_52_ & 0xff;
            for (int i_53_ = 0; i_53_ < 16; i_53_++) {
                i_51_ = method1553(class40);
                class40.aBooleanArray532[i_53_] = i_51_ == 1;
            }
            for (int i_54_ = 0; i_54_ < 256; i_54_++)
                class40.aBooleanArray523[i_54_] = false;
            for (int i_55_ = 0; i_55_ < 16; i_55_++) {
                if (class40.aBooleanArray532[i_55_]) {
                    for (int i_56_ = 0; i_56_ < 16; i_56_++) {
                        i_51_ = method1553(class40);
                        if (i_51_ == 1) class40.aBooleanArray523[(i_55_ * 16 + i_56_)] = true;
                    }
                }
            }
            method1550(class40);
            int i_57_ = class40.anInt541 + 2;
            int i_58_ = method1545(3, class40);
            int i_59_ = method1545(15, class40);
            for (int i_60_ = 0; i_60_ < i_59_; i_60_++) {
                int i_61_ = 0;
                for (; ; ) {
                    i_51_ = method1553(class40);
                    if (i_51_ == 0) break;
                    i_61_++;
                }
                class40.aByteArray544[i_60_] = (byte) i_61_;
            }
            byte[] is_62_ = new byte[6];
            for (byte i_63_ = 0; i_63_ < i_58_; i_63_++)
                is_62_[i_63_] = i_63_;
            for (int i_64_ = 0; i_64_ < i_59_; i_64_++) {
                byte i_65_ = class40.aByteArray544[i_64_];
                byte i_66_ = is_62_[i_65_];
                for (/**/; i_65_ > 0; i_65_--)
                    is_62_[i_65_] = is_62_[i_65_ - 1];
                is_62_[0] = i_66_;
                class40.aByteArray531[i_64_] = i_66_;
            }
            for (int i_67_ = 0; i_67_ < i_58_; i_67_++) {
                int i_68_ = method1545(5, class40);
                for (int i_69_ = 0; i_69_ < i_57_; i_69_++) {
                    for (; ; ) {
                        i_51_ = method1553(class40);
                        if (i_51_ == 0) break;
                        i_51_ = method1553(class40);
                        if (i_51_ == 0) i_68_++;
                        else i_68_--;
                    }
                    class40.aByteArrayArray549[i_67_][i_69_] = (byte) i_68_;
                }
            }
            for (int i_70_ = 0; i_70_ < i_58_; i_70_++) {
                int i_71_ = 32;
                byte i_72_ = 0;
                for (int i_73_ = 0; i_73_ < i_57_; i_73_++) {
                    if (class40.aByteArrayArray549[i_70_][i_73_] > i_72_) i_72_ = (class40.aByteArrayArray549[i_70_][i_73_]);
                    if (class40.aByteArrayArray549[i_70_][i_73_] < i_71_) i_71_ = (class40.aByteArrayArray549[i_70_][i_73_]);
                }
                method1546(class40.anIntArrayArray556[i_70_], class40.anIntArrayArray553[i_70_], class40.anIntArrayArray529[i_70_], class40.aByteArrayArray549[i_70_], i_71_, i_72_, i_57_);
                class40.anIntArray525[i_70_] = i_71_;
            }
            int i_74_ = class40.anInt541 + 1;
            int i_75_ = -1;
            int i_76_ = 0;
            for (int i_77_ = 0; i_77_ <= 255; i_77_++)
                class40.anIntArray535[i_77_] = 0;
            int i_78_ = 4095;
            for (int i_79_ = 15; i_79_ >= 0; i_79_--) {
                for (int i_80_ = 15; i_80_ >= 0; i_80_--) {
                    class40.aByteArray545[i_78_] = (byte) (i_79_ * 16 + i_80_);
                    i_78_--;
                }
                class40.anIntArray540[i_79_] = i_78_ + 1;
            }
            int i_81_ = 0;
            if (i_76_ == 0) {
                i_75_++;
                i_76_ = 50;
                byte i_82_ = class40.aByteArray531[i_75_];
                i = class40.anIntArray525[i_82_];
                is = class40.anIntArrayArray556[i_82_];
                is_49_ = class40.anIntArrayArray529[i_82_];
                is_48_ = class40.anIntArrayArray553[i_82_];
            }
            i_76_--;
            int i_83_ = i;
            int i_84_;
            int i_85_;
            for (i_85_ = method1545(i_83_, class40); i_85_ > is[i_83_]; i_85_ = i_85_ << 1 | i_84_) {
                i_83_++;
                i_84_ = method1553(class40);
            }
            int i_86_ = is_49_[i_85_ - is_48_[i_83_]];
            while (i_86_ != i_74_) {
                if (i_86_ == 0 || i_86_ == 1) {
                    int i_87_ = -1;
                    int i_88_ = 1;
                    do {
                        if (i_86_ == 0) i_87_ += i_88_;
                        else if (i_86_ == 1) i_87_ += 2 * i_88_;
                        i_88_ *= 2;
                        if (i_76_ == 0) {
                            i_75_++;
                            i_76_ = 50;
                            byte i_89_ = class40.aByteArray531[i_75_];
                            i = class40.anIntArray525[i_89_];
                            is = class40.anIntArrayArray556[i_89_];
                            is_49_ = (class40.anIntArrayArray529[i_89_]);
                            is_48_ = (class40.anIntArrayArray553[i_89_]);
                        }
                        i_76_--;
                        i_83_ = i;
                        for (i_85_ = method1545(i_83_, class40); i_85_ > is[i_83_]; i_85_ = i_85_ << 1 | i_84_) {
                            i_83_++;
                            i_84_ = method1553(class40);
                        }
                        i_86_ = is_49_[i_85_ - is_48_[i_83_]];
                    } while (i_86_ == 0 || i_86_ == 1);
                    i_87_++;
                    i_52_ = (class40.aByteArray528[(class40.aByteArray545[class40.anIntArray540[0]]) & 0xff]);
                    class40.anIntArray535[i_52_ & 0xff] += i_87_;
                    for (/**/; i_87_ > 0; i_87_--) {
                        Class286_Sub3.anIntArray6228[i_81_] = i_52_ & 0xff;
                        i_81_++;
                    }
                } else {
                    int i_90_ = i_86_ - 1;
                    if (i_90_ < 16) {
                        int i_91_ = class40.anIntArray540[0];
                        i_51_ = class40.aByteArray545[i_91_ + i_90_];
                        for (/**/; i_90_ > 3; i_90_ -= 4) {
                            int i_92_ = i_91_ + i_90_;
                            class40.aByteArray545[i_92_] = class40.aByteArray545[i_92_ - 1];
                            class40.aByteArray545[i_92_ - 1] = class40.aByteArray545[i_92_ - 2];
                            class40.aByteArray545[i_92_ - 2] = class40.aByteArray545[i_92_ - 3];
                            class40.aByteArray545[i_92_ - 3] = class40.aByteArray545[i_92_ - 4];
                        }
                        for (/**/; i_90_ > 0; i_90_--)
                            class40.aByteArray545[i_91_ + i_90_] = (class40.aByteArray545[i_91_ + i_90_ - 1]);
                        class40.aByteArray545[i_91_] = i_51_;
                    } else {
                        int i_93_ = i_90_ / 16;
                        int i_94_ = i_90_ % 16;
                        int i_95_ = class40.anIntArray540[i_93_] + i_94_;
                        i_51_ = class40.aByteArray545[i_95_];
                        for (/**/; i_95_ > class40.anIntArray540[i_93_]; i_95_--)
                            class40.aByteArray545[i_95_] = class40.aByteArray545[i_95_ - 1];
                        class40.anIntArray540[i_93_]++;
                        for (/**/; i_93_ > 0; i_93_--) {
                            class40.anIntArray540[i_93_]--;
                            class40.aByteArray545[class40.anIntArray540[i_93_]] = (class40.aByteArray545[(class40.anIntArray540[i_93_ - 1]) + 16 - 1]);
                        }
                        class40.anIntArray540[0]--;
                        class40.aByteArray545[(class40.anIntArray540[0])] = i_51_;
                        if (class40.anIntArray540[0] == 0) {
                            int i_96_ = 4095;
                            for (int i_97_ = 15; i_97_ >= 0; i_97_--) {
                                for (int i_98_ = 15; i_98_ >= 0; i_98_--) {
                                    class40.aByteArray545[i_96_] = (class40.aByteArray545[(class40.anIntArray540[i_97_]) + i_98_]);
                                    i_96_--;
                                }
                                class40.anIntArray540[i_97_] = i_96_ + 1;
                            }
                        }
                    }
                    class40.anIntArray535[(class40.aByteArray528[i_51_ & 0xff] & 0xff)]++;
                    Class286_Sub3.anIntArray6228[i_81_] = (class40.aByteArray528[i_51_ & 0xff] & 0xff);
                    i_81_++;
                    if (i_76_ == 0) {
                        i_75_++;
                        i_76_ = 50;
                        byte i_99_ = class40.aByteArray531[i_75_];
                        i = class40.anIntArray525[i_99_];
                        is = class40.anIntArrayArray556[i_99_];
                        is_49_ = class40.anIntArrayArray529[i_99_];
                        is_48_ = class40.anIntArrayArray553[i_99_];
                    }
                    i_76_--;
                    i_83_ = i;
                    for (i_85_ = method1545(i_83_, class40); i_85_ > is[i_83_]; i_85_ = i_85_ << 1 | i_84_) {
                        i_83_++;
                        i_84_ = method1553(class40);
                    }
                    i_86_ = is_49_[i_85_ - is_48_[i_83_]];
                }
            }
            class40.anInt533 = 0;
            class40.aByte539 = (byte) 0;
            class40.anIntArray557[0] = 0;
            for (int i_100_ = 1; i_100_ <= 256; i_100_++)
                class40.anIntArray557[i_100_] = class40.anIntArray535[i_100_ - 1];
            for (int i_101_ = 1; i_101_ <= 256; i_101_++)
                class40.anIntArray557[i_101_] += class40.anIntArray557[i_101_ - 1];
            for (int i_102_ = 0; i_102_ < i_81_; i_102_++) {
                i_52_ = (byte) (Class286_Sub3.anIntArray6228[i_102_] & 0xff);
                Class286_Sub3.anIntArray6228[(class40.anIntArray557[i_52_ & 0xff])] |= i_102_ << 8;
                class40.anIntArray557[i_52_ & 0xff]++;
            }
            class40.anInt552 = (Class286_Sub3.anIntArray6228[class40.anInt530] >> 8);
            class40.anInt555 = 0;
            class40.anInt552 = Class286_Sub3.anIntArray6228[class40.anInt552];
            class40.anInt537 = (byte) (class40.anInt552 & 0xff);
            class40.anInt552 >>= 8;
            class40.anInt555++;
            class40.anInt550 = i_81_;
            method1549(class40);
            bool_50_ = (class40.anInt555 == class40.anInt550 + 1) && class40.anInt533 == 0;
        }
    }

    private static final byte method1553(Class40 class40) {
        return (byte) method1545(1, class40);
    }
}
