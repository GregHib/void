package world.gregs.voidps.tools.inv.item;/* Class25 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class25 {
    static int anInt362;

    static final short method303(int i, int i_3_) {
        anInt362++;
        int i_4_ = (i & 0xfe66) >> 10;
        int i_5_ = i >> 3 & 0x70;
        int i_6_ = i & 0x7f;
        i_5_ = (i_6_ <= 64 ? i_6_ * i_5_ >> 7 : i_5_ * (127 + -i_6_) >> 7);
        int i_7_ = i_5_ + i_6_;
        int i_8_;
        if (i_7_ != 0) i_8_ = (i_5_ << 8) / i_7_;
        else i_8_ = i_5_ << 1;
        int i_9_ = i_7_;
        if (i_3_ != 30) return (short) 79;
        return (short) (i_9_ | (i_8_ >> 4 << 7 | i_4_ << 10));
    }
}
