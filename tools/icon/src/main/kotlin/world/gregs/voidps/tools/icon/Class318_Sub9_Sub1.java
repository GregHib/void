package world.gregs.voidps.tools.icon;/* Class318_Sub9_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class318_Sub9_Sub1 {
    static int anInt8788;
    static float aFloat8784;

    static final Class348_Sub42_Sub15 method2516(int i, byte i_7_, int i_8_) {
        anInt8788++;
        Class348_Sub42_Sub15 class348_sub42_sub15 = ((Class348_Sub42_Sub15) Class100.aIterableHashTable_1585.method3480(((long) i_8_ << 32 | (long) i), i_7_ ^ ~0x171e));
        if (i_7_ != 105) aFloat8784 = 0.99212307F;
        if (class348_sub42_sub15 == null) {
            class348_sub42_sub15 = new Class348_Sub42_Sub15(i_8_, i);
            Class100.aIterableHashTable_1585.put((byte) 91, (class348_sub42_sub15.aLong4291), class348_sub42_sub15);
        }
        return class348_sub42_sub15;
    }
}
