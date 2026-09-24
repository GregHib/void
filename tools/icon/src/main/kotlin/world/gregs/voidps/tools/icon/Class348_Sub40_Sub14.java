package world.gregs.voidps.tools.icon;/* Class348_Sub40_Sub14 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub14 extends Class348_Sub40 {
    static int anInt9205;
    static int anInt9206;
    static int anInt9207;
    private int[] anIntArray9208;
    static int anInt9209;
    private int[][] anIntArrayArray9210;
    private int anInt9211 = 0;
    static int anInt9212;
    static int anInt9213;
    private int[] anIntArray9214;
    private short[] aShortArray9215 = new short[257];

    final void method3049(Packet packet, int i, int i_0_) {
        if (i == 0) {
            anInt9211 = packet.readUnsignedByte(i_0_ + -30760);
            anIntArrayArray9210 = new int[packet.readUnsignedByte(255)][2];
            for (int i_1_ = 0; (i_1_ < anIntArrayArray9210.length); i_1_++) {
                anIntArrayArray9210[i_1_][0] = packet.readUnsignedShort(842397944);
                anIntArrayArray9210[i_1_][1] = packet.readUnsignedShort(842397944);
            }
        }
        if (i_0_ == 31015) anInt9213++;
    }

    private final void method3081(int i) {
        int i_2_ = anInt9211;
        while_158_:
        do {
            do {
                if (i_2_ == 2) {
                    for (i_2_ = 0; i_2_ < 257; i_2_++) {
                        int i_3_ = i_2_ << 4;
                        int i_4_;
                        for (i_4_ = 1; -1 + anIntArrayArray9210.length > i_4_; i_4_++) {
                            if (anIntArrayArray9210[i_4_][0] > i_3_) break;
                        }
                        int[] is = anIntArrayArray9210[i_4_ - 1];
                        int[] is_5_ = anIntArrayArray9210[i_4_];
                        int i_6_ = method3083(-2 + i_4_, (byte) -120)[1];
                        int i_7_ = is[1];
                        int i_8_ = is_5_[1];
                        int i_9_ = method3083(1 + i_4_, (byte) 81)[1];
                        int i_10_ = ((-is[0] + i_3_ << 12) / (is_5_[0] + -is[0]));
                        int i_11_ = i_10_ * i_10_ >> 12;
                        int i_12_ = i_7_ + -i_8_ + (i_9_ + -i_6_);
                        int i_13_ = -i_12_ + i_6_ - i_7_;
                        int i_14_ = i_8_ + -i_6_;
                        int i_15_ = i_7_;
                        int i_16_ = ((i_10_ * i_12_ >> 12) * i_11_ >> 12);
                        int i_17_ = i_13_ * i_11_ >> 12;
                        int i_18_ = i_10_ * i_14_ >> 12;
                        int i_19_ = i_15_ + (i_18_ + i_17_ + i_16_);
                        if (i_19_ <= -32768) i_19_ = -32767;
                        if (i_19_ >= 32768) i_19_ = 32767;
                        aShortArray9215[i_2_] = (short) i_19_;
                    }
                    break while_158_;
                } else if (i_2_ != 1) break;
                for (i_2_ = 0; i_2_ < 257; i_2_++) {
                    int i_20_ = i_2_ << 4;
                    int i_21_;
                    for (i_21_ = 1; (i_21_ < -1 + anIntArrayArray9210.length); i_21_++) {
                        if (anIntArrayArray9210[i_21_][0] > i_20_) break;
                    }
                    int[] is = anIntArrayArray9210[-1 + i_21_];
                    int[] is_22_ = anIntArrayArray9210[i_21_];
                    int i_23_ = ((-is[0] + i_20_ << 12) / (is_22_[0] - is[0]));
                    int i_24_ = (-(Class127.anIntArray4654[(i_23_ & 0x1ff6) >> 5]) + 4096 >> 1);
                    int i_25_ = -i_24_ + 4096;
                    int i_26_ = is[1] * i_25_ + is_22_[1] * i_24_ >> 12;
                    if (i_26_ <= -32768) i_26_ = -32767;
                    if (i_26_ >= 32768) i_26_ = 32767;
                    aShortArray9215[i_2_] = (short) i_26_;
                }
                break while_158_;
            } while (false);
            for (i_2_ = 0; i_2_ < 257; i_2_++) {
                int i_27_ = i_2_ << 4;
                int i_28_;
                for (i_28_ = 1; (-1 + anIntArrayArray9210.length > i_28_); i_28_++) {
                    if (anIntArrayArray9210[i_28_][0] > i_27_) break;
                }
                int[] is = anIntArrayArray9210[i_28_ + -1];
                int[] is_29_ = anIntArrayArray9210[i_28_];
                int i_30_ = (i_27_ + -is[0] << 12) / (is_29_[0] + -is[0]);
                int i_31_ = 4096 - i_30_;
                int i_32_ = is_29_[1] * i_30_ + i_31_ * is[1] >> 12;
                if (i_32_ <= -32768) i_32_ = -32767;
                if (i_32_ >= 32768) i_32_ = 32767;
                aShortArray9215[i_2_] = (short) i_32_;
            }
        } while (false);
        if (i != -1) aShortArray9215 = null;
        anInt9205++;
    }

    public Class348_Sub40_Sub14() {
        super(1, true);
    }

    final int[] method3042(int i, int i_33_) {
        anInt9207++;
        int[] is = this.aClass191_7032.method1433(0, i);
        if (i_33_ != 255) anIntArray9214 = null;
        if (this.aClass191_7032.aBoolean2570) {
            int[] is_34_ = this.method3048(i, 633706337, 0);
            for (int i_35_ = 0; i_35_ < Class348_Sub40_Sub6.anInt9139; i_35_++) {
                int i_36_ = is_34_[i_35_] >> 4;
                if (i_36_ < 0) i_36_ = 0;
                if (i_36_ > 256) i_36_ = 256;
                is[i_35_] = aShortArray9215[i_36_];
            }
        }
        return is;
    }

    private final void method3082(byte i) {
        anInt9206++;
        int[] is = anIntArrayArray9210[0];
        int[] is_37_ = anIntArrayArray9210[1];
        int[] is_38_ = anIntArrayArray9210[-2 + anIntArrayArray9210.length];
        int[] is_39_ = anIntArrayArray9210[-1 + anIntArrayArray9210.length];
        anIntArray9208 = new int[]{is_38_[0] - (is_39_[0] + -is_38_[0]), is_38_[1] - (-is_38_[1] + is_39_[1])};
        if (i != 73) method3042(75, 39);
        anIntArray9214 = new int[]{is[0] + -is_37_[0] + is[0], is[1] - (-is[1] - -is_37_[1])};
    }

    final void method3044(int i) {
        if (anIntArrayArray9210 == null) anIntArrayArray9210 = new int[][]{new int[2], {4096, 4096}};
        if (i <= 108) anIntArrayArray9210 = null;
        anInt9209++;
        if (anIntArrayArray9210.length < 2) throw new RuntimeException("Curve operation requires at least two markers");
        if (anInt9211 == 2) method3082((byte) 73);
        Class220.method1605(26188);
        method3081(-1);
    }

    private final int[] method3083(int i, byte i_40_) {
        anInt9212++;
        if (i < 0) return anIntArray9214;
        int i_41_ = -48 % ((i_40_ - 13) / 56);
        if (anIntArrayArray9210.length <= i) return anIntArray9208;
        return anIntArrayArray9210[i];
    }
}
