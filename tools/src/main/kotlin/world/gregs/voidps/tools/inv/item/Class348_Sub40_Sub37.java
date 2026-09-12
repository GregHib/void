package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub37 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub37 extends Class348_Sub40 {
    static int anInt9459;
    private int anInt9463 = 1;
    static int anInt9464;
    static long[] aLongArray9465;
    private int anInt9466 = 1;
    static Class105[] aClass105Array9467;
    static int[] anIntArray9458;
    static int anInt9460;
    static int anInt9462;

    static final void method3146(int i, int i_0_, byte i_1_, int i_2_, int i_3_, int i_4_, byte[] is, byte[] is_5_, int i_6_) {
        try {
            anInt9464++;
            int i_7_ = -(i_0_ >> 2);
            i_0_ = -(i_0_ & 0x3);
            if (i_1_ >= -12) method3148(true);
            for (int i_8_ = -i_3_; i_8_ < 0; i_8_++) {
                for (int i_9_ = i_7_; i_9_ < 0; i_9_++) {
                    is[i_4_++] += -is_5_[i_6_++];
                    is[i_4_++] += -is_5_[i_6_++];
                    is[i_4_++] += -is_5_[i_6_++];
                    is[i_4_++] += -is_5_[i_6_++];
                }
                for (int i_10_ = i_0_; i_10_ < 0; i_10_++)
                    is[i_4_++] += -is_5_[i_6_++];
                i_4_ += i;
                i_6_ += i_2_;
            }
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("fd.D(" + i + ',' + i_0_ + ',' + i_1_ + ',' + i_2_ + ',' + i_3_ + ',' + i_4_ + ',' + (is != null ? "{...}" : "null") + ',' + (is_5_ != null ? "{...}" : "null") + ',' + i_6_ + ')'));
        }
    }

    final void method3049(Class348_Sub49 class348_sub49, int i, int i_11_) {
        anInt9460++;
        if (i_11_ != 31015) method3147((byte) 32);
        int i_12_ = i;
        while_212_:
        do {
            do {
                if (i_12_ == 0) {
                    anInt9466 = class348_sub49.readUnsignedByte(255);
                    return;
                } else if (i_12_ != 1) {
                    if (i_12_ == 2) break;
                    break while_212_;
                }
                anInt9463 = class348_sub49.readUnsignedByte(255);
                return;
            } while (false);
            this.aBoolean7045 = class348_sub49.readUnsignedByte(i_11_ ^ 0x79d8) == 1;
        } while (false);
    }

    /* NOTE: method3147 is NOT in the genuine-methods list (0 JaCoCo hits)
     * for this renderer, so it is stubbed rather than pulling in
     * Class248/Class97/Class259's world-state fields to compile a path
     * that never executes here. */
    static final void method3147(byte i) {
        if (i != 27) method3146(-85, 87, (byte) 91, 46, -77, 54, null, null, -117);
        anInt9459++;
        throw new IllegalStateException(); // unreachable per JaCoCo coverage
    }

    final int[][] method3047(int i, int i_28_) {
        if (i_28_ != -1564599039) return null;
        anInt9462++;
        int[][] is = this.aClass322_7033.method2557(i_28_ ^ 0x5d41e2a6, i);
        if (this.aClass322_7033.aBoolean4035) {
            int i_29_ = 1 + (anInt9463 + anInt9463);
            int i_30_ = 65536 / i_29_;
            int i_31_ = anInt9466 + (anInt9466 + 1);
            int i_32_ = 65536 / i_31_;
            int[][][] is_33_ = new int[i_29_][][];
            for (int i_34_ = i + -anInt9463; i_34_ <= anInt9463 + i; i_34_++) {
                int[][] is_35_ = this.method3039((byte) 55, Class299_Sub2.anInt6325 & i_34_, 0);
                int[][] is_36_ = new int[3][Class348_Sub40_Sub6.anInt9139];
                int i_37_ = 0;
                int i_38_ = 0;
                int i_39_ = 0;
                int[] is_40_ = is_35_[0];
                int[] is_41_ = is_35_[1];
                int[] is_42_ = is_35_[2];
                for (int i_43_ = -anInt9466; i_43_ <= anInt9466; i_43_++) {
                    int i_44_ = Class239_Sub22.anInt6076 & i_43_;
                    i_39_ += is_42_[i_44_];
                    i_37_ += is_40_[i_44_];
                    i_38_ += is_41_[i_44_];
                }
                int[] is_45_ = is_36_[0];
                int[] is_46_ = is_36_[1];
                int[] is_47_ = is_36_[2];
                int i_48_ = 0;
                while (Class348_Sub40_Sub6.anInt9139 > i_48_) {
                    is_45_[i_48_] = i_37_ * i_32_ >> 16;
                    is_46_[i_48_] = i_38_ * i_32_ >> 16;
                    is_47_[i_48_] = i_39_ * i_32_ >> 16;
                    int i_49_ = Class239_Sub22.anInt6076 & -anInt9466 + i_48_;
                    i_37_ -= is_40_[i_49_];
                    i_48_++;
                    i_38_ -= is_41_[i_49_];
                    i_39_ -= is_42_[i_49_];
                    i_49_ = i_48_ - -anInt9466 & Class239_Sub22.anInt6076;
                    i_39_ += is_42_[i_49_];
                    i_38_ += is_41_[i_49_];
                    i_37_ += is_40_[i_49_];
                }
                is_33_[i_34_ + anInt9463 + -i] = is_36_;
            }
            int[] is_50_ = is[0];
            int[] is_51_ = is[1];
            int[] is_52_ = is[2];
            for (int i_53_ = 0; Class348_Sub40_Sub6.anInt9139 > i_53_; i_53_++) {
                int i_54_ = 0;
                int i_55_ = 0;
                int i_56_ = 0;
                for (int i_57_ = 0; i_57_ < i_29_; i_57_++) {
                    int[][] is_58_ = is_33_[i_57_];
                    i_54_ += is_58_[0][i_53_];
                    i_55_ += is_58_[1][i_53_];
                    i_56_ += is_58_[2][i_53_];
                }
                is_50_[i_53_] = i_54_ * i_30_ >> 16;
                is_51_[i_53_] = i_55_ * i_30_ >> 16;
                is_52_[i_53_] = i_30_ * i_56_ >> 16;
            }
        }
        return is;
    }

    public Class348_Sub40_Sub37() {
        super(1, false);
    }

    public static void method3148(boolean bool) {
        anIntArray9458 = null;
        aLongArray9465 = null;
        if (bool != true) method3146(79, -54, (byte) -128, -109, 85, -107, null, null, 58);
        aClass105Array9467 = null;
    }

    static {
        anIntArray9458 = new int[1000];
    }
}
