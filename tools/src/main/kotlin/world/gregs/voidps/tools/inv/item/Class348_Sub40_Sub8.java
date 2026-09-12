package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub8 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub8 extends Class348_Sub40 {
    static int anInt9148;
    int anInt9149 = 1638;
    int anInt9150;
    static int anInt9151;
    private byte[] aByteArray9152 = new byte[512];
    static int anInt9153;
    static int anInt9154;
    static int anInt9155;
    int anInt9156;
    int anInt9158;
    private short[] aShortArray9159;
    boolean aBoolean9160 = true;
    static int anInt9161;
    private short[] aShortArray9162;
    int anInt9164;
    static Class238 aClass238_9165;

    final void method3049(Class348_Sub49 class348_sub49, int i, int i_0_) {
        int i_1_ = i;
        while_151_:
        do {
            while_150_:
            do {
                while_149_:
                do {
                    while_148_:
                    do {
                        while_147_:
                        do {
                            do {
                                if (i_1_ == 0) {
                                    this.aBoolean9160 = class348_sub49.readUnsignedByte(i_0_ ^ 0x79d8) == 1;
                                    break while_151_;
                                } else if (i_1_ != 1) {
                                    if (i_1_ != 2) {
                                        if (i_1_ != 3) {
                                            if (i_1_ != 4) {
                                                if (i_1_ != 5) {
                                                    if (i_1_ == 6) break while_150_;
                                                    break while_151_;
                                                }
                                            } else break while_148_;
                                            break while_149_;
                                        }
                                    } else break;
                                    break while_147_;
                                }
                                this.anInt9150 = class348_sub49.readUnsignedByte(i_0_ ^ 0x79d8);
                                break while_151_;
                            } while (false);
                            this.anInt9149 = class348_sub49.readShort(13638);
                            if (this.anInt9149 < 0) {
                                aShortArray9159 = new short[(this.anInt9150)];
                                for (i_1_ = 0; (this.anInt9150 > i_1_); i_1_++)
                                    aShortArray9159[i_1_] = (short) class348_sub49.readShort(13638);
                            }
                            break while_151_;
                        } while (false);
                        this.anInt9158 = this.anInt9164 = class348_sub49.readUnsignedByte(255);
                        break while_151_;
                    } while (false);
                    this.anInt9156 = class348_sub49.readUnsignedByte(255);
                    break while_151_;
                } while (false);
                this.anInt9158 = class348_sub49.readUnsignedByte(255);
                break while_151_;
            } while (false);
            this.anInt9164 = class348_sub49.readUnsignedByte(255);
        } while (false);
        if (i_0_ != 31015) method3070(20, 127, -38, 124, -110, true, 16);
        anInt9153++;
    }

    final int[] method3042(int i, int i_2_) {
        anInt9154++;
        int[] is = this.aClass191_7032.method1433(0, i);
        if (this.aClass191_7032.aBoolean2570) method3069(i, is, (byte) 98);
        return is;
    }

    public Class348_Sub40_Sub8() {
        super(0, true);
        this.anInt9150 = 4;
        this.anInt9156 = 0;
        this.anInt9164 = 4;
        this.anInt9158 = 4;
    }

    final void method3044(int i) {
        aByteArray9152 = ha.method3664(this.anInt9156, 95);
        anInt9148++;
        method3067((byte) -98);
        int i_3_ = this.anInt9150 + -1;
        if (i < 108) aClass238_9165 = null;
        for (/**/; i_3_ >= 1; i_3_--) {
            short i_4_ = aShortArray9159[i_3_];
            if (i_4_ > 8) break;
            if (i_4_ < -8) break;
            this.anInt9150--;
        }
    }

    private final void method3067(byte i) {
        int i_5_ = 25 / ((i - -28) / 57);
        if (this.anInt9149 > 0) {
            aShortArray9162 = new short[this.anInt9150];
            aShortArray9159 = new short[this.anInt9150];
            for (int i_6_ = 0; this.anInt9150 > i_6_; i_6_++) {
                aShortArray9159[i_6_] = (short) (int) ((Math.pow((float) this.anInt9149 / 4096.0F, i_6_)) * 4096.0);
                aShortArray9162[i_6_] = (short) (int) Math.pow(2.0, i_6_);
            }
        } else if (aShortArray9159 != null && (this.anInt9150 == aShortArray9159.length)) {
            aShortArray9162 = new short[this.anInt9150];
            for (int i_7_ = 0; (i_7_ < this.anInt9150); i_7_++)
                aShortArray9162[i_7_] = (short) (int) Math.pow(2.0, i_7_);
        }
        anInt9151++;
    }

    final void method3069(int i, int[] is, byte i_8_) {
        anInt9161++;
        int i_9_ = (Class239_Sub18.anIntArray6035[i] * this.anInt9164);
        if (i_8_ > 91) {
            if (this.anInt9150 == 1) {
                int i_39_ = aShortArray9162[0] << 12;
                int i_40_ = aShortArray9159[0];
                int i_41_ = i_39_ * i_9_ >> 12;
                int i_42_ = (i_39_ * this.anInt9158 >> 12);
                int i_43_ = (this.anInt9164 * i_39_ >> 12);
                int i_44_ = i_41_ >> 12;
                int i_45_ = 1 + i_44_;
                i_41_ &= 0xfff;
                if (i_45_ >= i_43_) i_45_ = 0;
                int i_46_ = aByteArray9152[0xff & i_45_] & 0xff;
                int i_47_ = Class199.anIntArray2631[i_41_];
                int i_48_ = aByteArray9152[0xff & i_44_] & 0xff;
                if (this.aBoolean9160) {
                    for (int i_52_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_52_); i_52_++) {
                        int i_53_ = (this.anInt9158 * Class318_Sub6.anIntArray6432[i_52_]);
                        int i_54_ = method3070(i_46_, i_41_, i_42_, i_53_ * i_39_ >> 12, i_47_, true, i_48_);
                        i_54_ = i_40_ * i_54_ >> 12;
                        is[i_52_] = (i_54_ >> 1) + 2048;
                    }
                } else {
                    for (int i_49_ = 0; i_49_ < Class348_Sub40_Sub6.anInt9139; i_49_++) {
                        int i_50_ = (this.anInt9158 * Class318_Sub6.anIntArray6432[i_49_]);
                        int i_51_ = method3070(i_46_, i_41_, i_42_, i_39_ * i_50_ >> 12, i_47_, true, i_48_);
                        is[i_49_] = i_51_ * i_40_ >> 12;
                    }
                }
            } else {
                int i_10_ = aShortArray9159[0];
                if (i_10_ > 8 || i_10_ < -8) {
                    int i_11_ = aShortArray9162[0] << 12;
                    int i_12_ = i_11_ * i_9_ >> 12;
                    int i_13_ = (this.anInt9158 * i_11_ >> 12);
                    int i_14_ = (this.anInt9164 * i_11_ >> 12);
                    int i_15_ = i_12_ >> 12;
                    int i_16_ = 1 + i_15_;
                    if (i_16_ >= i_14_) i_16_ = 0;
                    i_12_ &= 0xfff;
                    int i_17_ = aByteArray9152[0xff & i_16_] & 0xff;
                    int i_18_ = aByteArray9152[0xff & i_15_] & 0xff;
                    int i_19_ = Class199.anIntArray2631[i_12_];
                    for (int i_20_ = 0; Class348_Sub40_Sub6.anInt9139 > i_20_; i_20_++) {
                        int i_21_ = (Class318_Sub6.anIntArray6432[i_20_] * this.anInt9158);
                        int i_22_ = method3070(i_17_, i_12_, i_13_, i_11_ * i_21_ >> 12, i_19_, true, i_18_);
                        is[i_20_] = i_10_ * i_22_ >> 12;
                    }
                }
                for (int i_23_ = 1; this.anInt9150 > i_23_; i_23_++) {
                    i_10_ = aShortArray9159[i_23_];
                    if (i_10_ > 8 || i_10_ < -8) {
                        int i_24_ = aShortArray9162[i_23_] << 12;
                        int i_25_ = i_24_ * i_9_ >> 12;
                        int i_26_ = (this.anInt9158 * i_24_ >> 12);
                        int i_27_ = (this.anInt9164 * i_24_ >> 12);
                        int i_28_ = i_25_ >> 12;
                        int i_29_ = 1 + i_28_;
                        i_25_ &= 0xfff;
                        if (i_29_ >= i_27_) i_29_ = 0;
                        int i_30_ = aByteArray9152[i_28_ & 0xff] & 0xff;
                        int i_31_ = Class199.anIntArray2631[i_25_];
                        int i_32_ = 0xff & aByteArray9152[i_29_ & 0xff];
                        if (this.aBoolean9160 && (this.anInt9150 - 1 == i_23_)) {
                            for (int i_33_ = 0; Class348_Sub40_Sub6.anInt9139 > i_33_; i_33_++) {
                                int i_34_ = (Class318_Sub6.anIntArray6432[i_33_] * (this.anInt9158));
                                int i_35_ = method3070(i_32_, i_25_, i_26_, i_34_ * i_24_ >> 12, i_31_, true, i_30_);
                                i_35_ = is[i_33_] - -(i_35_ * i_10_ >> 12);
                                is[i_33_] = (i_35_ >> 1) + 2048;
                            }
                        } else {
                            for (int i_36_ = 0; (i_36_ < Class348_Sub40_Sub6.anInt9139); i_36_++) {
                                int i_37_ = (this.anInt9158 * Class318_Sub6.anIntArray6432[i_36_]);
                                int i_38_ = method3070(i_32_, i_25_, i_26_, i_37_ * i_24_ >> 12, i_31_, true, i_30_);
                                is[i_36_] += i_10_ * i_38_ >> 12;
                            }
                        }
                    }
                }
            }
        }
    }

    private final int method3070(int i, int i_55_, int i_56_, int i_57_, int i_58_, boolean bool, int i_59_) {
        anInt9155++;
        int i_60_ = i_57_ >> 12;
        int i_61_ = i_60_ - -1;
        i_60_ &= 0xff;
        i_57_ &= 0xfff;
        if (i_56_ <= i_61_) i_61_ = 0;
        i_61_ &= 0xff;
        int i_62_ = -4096 + i_57_;
        int i_63_ = i_55_ + -4096;
        int i_64_ = 0x3 & aByteArray9152[i_59_ + i_60_];
        int i_65_ = Class199.anIntArray2631[i_57_];
        int i_66_;
        if (i_64_ > 1) i_66_ = i_64_ == 2 ? i_57_ - i_55_ : -i_55_ + -i_57_;
        else i_66_ = i_64_ != 0 ? i_55_ - i_57_ : i_57_ + i_55_;
        i_64_ = 0x3 & aByteArray9152[i_59_ + i_61_];
        int i_67_;
        if (i_64_ <= 1) i_67_ = i_64_ == 0 ? i_62_ + i_55_ : i_55_ + -i_62_;
        else i_67_ = i_64_ != 2 ? -i_55_ + -i_62_ : -i_55_ + i_62_;
        i_64_ = aByteArray9152[i + i_60_] & 0x3;
        if (bool != true) return -82;
        int i_68_ = i_66_ - -(i_65_ * (i_67_ + -i_66_) >> 12);
        if (i_64_ > 1) i_66_ = i_64_ == 2 ? -i_63_ + i_57_ : -i_57_ - i_63_;
        else i_66_ = i_64_ != 0 ? -i_57_ + i_63_ : i_57_ - -i_63_;
        i_64_ = 0x3 & aByteArray9152[i + i_61_];
        if (i_64_ > 1) i_67_ = i_64_ == 2 ? -i_63_ + i_62_ : -i_62_ - i_63_;
        else i_67_ = i_64_ == 0 ? i_62_ - -i_63_ : i_63_ + -i_62_;
        int i_69_ = ((-i_66_ + i_67_) * i_65_ >> 12) + i_66_;
        return i_68_ - -(i_58_ * (-i_68_ + i_69_) >> 12);
    }
}
