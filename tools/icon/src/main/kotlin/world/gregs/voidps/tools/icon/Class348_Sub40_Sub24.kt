package world.gregs.voidps.tools.icon;/* Class348_Sub40_Sub24 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub24 extends Class348_Sub40 {
    private int anInt9325 = 0;
    static int anInt9326;
    static int anInt9328;
    private int anInt9329 = 10;
    static int anInt9330;
    static int anInt9331;
    private int[] anIntArray9332;
    private int[] anIntArray9333;
    private int anInt9334 = 2048;

    final int[] method3042(int i, int i_0_) {
        if (i_0_ != 255) anIntArray9333 = null;
        anInt9328++;
        int[] is = this.aClass191_7032.method1433(0, i);
        if (this.aClass191_7032.aBoolean2570) {
            int i_1_ = Class239_Sub18.anIntArray6035[i];
            if (anInt9325 == 0) {
                int i_7_ = 0;
                for (int i_8_ = 0; i_8_ < anInt9329; i_8_++) {
                    if (i_1_ >= anIntArray9332[i_8_] && (i_1_ < anIntArray9332[i_8_ + 1])) {
                        if (anIntArray9333[i_8_] > i_1_) i_7_ = 4096;
                        break;
                    }
                }
                Class214.method1579(is, 0, Class348_Sub40_Sub6.anInt9139, i_7_);
            } else {
                for (int i_2_ = 0; i_2_ < Class348_Sub40_Sub6.anInt9139; i_2_++) {
                    int i_3_ = 0;
                    int i_4_ = 0;
                    int i_5_ = Class318_Sub6.anIntArray6432[i_2_];
                    int i_6_ = anInt9325;
                    while_197_:
                    do {
                        do {
                            if (i_6_ == 1) {
                                i_3_ = i_5_;
                                break while_197_;
                            } else if (i_6_ != 2) {
                                if (i_6_ == 3) break;
                                break while_197_;
                            }
                            i_3_ = ((i_5_ + (-4096 - -i_1_) >> 1) + 2048);
                            break while_197_;
                        } while (false);
                        i_3_ = 2048 + (i_5_ - i_1_ >> 1);
                    } while (false);
                    for (i_6_ = 0; i_6_ < anInt9329; i_6_++) {
                        if (i_3_ >= anIntArray9332[i_6_] && i_3_ < anIntArray9332[1 + i_6_]) {
                            if (i_3_ < anIntArray9333[i_6_]) i_4_ = 4096;
                            break;
                        }
                    }
                    is[i_2_] = i_4_;
                }
            }
        }
        return is;
    }

    final void method3044(int i) {
        if (i < 108) method3049(null, -68, -1);
        anInt9330++;
        method3116((byte) -111);
    }

    final void method3049(Packet packet, int i, int i_9_) {
        anInt9331++;
        int i_10_ = i;
        while_198_:
        do {
            do {
                if (i_10_ == 0) {
                    anInt9329 = packet.readUnsignedByte(255);
                    break while_198_;
                } else if (i_10_ != 1) {
                    if (i_10_ == 2) break;
                    break while_198_;
                }
                anInt9334 = packet.readUnsignedShort(842397944);
                break while_198_;
            } while (false);
            anInt9325 = packet.readUnsignedByte(i_9_ ^ 0x79d8);
        } while (false);
        if (i_9_ != 31015) method3049(null, 38, 1);
    }

    private final void method3116(byte i) {
        anInt9326++;
        int i_11_ = 0;
        anIntArray9333 = new int[anInt9329 - -1];
        anIntArray9332 = new int[anInt9329 - -1];
        int i_12_ = 4096 / anInt9329;
        int i_13_ = i_12_ * anInt9334 >> 12;
        for (int i_14_ = 0; anInt9329 > i_14_; i_14_++) {
            anIntArray9332[i_14_] = i_11_;
            anIntArray9333[i_14_] = i_13_ + i_11_;
            i_11_ += i_12_;
        }
        if (i != -111) anIntArray9332 = null;
        anIntArray9332[anInt9329] = 4096;
        anIntArray9333[anInt9329] = 4096 - -anIntArray9333[0];
    }

    public Class348_Sub40_Sub24() {
        super(0, true);
    }
}
