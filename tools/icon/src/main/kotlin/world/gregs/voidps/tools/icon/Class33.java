package world.gregs.voidps.tools.icon;/* Class33 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class33 {
    static int anInt459;

    static final int method340(int i, byte i_5_) {
        anInt459++;
        i = --i | i >>> 1;
        i |= i >>> 2;
        i |= i >>> 4;
        if (i_5_ != 108) return 34;
        i |= i >>> 8;
        i |= i >>> 16;
        return 1 + i;
    }
}
