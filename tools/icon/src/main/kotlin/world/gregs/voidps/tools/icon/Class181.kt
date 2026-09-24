package world.gregs.voidps.tools.icon;/* Class181 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class181 {
    /* Minimal stub: Class64_Sub1's kept code only calls the static
     * method1367(...) helper (verbatim from the original), which in turn
     * calls method1369(...) and uses these two static fields. */
    static int anInt2409;
    static boolean[] aBooleanArray2374 = new boolean[5];
    static int anInt2398;

    static final void sphereMap(int i, int i_0_, float f, int i_1_, float[] fs, int i_2_, int i_3_, int i_4_, int i_5_, int i_6_, float[] fs_7_) {
        try {
            i_2_ -= i;
            anInt2409++;
            i_1_ -= i_3_;
            i_4_ -= i_5_;
            float f_8_ = (float) i_2_ * fs_7_[2] + (fs_7_[1] * (float) i_4_ + fs_7_[0] * (float) i_1_);
            float f_9_ = (float) i_2_ * fs_7_[5] + ((float) i_1_ * fs_7_[3] + (float) i_4_ * fs_7_[4]);
            float f_10_ = (fs_7_[6] * (float) i_1_ + (float) i_4_ * fs_7_[7] + (float) i_2_ * fs_7_[8]);
            float f_11_ = (float) Math.sqrt(f_8_ * f_8_ + f_9_ * f_9_ + f_10_ * f_10_);
            float f_12_ = 0.5F + ((float) Math.atan2(f_8_, f_10_) / 6.2831855F);
            if (i_6_ != -4) method1369((byte) 98);
            float f_13_ = f + (0.5F + ((float) Math.asin(f_9_ / f_11_) / 3.1415927F));
            if (i_0_ == 1) {
                float f_15_ = f_12_;
                f_12_ = -f_13_;
                f_13_ = f_15_;
            } else if (i_0_ == 2) {
                f_13_ = -f_13_;
                f_12_ = -f_12_;
            } else if (i_0_ == 3) {
                float f_14_ = f_12_;
                f_12_ = f_13_;
                f_13_ = -f_14_;
            }
            fs[0] = f_12_;
            fs[1] = f_13_;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("qb.E(" + i + ',' + i_0_ + ',' + f + ',' + i_1_ + ',' + (fs != null ? "{...}" : "null") + ',' + i_2_ + ',' + i_3_ + ',' + i_4_ + ',' + i_5_ + ',' + i_6_ + ',' + (fs_7_ != null ? "{...}" : "null") + ')'));
        }
    }

    public static void method1369(byte i) {
        aBooleanArray2374 = null;
        if (i != 2) anInt2398 = 113;
    }
}
