package world.gregs.voidps.tools.inv.item;/* Class6 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class6 {
    /* Minimal stub: only the fields/method/constructor that Class64_Sub1's
     * kept code actually reads/calls are included (verbatim from the
     * original), matching the full Class6(...) constructor signature used
     * at the aClass6Array5361[...] = new Class6(...) call site. */
    short aShort143;
    int anInt144;
    boolean aBoolean145;
    short aShort146;
    byte aByte148;
    short aShort150;
    int anInt154;
    byte aByte156;

    static final int method206(int i, int i_31_, int i_32_) {
        int i_33_ = i_31_ >>> 24;
        int i_34_ = -i_33_ + i_32_;
        i_31_ = (0xff0000 & (i_31_ & 0xff00) * i_33_ | (0xff00ff & i_31_) * i_33_ & ~0xff00ff) >>> 8;
        return i_31_ + (((i & 0xff00) * i_34_ & 0xff0000 | ~0xff00ff & (0xff00ff & i) * i_34_) >>> 8);
    }

    Class6(int i, int i_46_, int i_47_, int i_48_, int i_49_, int i_50_, int i_51_, int i_52_, int i_53_, boolean bool, int i_54_) {
        this.aBoolean145 = bool;
        this.aByte156 = (byte) i_53_;
        this.anInt154 = i_54_;
        this.aShort143 = (short) i_50_;
        this.aByte148 = (byte) i_52_;
        this.aShort146 = (short) i_51_;
        this.aShort150 = (short) i_49_;
        this.anInt144 = i;
    }
}
