package world.gregs.voidps.tools.inv.item;/* Class239_Sub25 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class239_Sub25 {
    static int anInt6112;

    static final void method1827(int i) {
        anInt6112++;
        if (ItemSpriteCacheKey.anIntArray4983 == null) ItemSpriteCacheKey.anIntArray4983 = new int[65536];
        else return;
        double d = 0.7 + (0.03 * Math.random() - 0.015);
        int i_5_ = 0;
        if (i == 1415665776) {
            for (int i_6_ = 0; i_6_ < 512; i_6_++) {
                float f = (((float) (i_6_ >> 3) / 64.0F + 0.0078125F) * 360.0F);
                float f_7_ = (float) (i_6_ & 0x7) / 8.0F + 0.0625F;
                for (int i_8_ = 0; i_8_ < 128; i_8_++) {
                    float f_9_ = (float) i_8_ / 128.0F;
                    float f_10_ = 0.0F;
                    float f_11_ = 0.0F;
                    float f_12_ = 0.0F;
                    float f_13_ = f / 60.0F;
                    int i_14_ = (int) f_13_;
                    int i_15_ = i_14_ % 6;
                    float f_16_ = (float) -i_14_ + f_13_;
                    float f_17_ = f_9_ * (-f_7_ + 1.0F);
                    float f_18_ = f_9_ * (1.0F - f_16_ * f_7_);
                    float f_19_ = (1.0F - (1.0F - f_16_) * f_7_) * f_9_;
                    if (i_15_ == 0) {
                        f_10_ = f_9_;
                        f_11_ = f_19_;
                        f_12_ = f_17_;
                    } else if (i_15_ == 1) {
                        f_11_ = f_9_;
                        f_10_ = f_18_;
                        f_12_ = f_17_;
                    } else if (i_15_ == 2) {
                        f_12_ = f_19_;
                        f_10_ = f_17_;
                        f_11_ = f_9_;
                    } else if (i_15_ == 3) {
                        f_12_ = f_9_;
                        f_11_ = f_18_;
                        f_10_ = f_17_;
                    } else if (i_15_ == 4) {
                        f_12_ = f_9_;
                        f_11_ = f_17_;
                        f_10_ = f_19_;
                    } else if (i_15_ == 5) {
                        f_10_ = f_9_;
                        f_11_ = f_17_;
                        f_12_ = f_18_;
                    }
                    f_10_ = (float) Math.pow(f_10_, d);
                    f_11_ = (float) Math.pow(f_11_, d);
                    f_12_ = (float) Math.pow(f_12_, d);
                    int i_20_ = (int) (f_10_ * 256.0F);
                    int i_21_ = (int) (256.0F * f_11_);
                    int i_22_ = (int) (256.0F * f_12_);
                    int i_23_ = ((i_21_ << 8) + ((i_20_ << 16) + (-16777216 + i_22_)));
                    ItemSpriteCacheKey.anIntArray4983[i_5_++] = i_23_;
                }
            }
        }
    }
}
