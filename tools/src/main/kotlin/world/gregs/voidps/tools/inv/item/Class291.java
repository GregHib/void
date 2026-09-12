package world.gregs.voidps.tools.inv.item;/* Class291 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class291 {
    int anInt3719;
    int[][] anIntArrayArray3721;
    int[] anIntArray3722;
    Class316 aClass316_3723;
    int[] anIntArray3724;
    int[] anIntArray3725;
    int anInt3727;
    Class316[] aClass316Array3728;
    int[] anIntArray3729;
    byte[][] aByteArrayArray3730;
    static int anInt3731;
    int anInt3732;
    int[] anIntArray3733;
    int anInt3734;
    int[][] anIntArrayArray3735;
    int[] anIntArray3738;
    private byte[] aByteArray3740;
    static int anInt3741;

    static final int method2198(int i, int i_0_, int i_1_) {
        i_0_ = i_0_ * (i_1_ & 0x7f) >> 7;
        if (i != 0) method2198(52, -11, 108);
        anInt3741++;
        if (i_0_ >= 2) {
            if (i_0_ > 126) i_0_ = 126;
        } else i_0_ = 2;
        return (0xff80 & i_1_) - -i_0_;
    }

    private final void method2200(byte i, byte[] is) {
        anInt3731++;
        Class348_Sub49 class348_sub49 = new Class348_Sub49(Class348_Sub41.method3158(is, -105));
        int i_2_ = class348_sub49.readUnsignedByte(255);
        if (i_2_ < 5 || i_2_ > 6) throw new RuntimeException();
        if (i_2_ < 6) this.anInt3732 = 0;
        else this.anInt3732 = class348_sub49.readInt((byte) -126);
        int i_3_ = class348_sub49.readUnsignedByte(255);
        boolean bool = (i_3_ & 0x1) != 0;
        boolean bool_4_ = (i_3_ & 0x2) != 0;
        this.anInt3727 = class348_sub49.readUnsignedShort(842397944);
        int i_5_ = 0;
        this.anIntArray3738 = new int[this.anInt3727];
        int i_6_ = -1;
        for (int i_7_ = 0; i_7_ < this.anInt3727; i_7_++) {
            this.anIntArray3738[i_7_] = i_5_ += class348_sub49.readUnsignedShort(842397944);
            if (i_6_ < this.anIntArray3738[i_7_]) i_6_ = this.anIntArray3738[i_7_];
        }
        this.anInt3734 = i_6_ - -1;
        if (bool_4_) this.aByteArrayArray3730 = new byte[this.anInt3734][];
        this.anIntArray3729 = new int[this.anInt3734];
        this.anIntArray3724 = new int[this.anInt3734];
        this.anIntArray3725 = new int[this.anInt3734];
        this.anIntArray3722 = new int[this.anInt3734];
        this.anIntArrayArray3721 = new int[this.anInt3734][];
        if (bool) {
            this.anIntArray3733 = new int[this.anInt3734];
            for (int i_8_ = 0; i_8_ < this.anInt3734; i_8_++)
                this.anIntArray3733[i_8_] = -1;
            for (int i_9_ = 0; (this.anInt3727 > i_9_); i_9_++)
                this.anIntArray3733[(this.anIntArray3738[i_9_])] = class348_sub49.readInt((byte) -126);
            this.aClass316_3723 = new Class316(this.anIntArray3733);
        }
        if (i >= -83) method2200((byte) 42, null);
        for (int i_10_ = 0; i_10_ < this.anInt3727; i_10_++)
            this.anIntArray3729[(this.anIntArray3738[i_10_])] = class348_sub49.readInt((byte) -126);
        if (bool_4_) {
            for (int i_11_ = 0; i_11_ < this.anInt3727; i_11_++) {
                byte[] is_12_ = new byte[64];
                class348_sub49.method3389(2147483647, 0, 64, is_12_);
                this.aByteArrayArray3730[(this.anIntArray3738[i_11_])] = is_12_;
            }
        }
        for (int i_13_ = 0; i_13_ < this.anInt3727; i_13_++)
            this.anIntArray3722[(this.anIntArray3738[i_13_])] = class348_sub49.readInt((byte) -126);
        for (int i_14_ = 0; this.anInt3727 > i_14_; i_14_++)
            this.anIntArray3725[(this.anIntArray3738[i_14_])] = class348_sub49.readUnsignedShort(842397944);
        for (int i_15_ = 0; this.anInt3727 > i_15_; i_15_++) {
            int i_16_ = this.anIntArray3738[i_15_];
            i_5_ = 0;
            int i_17_ = this.anIntArray3725[i_16_];
            int i_18_ = -1;
            this.anIntArrayArray3721[i_16_] = new int[i_17_];
            for (int i_19_ = 0; i_17_ > i_19_; i_19_++) {
                int i_20_ = (this.anIntArrayArray3721[i_16_][i_19_] = i_5_ += class348_sub49.readUnsignedShort(842397944));
                if (i_18_ < i_20_) i_18_ = i_20_;
            }
            this.anIntArray3724[i_16_] = i_18_ + 1;
            if (1 + i_18_ == i_17_) this.anIntArrayArray3721[i_16_] = null;
        }
        if (bool) {
            this.anIntArrayArray3735 = new int[i_6_ + 1][];
            this.aClass316Array3728 = new Class316[1 + i_6_];
            for (int i_21_ = 0; i_21_ < this.anInt3727; i_21_++) {
                int i_22_ = this.anIntArray3738[i_21_];
                int i_23_ = this.anIntArray3725[i_22_];
                this.anIntArrayArray3735[i_22_] = new int[this.anIntArray3724[i_22_]];
                for (int i_24_ = 0; this.anIntArray3724[i_22_] > i_24_; i_24_++)
                    this.anIntArrayArray3735[i_22_][i_24_] = -1;
                for (int i_25_ = 0; i_23_ > i_25_; i_25_++) {
                    int i_26_;
                    if (this.anIntArrayArray3721[i_22_] != null) i_26_ = (this.anIntArrayArray3721[i_22_][i_25_]);
                    else i_26_ = i_25_;
                    this.anIntArrayArray3735[i_22_][i_26_] = class348_sub49.readInt((byte) -126);
                }
                this.aClass316Array3728[i_22_] = new Class316(this.anIntArrayArray3735[i_22_]);
            }
        }
    }

    Class291(byte[] is, int i, byte[] is_27_) {
        try {
            this.anInt3719 = Class59_Sub1.method554(5126, is.length, is);
            if (i != this.anInt3719) throw new RuntimeException();
            if (is_27_ != null) {
                if (is_27_.length != 64) throw new RuntimeException();
                aByteArray3740 = Class348_Sub1_Sub2.method2730(4567, 0, is, is.length);
                for (int i_28_ = 0; i_28_ < 64; i_28_++) {
                    if (aByteArray3740[i_28_] != is_27_[i_28_]) throw new RuntimeException();
                }
            }
            method2200((byte) -120, is);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("vw.<init>(" + (is != null ? "{...}" : "null") + ',' + i + ',' + (is_27_ != null ? "{...}" : "null") + ')'));
        }
    }
}
