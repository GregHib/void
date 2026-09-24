package world.gregs.voidps.tools.icon;/* Class348_Sub40_Sub17 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

class Class348_Sub40_Sub17 extends Class348_Sub40 {
    int[] anIntArray9232;
    static int anInt9233;
    int anInt9237;
    static int anInt9238;
    static int anInt9239;
    int anInt9241;
    static boolean aBoolean9242;
    private int anInt9243 = -1;
    static int anInt9235;
    static int anInt9236;

    final boolean method3090(boolean bool) {
        anInt9235++;
        if (bool != true) return true;
        if (this.anIntArray9232 != null) return true;
        if (anInt9243 >= 0) {
            Class207 class207 = (Matrix_Sub2.anInt5713 >= 0 ? Class207.method1521(Class348.aJs5_4286, Matrix_Sub2.anInt5713, anInt9243) : Class207.method1512(Class348.aJs5_4286, anInt9243));
            class207.method1524();
            this.anIntArray9232 = class207.method1516();
            this.anInt9237 = class207.anInt2702;
            this.anInt9241 = class207.anInt2696;
            return true;
        }
        return false;
    }

    public Class348_Sub40_Sub17() {
        super(0, false);
    }

    final void method3049(Packet packet, int i, int i_0_) {
        if (i_0_ == 31015) {
            if (i == 0) anInt9243 = packet.readUnsignedShort(842397944);
            anInt9236++;
        }
    }

    final int method3037(int i) {
        anInt9233++;
        if (i > -113) aBoolean9242 = false;
        return anInt9243;
    }

    final void method3046(byte i) {
        super.method3046((byte) -112);
        if (i < -102) {
            anInt9238++;
            this.anIntArray9232 = null;
        }
    }

    int[][] method3047(int i, int i_1_) {
        anInt9239++;
        if (i_1_ != -1564599039) method3047(8, -86);
        int[][] is = this.aClass322_7033.method2557(-108, i);
        if (this.aClass322_7033.aBoolean4035 && method3090(true)) {
            int[] is_2_ = is[0];
            int[] is_3_ = is[1];
            int[] is_4_ = is[2];
            int i_5_ = (this.anInt9237 * ((this.anInt9241 != Class286_Sub2.anInt6212) ? (this.anInt9241 * i / Class286_Sub2.anInt6212) : i));
            if (Class348_Sub40_Sub6.anInt9139 == this.anInt9237) {
                for (int i_6_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_6_); i_6_++) {
                    int i_7_ = this.anIntArray9232[i_5_++];
                    is_4_[i_6_] = Class139.method1166(4080, i_7_ << 4);
                    is_3_[i_6_] = Class139.method1166(65280, i_7_) >> 4;
                    is_2_[i_6_] = Class139.method1166(4080, i_7_ >> 12);
                }
            } else {
                for (int i_8_ = 0; (Class348_Sub40_Sub6.anInt9139 > i_8_); i_8_++) {
                    int i_9_ = (this.anInt9237 * i_8_ / Class348_Sub40_Sub6.anInt9139);
                    int i_10_ = (this.anIntArray9232[i_9_ + i_5_]);
                    is_4_[i_8_] = Class139.method1166(i_10_, 255) << 4;
                    is_3_[i_8_] = Class139.method1166(i_10_ >> 4, 4080);
                    is_2_[i_8_] = Class139.method1166(i_10_, 16711680) >> 12;
                }
            }
        }
        return is;
    }
}
