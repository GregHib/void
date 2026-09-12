package world.gregs.voidps.tools.inv.item;/* Class171 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class171 {
    static int anInt2267;

    static final int method1319(int i, boolean bool, byte[] is, int i_0_) {
        anInt2267++;
        if (bool != true) return 63;
        int i_1_ = -1;
        for (int i_2_ = i_0_; i_2_ < i; i_2_++)
            i_1_ = (i_1_ >>> 8 ^ Class89.anIntArray1508[(i_1_ ^ is[i_2_]) & 0xff]);
        i_1_ ^= 0xffffffff;
        return i_1_;
    }
}
