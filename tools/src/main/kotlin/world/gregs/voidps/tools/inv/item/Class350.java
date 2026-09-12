package world.gregs.voidps.tools.inv.item;/* Class350 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class350 {
    int anInt4307;
    int anInt4308;
    int anInt4309;
    int anInt4310;
    int anInt4311 = 128;
    int anInt4312;
    int anInt4313;
    int anInt4314 = 128;
    static int anInt4315;
    int anInt4316;
    int anInt4317;
    int anInt4320;

    static final int method3452(int i, byte i_0_, int i_1_) {
        if (i_0_ != -15) method3452(80, (byte) 123, -88);
        anInt4315++;
        int i_2_ = i_1_ + -1 & i >> 31;
        return (i + (i >>> 31)) % i_1_ + i_2_;
    }

    Class350(int i) {
        this.anInt4313 = i;
    }
}
