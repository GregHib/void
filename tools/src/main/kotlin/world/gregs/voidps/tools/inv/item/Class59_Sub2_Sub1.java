package world.gregs.voidps.tools.inv.item;/* Class59_Sub2_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class59_Sub2_Sub1 {
    static Class45 aClass45_8670;
    static int anInt8671;
    static int anInt8673;

    static final Class358 method565(int i, int i_0_, Class124 class124, int[] is) {
        try {
            anInt8673++;
            int[] is_1_ = null;
            if (i != 255) aClass45_8670 = null;
            int[] is_2_ = null;
            int[] is_3_ = null;
            float[][] fs = null;
            if (class124.aByteArray1820 != null) {
                int i_4_ = class124.anInt1818;
                int[] is_5_ = new int[i_4_];
                int[] is_6_ = new int[i_4_];
                int[] is_7_ = new int[i_4_];
                int[] is_8_ = new int[i_4_];
                int[] is_9_ = new int[i_4_];
                int[] is_10_ = new int[i_4_];
                for (int i_11_ = 0; i_11_ < i_4_; i_11_++) {
                    is_5_[i_11_] = 2147483647;
                    is_6_[i_11_] = -2147483647;
                    is_7_[i_11_] = 2147483647;
                    is_8_[i_11_] = -2147483647;
                    is_9_[i_11_] = 2147483647;
                    is_10_[i_11_] = -2147483647;
                }
                fs = new float[i_4_][];
                is_3_ = new int[i_4_];
                is_2_ = new int[i_4_];
                for (int i_12_ = 0; i_12_ < i_0_; i_12_++) {
                    int i_13_ = is[i_12_];
                    if (class124.aByteArray1820[i_13_] != -1) {
                        int i_14_ = (class124.aByteArray1820[i_13_] & 0xff);
                        for (int i_15_ = 0; i_15_ < 3; i_15_++) {
                            short i_16_;
                            if (i_15_ != 0) {
                                if (i_15_ == 1) i_16_ = (class124.aShortArray1835[i_13_]);
                                else i_16_ = (class124.aShortArray1855[i_13_]);
                            } else i_16_ = (class124.aShortArray1863[i_13_]);
                            int i_17_ = class124.anIntArray1841[i_16_];
                            int i_18_ = class124.anIntArray1847[i_16_];
                            int i_19_ = class124.anIntArray1852[i_16_];
                            if (i_17_ < is_5_[i_14_]) is_5_[i_14_] = i_17_;
                            if (is_6_[i_14_] < i_17_) is_6_[i_14_] = i_17_;
                            if (is_7_[i_14_] > i_18_) is_7_[i_14_] = i_18_;
                            if (i_18_ > is_8_[i_14_]) is_8_[i_14_] = i_18_;
                            if (i_19_ < is_9_[i_14_]) is_9_[i_14_] = i_19_;
                            if (i_19_ > is_10_[i_14_]) is_10_[i_14_] = i_19_;
                        }
                    }
                }
                is_1_ = new int[i_4_];
                for (int i_20_ = 0; i_4_ > i_20_; i_20_++) {
                    byte i_21_ = class124.aByteArray1823[i_20_];
                    if (i_21_ > 0) {
                        is_1_[i_20_] = (is_6_[i_20_] + is_5_[i_20_]) / 2;
                        is_2_[i_20_] = (is_8_[i_20_] + is_7_[i_20_]) / 2;
                        is_3_[i_20_] = (is_9_[i_20_] + is_10_[i_20_]) / 2;
                        float f;
                        float f_22_;
                        float f_23_;
                        if (i_21_ == 1) {
                            int i_24_ = class124.anIntArray1859[i_20_];
                            if (i_24_ == 0) {
                                f_22_ = 1.0F;
                                f_23_ = 1.0F;
                            } else if (i_24_ <= 0) {
                                f_22_ = 1.0F;
                                f_23_ = (float) -i_24_ / 1024.0F;
                            } else {
                                f_23_ = 1.0F;
                                f_22_ = (float) i_24_ / 1024.0F;
                            }
                            f = 64.0F / (float) (class124.anIntArray1816[i_20_]);
                        } else if (i_21_ == 2) {
                            f = 64.0F / (float) (class124.anIntArray1816[i_20_]);
                            f_22_ = 64.0F / (float) (class124.anIntArray1844[i_20_]);
                            f_23_ = 64.0F / (float) (class124.anIntArray1859[i_20_]);
                        } else {
                            f = (float) (class124.anIntArray1816[i_20_]) / 1024.0F;
                            f_22_ = (float) (class124.anIntArray1844[i_20_]) / 1024.0F;
                            f_23_ = (float) (class124.anIntArray1859[i_20_]) / 1024.0F;
                        }
                        fs[i_20_] = (Class175.method1347(class124.aShortArray1825[i_20_], class124.aShortArray1849[i_20_], f_22_, f_23_, 126, f, class124.aShortArray1829[i_20_], Class139.method1166(255, (class124.aByteArray1833[i_20_]))));
                    }
                }
            }
            return new Class358(is_1_, is_2_, is_3_, fs);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("dha.B(" + i + ',' + i_0_ + ',' + (class124 != null ? "{...}" : "null") + ',' + (is != null ? "{...}" : "null") + ')'));
        }
    }

    static final void method566(boolean bool, boolean bool_25_, byte i) {
        anInt8671++;
        int i_26_ = -94 / ((-67 - i) / 59);
        if (bool) {
            Class348_Sub40_Sub26.anInt9346++;
            Class239_Sub25.method1827(1415665776);
        }
        if (bool_25_) {
            Class26.anInt383++;
            Class348_Sub6.method2770(2);
        }
    }
}
