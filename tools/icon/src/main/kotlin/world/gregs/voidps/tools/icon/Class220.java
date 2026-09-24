package world.gregs.voidps.tools.icon;/* Class220 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class220 {
    static int anInt2876;
    static int anInt2878;

    static final void method1605(int i) {
        if (Class235.anIntArray3068 == null || Class127.anIntArray4654 == null) {
            Class127.anIntArray4654 = new int[256];
            Class235.anIntArray3068 = new int[256];
            for (int i_0_ = 0; i_0_ < 256; i_0_++) {
                double d = 6.283185307179586 * ((double) i_0_ / 255.0);
                Class235.anIntArray3068[i_0_] = (int) (4096.0 * Math.sin(d));
                Class127.anIntArray4654[i_0_] = (int) (4096.0 * Math.cos(d));
            }
        }
        anInt2878++;
        if (i != 26188) method1606(-76, 98, 86);
    }

    static final byte method1606(int i, int i_1_, int i_2_) {
        anInt2876++;
        if (i_2_ != 9) return (byte) 0;
        if (i_1_ != -27939) return (byte) -50;
        if ((i & 0x1) == 0) return (byte) 1;
        return (byte) 2;
    }
}
