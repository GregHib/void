package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub31 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub31 extends Class348_Sub40 {
    private int anInt9405 = 4;
    static int anInt9407;
    private int anInt9410 = 4;
    static int anInt9412 = 0;
    static int anInt9413;

    public Class348_Sub40_Sub31() {
        super(1, false);
    }

    final int[][] method3047(int i, int i_0_) {
        anInt9407++;
        if (i_0_ != -1564599039) return null;
        int[][] is = this.aClass322_7033.method2557(-111, i);
        if (this.aClass322_7033.aBoolean4035) {
            int i_1_ = Class348_Sub40_Sub6.anInt9139 / anInt9405;
            int i_2_ = Class286_Sub2.anInt6212 / anInt9410;
            int[][] is_3_;
            if (i_2_ > 0) {
                int i_4_ = i % i_2_;
                is_3_ = this.method3039((byte) -86, Class286_Sub2.anInt6212 * i_4_ / i_2_, 0);
            } else is_3_ = this.method3039((byte) 105, 0, 0);
            int[] is_5_ = is_3_[0];
            int[] is_6_ = is_3_[1];
            int[] is_7_ = is_3_[2];
            int[] is_8_ = is[0];
            int[] is_9_ = is[1];
            int[] is_10_ = is[2];
            for (int i_11_ = 0; (i_11_ < Class348_Sub40_Sub6.anInt9139); i_11_++) {
                int i_12_;
                if (i_1_ <= 0) i_12_ = 0;
                else {
                    int i_13_ = i_11_ % i_1_;
                    i_12_ = Class348_Sub40_Sub6.anInt9139 * i_13_ / i_1_;
                }
                is_8_[i_11_] = is_5_[i_12_];
                is_9_[i_11_] = is_6_[i_12_];
                is_10_[i_11_] = is_7_[i_12_];
            }
        }
        return is;
    }

    final void method3049(Class348_Sub49 class348_sub49, int i, int i_14_) {
        if (i_14_ == 31015) {
            int i_15_ = i;
            do {
                if (i_15_ == 0) {
                    anInt9405 = class348_sub49.readUnsignedByte(255);
                    break;
                } else if (i_15_ != 1) break;
                anInt9410 = class348_sub49.readUnsignedByte(255);
            } while (false);
            anInt9413++;
        }
    }
}
