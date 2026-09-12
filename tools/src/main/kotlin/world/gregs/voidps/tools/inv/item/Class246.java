package world.gregs.voidps.tools.inv.item;/* Class246 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Class246 {
    static int anInt3175;

    static final void method1885(int i, int i_0_, int i_1_, int i_2_, int i_3_, float[] fs, int i_4_, float f, int i_5_, int i_6_, float f_7_, float[] fs_8_) {
        try {
            i_4_ -= i_5_;
            i_0_ -= i;
            anInt3175++;
            i_3_ -= i_6_;
            float f_9_ = fs_8_[2] * (float) i_0_ + (fs_8_[1] * (float) i_4_ + (float) i_3_ * fs_8_[0]);
            float f_10_ = (fs_8_[5] * (float) i_0_ + (fs_8_[3] * (float) i_3_ + (float) i_4_ * fs_8_[4]));
            float f_11_ = ((float) i_3_ * fs_8_[6] + fs_8_[7] * (float) i_4_ + (float) i_0_ * fs_8_[i_2_]);
            float f_12_ = 0.5F + ((float) Math.atan2(f_9_, f_11_) / 6.2831855F);
            if (f_7_ != 1.0F) f_12_ *= f_7_;
            float f_13_ = 0.5F + f_10_ + f;
            if (i_1_ == 1) {
                float f_14_ = f_12_;
                f_12_ = -f_13_;
                f_13_ = f_14_;
            } else if (i_1_ == 2) {
                f_12_ = -f_12_;
                f_13_ = -f_13_;
            } else if (i_1_ == 3) {
                float f_15_ = f_12_;
                f_12_ = f_13_;
                f_13_ = -f_15_;
            }
            fs[1] = f_13_;
            fs[0] = f_12_;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ca.D(" + i + ',' + i_0_ + ',' + i_1_ + ',' + i_2_ + ',' + i_3_ + ',' + (fs != null ? "{...}" : "null") + ',' + i_4_ + ',' + f + ',' + i_5_ + ',' + i_6_ + ',' + f_7_ + ',' + (fs_8_ != null ? "{...}" : "null") + ')'));
        }
    }

    Class246() {
        /* empty */
    }

    abstract Class348_Sub42_Sub8 method1888(int i, Class348_Sub42_Sub8 class348_sub42_sub8);
}
