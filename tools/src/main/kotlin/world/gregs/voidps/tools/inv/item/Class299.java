package world.gregs.voidps.tools.inv.item;/* Class299 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Class299 {
    static int anInt3814;

    static final int method2253(int i, int i_0_) {
        anInt3814++;
        int i_1_ = -124 % ((i_0_ - 55) / 63);
        int i_2_ = 0;
        if (i < 0 || i >= 65536) {
            i_2_ += 16;
            i >>>= 16;
        }
        if (i >= 256) {
            i >>>= 8;
            i_2_ += 8;
        }
        if (i >= 16) {
            i_2_ += 4;
            i >>>= 4;
        }
        if (i >= 4) {
            i_2_ += 2;
            i >>>= 2;
        }
        if (i >= 1) {
            i >>>= 1;
            i_2_++;
        }
        return i_2_ - -i;
    }
}
