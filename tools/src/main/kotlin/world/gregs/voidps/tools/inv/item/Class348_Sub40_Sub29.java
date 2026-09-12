package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub29 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub29 extends Class348_Sub40 {
    static int anInt9373;
    private int anInt9374;
    private int[] anIntArray9375;
    static int anInt9376;
    static int anInt9378;
    private int anInt9379;
    private int anInt9380 = -1;
    static int anInt9381;
    static int anInt9382;

    final int[][] method3047(int i, int i_0_) {
        if (i_0_ != -1564599039) return null;
        anInt9378++;
        int[][] is = this.aClass322_7033.method2557(-94, i);
        if (this.aClass322_7033.aBoolean4035) {
            int i_1_ = (anInt9374 * (Class286_Sub2.anInt6212 == anInt9379 ? i : anInt9379 * i / Class286_Sub2.anInt6212));
            int[] is_2_ = is[0];
            int[] is_3_ = is[1];
            int[] is_4_ = is[2];
            if (Class348_Sub40_Sub6.anInt9139 == anInt9374) {
                for (int i_8_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_8_); i_8_++) {
                    int i_9_ = anIntArray9375[i_1_++];
                    is_4_[i_8_] = Class139.method1166(255, i_9_) << 4;
                    is_3_[i_8_] = Class139.method1166(i_9_ >> 4, 4080);
                    is_2_[i_8_] = Class139.method1166(16711680, i_9_) >> 12;
                }
            } else {
                for (int i_5_ = 0; i_5_ < Class348_Sub40_Sub6.anInt9139; i_5_++) {
                    int i_6_ = anInt9374 * i_5_ / Class348_Sub40_Sub6.anInt9139;
                    int i_7_ = anIntArray9375[i_6_ + i_1_];
                    is_4_[i_5_] = Class139.method1166(i_7_, 255) << 4;
                    is_3_[i_5_] = Class139.method1166(65280, i_7_) >> 4;
                    is_2_[i_5_] = Class139.method1166(i_7_ >> 12, 4080);
                }
            }
        }
        return is;
    }

    final void method3045(int i, int i_10_, int i_11_) {
        super.method3045(i, i_10_, i_11_);
        anInt9382++;
        if (anInt9380 >= 0 && Class286_Sub5.aD6247 != null) {
            int i_12_ = (!(Class286_Sub5.aD6247.method3(anInt9380, -6662).aBoolean199) ? 128 : 64);
            anIntArray9375 = Class286_Sub5.aD6247.method5(false, anInt9380, 1.0F, i_12_, i_12_, -123);
            anInt9379 = i_12_;
            anInt9374 = i_12_;
        }
    }

    public Class348_Sub40_Sub29() {
        super(0, false);
    }

    final void method3046(byte i) {
        if (i >= -102) anInt9374 = -104;
        anInt9373++;
        super.method3046((byte) -107);
        anIntArray9375 = null;
    }

    final void method3049(Class348_Sub49 class348_sub49, int i, int i_15_) {
        if (i_15_ == 31015) {
            if (i == 0) anInt9380 = class348_sub49.readUnsignedShort(842397944);
            anInt9376++;
        }
    }

    final int method3043(int i) {
        if (i != -1) anInt9379 = 10;
        anInt9381++;
        return anInt9380;
    }
}
