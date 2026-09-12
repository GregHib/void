package world.gregs.voidps.tools.inv.item;/* Class175 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class175 {
    private int anInt2311;
    private final Class356 aClass356_2312;
    static int anInt2313;
    private Class107 aClass107_2316 = new Class107();
    private final int anInt2324;
    static int anInt2323;

    final Object method1340(int i, Interface14 interface14) {
        anInt2313++;
        long l = interface14.method52((byte) 64);
        for (Class348_Sub42_Sub9 class348_sub42_sub9 = (Class348_Sub42_Sub9) aClass356_2312.method3480(l, -6008); class348_sub42_sub9 != null; class348_sub42_sub9 = (Class348_Sub42_Sub9) aClass356_2312.method3476(true)) {
            if (class348_sub42_sub9.anInterface14_9559.method53(94, interface14)) {
                Object object = class348_sub42_sub9.method3205(65536);
                if (object == null) {
                    class348_sub42_sub9.method2715((byte) 36);
                    class348_sub42_sub9.method3162(true);
                    anInt2311 += (class348_sub42_sub9.anInt9556);
                } else {
                    if (class348_sub42_sub9.method3206((byte) -128)) {
                        Class348_Sub42_Sub9_Sub1 class348_sub42_sub9_sub1 = (new Class348_Sub42_Sub9_Sub1(interface14, object, (class348_sub42_sub9.anInt9556)));
                        aClass356_2312.method3483((byte) 125, (class348_sub42_sub9.aLong4291), class348_sub42_sub9_sub1);
                        aClass107_2316.method1005(true, class348_sub42_sub9_sub1);
                        class348_sub42_sub9_sub1.aLong7057 = 0L;
                        class348_sub42_sub9.method2715((byte) 65);
                        class348_sub42_sub9.method3162(true);
                    } else {
                        aClass107_2316.method1005(true, class348_sub42_sub9);
                        class348_sub42_sub9.aLong7057 = 0L;
                    }
                    return object;
                }
            }
        }
        if (i < 66) return null;
        return null;
    }

    static final float[] method1347(int i, int i_6_, float f, float f_7_, int i_8_, float f_9_, int i_10_, int i_11_) {
        anInt2323++;
        float[] fs = new float[9];
        float[] fs_12_ = new float[9];
        float f_13_ = (float) Math.cos((float) i_11_ * 0.024543693F);
        int i_14_ = -94 / ((i_8_ - 57) / 62);
        float f_15_ = (float) Math.sin(0.024543693F * (float) i_11_);
        fs[6] = -f_15_;
        float f_16_ = -f_13_ + 1.0F;
        fs[8] = f_13_;
        fs[3] = 0.0F;
        fs[1] = 0.0F;
        fs[2] = f_15_;
        fs[4] = 1.0F;
        fs[5] = 0.0F;
        fs[0] = f_13_;
        fs[7] = 0.0F;
        float[] fs_17_ = new float[9];
        float f_18_ = 1.0F;
        f_13_ = (float) i_6_ / 32767.0F;
        float f_19_ = 0.0F;
        f_16_ = -f_13_ + 1.0F;
        f_15_ = -(float) Math.sqrt(1.0F - f_13_ * f_13_);
        float f_20_ = (float) Math.sqrt(i_10_ * i_10_ + i * i);
        if (f_20_ == 0.0F && f_13_ == 0.0F) fs_12_ = fs;
        else {
            if (f_20_ != 0.0F) {
                f_18_ = (float) -i / f_20_;
                f_19_ = (float) i_10_ / f_20_;
            }
            fs_17_[5] = f_18_ * f_15_;
            fs_17_[2] = f_18_ * f_19_ * f_16_;
            fs_17_[8] = f_13_ + f_16_ * (f_19_ * f_19_);
            fs_17_[4] = f_13_;
            fs_17_[0] = f_16_ * (f_18_ * f_18_) + f_13_;
            fs_17_[6] = f_16_ * (f_19_ * f_18_);
            fs_17_[3] = f_15_ * -f_19_;
            fs_17_[1] = f_15_ * f_19_;
            fs_17_[7] = f_15_ * -f_18_;
            fs_12_[0] = fs_17_[0] * fs[0] + fs[1] * fs_17_[3] + fs_17_[6] * fs[2];
            fs_12_[1] = fs_17_[7] * fs[2] + (fs[1] * fs_17_[4] + fs[0] * fs_17_[1]);
            fs_12_[2] = fs[1] * fs_17_[5] + fs[0] * fs_17_[2] + fs[2] * fs_17_[8];
            fs_12_[3] = fs_17_[0] * fs[3] + fs[4] * fs_17_[3] + fs_17_[6] * fs[5];
            fs_12_[4] = fs[5] * fs_17_[7] + (fs[3] * fs_17_[1] + fs[4] * fs_17_[4]);
            fs_12_[6] = fs_17_[0] * fs[6] + fs[7] * fs_17_[3] + fs_17_[6] * fs[8];
            fs_12_[5] = fs[4] * fs_17_[5] + fs_17_[2] * fs[3] + fs[5] * fs_17_[8];
            fs_12_[7] = fs_17_[1] * fs[6] + fs_17_[4] * fs[7] + fs[8] * fs_17_[7];
            fs_12_[8] = fs_17_[5] * fs[7] + fs[6] * fs_17_[2] + fs[8] * fs_17_[8];
        }
        fs_12_[7] *= f;
        fs_12_[4] *= f_9_;
        fs_12_[3] *= f_9_;
        fs_12_[5] *= f_9_;
        fs_12_[2] *= f_7_;
        fs_12_[8] *= f;
        fs_12_[6] *= f;
        fs_12_[1] *= f_7_;
        fs_12_[0] *= f_7_;
        return fs_12_;
    }

    Class175(int i) {
        anInt2311 = i;
        anInt2324 = i;
        int i_22_;
        for (i_22_ = 1; i_22_ + i_22_ < i; i_22_ += i_22_) {
            /* empty */
        }
        aClass356_2312 = new Class356(i_22_);
    }
}
