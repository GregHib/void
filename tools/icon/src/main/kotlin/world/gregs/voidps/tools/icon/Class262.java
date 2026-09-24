package world.gregs.voidps.tools.icon;/* Class262 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class262 {
    static Font aFont_3326;
    static int anInt3330;
    Class348 aClass348_3334 = new Class348();
    static Class190[][] aClass190ArrayArray3335;
    static int anInt3336;
    static int anInt3338;
    static int anInt3339;
    static int anInt3341;
    private Class348 aClass348_3342;

    static final void cubeMap(float f, float f_3_, float[] fs, int i, int i_4_, boolean bool, int i_5_, int i_6_, int i_7_, int i_8_, float f_9_, float[] fs_10_, int i_11_, int i_12_) {
        do {
            try {
                anInt3338++;
                i_7_ -= i_6_;
                i -= i_4_;
                i_8_ -= i_11_;
                float f_13_ = fs[2] * (float) i + (fs[1] * (float) i_8_ + fs[0] * (float) i_7_);
                float f_14_ = ((float) i_7_ * fs[3] + (float) i_8_ * fs[4] + (float) i * fs[5]);
                float f_15_ = fs[8] * (float) i + (fs[6] * (float) i_7_ + (float) i_8_ * fs[7]);
                float f_16_;
                float f_17_;
                if (i_12_ == 0) {
                    f_16_ = 0.5F + (f_3_ + f_13_);
                    f_17_ = -f_15_ + f + 0.5F;
                } else if (i_12_ == 1) {
                    f_17_ = 0.5F + (f_15_ + f);
                    f_16_ = 0.5F + (f_3_ + f_13_);
                } else if (i_12_ == 2) {
                    f_16_ = 0.5F + (-f_13_ + f_3_);
                    f_17_ = -f_14_ + f_9_ + 0.5F;
                } else if (i_12_ == 3) {
                    f_17_ = -f_14_ + f_9_ + 0.5F;
                    f_16_ = f_13_ + f_3_ + 0.5F;
                } else if (i_12_ == 4) {
                    f_16_ = f_15_ + f + 0.5F;
                    f_17_ = -f_14_ + f_9_ + 0.5F;
                } else {
                    f_16_ = 0.5F + (f + -f_15_);
                    f_17_ = -f_14_ + f_9_ + 0.5F;
                }
                if (i_5_ == 1) {
                    float f_18_ = f_16_;
                    f_16_ = -f_17_;
                    f_17_ = f_18_;
                } else if (i_5_ == 2) {
                    f_17_ = -f_17_;
                    f_16_ = -f_16_;
                } else if (i_5_ == 3) {
                    float f_19_ = f_16_;
                    f_16_ = f_17_;
                    f_17_ = -f_19_;
                }
                fs_10_[1] = f_17_;
                fs_10_[0] = f_16_;
                if (bool == false) break;
                cubeMap(0.31271333F, 1.5829445F, null, -17, 88, true, -70, -107, 8, 5, -0.347415F, null, -24, -19);
            } catch (RuntimeException runtimeexception) {
                throw Class348_Sub17.method2929(runtimeexception, ("uh.B(" + f + ',' + f_3_ + ',' + (fs != null ? "{...}" : "null") + ',' + i + ',' + i_4_ + ',' + bool + ',' + i_5_ + ',' + i_6_ + ',' + i_7_ + ',' + i_8_ + ',' + f_9_ + ',' + (fs_10_ != null ? "{...}" : "null") + ',' + i_11_ + ',' + i_12_ + ')'));
            }
            break;
        } while (false);
    }

    final Class348 method1993(int i) {
        anInt3336++;
        Class348 class348 = this.aClass348_3334.aClass348_4295;
        if (this.aClass348_3334 == class348) {
            aClass348_3342 = null;
            return null;
        }
        aClass348_3342 = class348.aClass348_4295;
        if (i > -65) method1993(67);
        return class348;
    }

    final void method1996(int i) {
        if (i > 97) {
            anInt3339++;
            for (; ; ) {
                Class348 class348 = (this.aClass348_3334.aClass348_4294);
                if (this.aClass348_3334 == class348) break;
                class348.unlink((byte) 24);
            }
            aClass348_3342 = null;
        }
    }

    final Class348 method1997(int i) {
        anInt3341++;
        if (i != 8) aClass190ArrayArray3335 = null;
        Class348 class348 = this.aClass348_3334.aClass348_4294;
        if (this.aClass348_3334 == class348) return null;
        class348.unlink((byte) 114);
        return class348;
    }

    final void method2001(Class348 class348, int i) {
        anInt3330++;
        if (class348.aClass348_4295 != null) class348.unlink((byte) 63);
        class348.aClass348_4295 = this.aClass348_3334;
        class348.aClass348_4294 = this.aClass348_3334.aClass348_4294;
        if (i > -89) aFont_3326 = null;
        class348.aClass348_4295.aClass348_4294 = class348;
        class348.aClass348_4294.aClass348_4295 = class348;
    }

    public Class262() {
        this.aClass348_3334.aClass348_4295 = this.aClass348_3334;
        this.aClass348_3334.aClass348_4294 = this.aClass348_3334;
    }
}
