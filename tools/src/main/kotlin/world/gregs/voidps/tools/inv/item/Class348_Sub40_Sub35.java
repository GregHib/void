package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub35 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub35 extends Class348_Sub40 {
    static int anInt9440;
    static int anInt9441;
    static int anInt9442;
    static Class161 aClass161_9443;
    private int anInt9445 = 3216;
    static int anInt9446;
    private int anInt9447 = 3216;
    private int anInt9448 = 4096;
    private final int[] anIntArray9449 = new int[3];

    private final void method3143(int i) {
        anInt9440++;
        if (i >= -118) method3143(-88);
        double d = Math.cos((float) anInt9445 / 4096.0F);
        anIntArray9449[0] = (int) (d * Math.sin((float) anInt9447 / 4096.0F) * 4096.0);
        anIntArray9449[1] = (int) (d * Math.cos((float) anInt9447 / 4096.0F) * 4096.0);
        anIntArray9449[2] = (int) (Math.sin((float) anInt9445 / 4096.0F) * 4096.0);
        int i_0_ = anIntArray9449[0] * anIntArray9449[0] >> 12;
        int i_1_ = anIntArray9449[1] * anIntArray9449[1] >> 12;
        int i_2_ = anIntArray9449[2] * anIntArray9449[2] >> 12;
        int i_3_ = (int) (4096.0 * Math.sqrt(i_1_ + i_0_ + i_2_ >> 12));
        if (i_3_ != 0) {
            anIntArray9449[1] = (anIntArray9449[1] << 12) / i_3_;
            anIntArray9449[0] = (anIntArray9449[0] << 12) / i_3_;
            anIntArray9449[2] = (anIntArray9449[2] << 12) / i_3_;
        }
    }

    final int[] method3042(int i, int i_4_) {
        anInt9446++;
        int[] is = this.aClass191_7032.method1433(0, i);
        if (this.aClass191_7032.aBoolean2570) {
            int i_5_ = Class248.anInt3201 * anInt9448 >> 12;
            int[] is_6_ = this.method3048(Class299_Sub2.anInt6325 & -1 + i, 633706337, 0);
            int[] is_7_ = this.method3048(i, 633706337, 0);
            int[] is_8_ = this.method3048(Class299_Sub2.anInt6325 & i - -1, i_4_ + 633706082, 0);
            for (int i_9_ = 0; Class348_Sub40_Sub6.anInt9139 > i_9_; i_9_++) {
                int i_10_ = i_5_ * (is_8_[i_9_] - is_6_[i_9_]) >> 12;
                int i_11_ = (i_5_ * (-is_7_[Class239_Sub22.anInt6076 & i_9_ - -1] + is_7_[-1 + i_9_ & Class239_Sub22.anInt6076]) >> 12);
                int i_12_ = i_11_ >> 4;
                int i_13_ = i_10_ >> 4;
                if (i_12_ < 0) i_12_ = -i_12_;
                if (i_13_ < 0) i_13_ = -i_13_;
                if (i_12_ > 255) i_12_ = 255;
                if (i_13_ > 255) i_13_ = 255;
                int i_14_ = (Class46.aByteArray821[i_12_ + (i_13_ * (1 + i_13_) >> 1)] & 0xff);
                int i_15_ = i_14_ * 4096 >> 8;
                int i_16_ = i_14_ * i_11_ >> 8;
                int i_17_ = i_10_ * i_14_ >> 8;
                i_17_ = i_17_ * anIntArray9449[1] >> 12;
                i_16_ = i_16_ * anIntArray9449[0] >> 12;
                i_15_ = anIntArray9449[2] * i_15_ >> 12;
                is[i_9_] = i_15_ + i_16_ - -i_17_;
            }
        }
        if (i_4_ != 255) return null;
        return is;
    }

    final void method3044(int i) {
        if (i <= 108) method3143(38);
        anInt9441++;
        method3143(-119);
    }

    final void method3049(Packet packet, int i, int i_18_) {
        int i_19_ = i;
        while_210_:
        do {
            do {
                if (i_19_ == 0) {
                    anInt9448 = packet.readUnsignedShort(i_18_ ^ 0x323581df);
                    break while_210_;
                } else if (i_19_ != 1) {
                    if (i_19_ == 2) break;
                    break while_210_;
                }
                anInt9447 = packet.readUnsignedShort(842397944);
                break while_210_;
            } while (false);
            anInt9445 = packet.readUnsignedShort(842397944);
        } while (false);
        anInt9442++;
        if (i_18_ != 31015) aClass161_9443 = null;
    }

    public Class348_Sub40_Sub35() {
        super(1, true);
    }
}
