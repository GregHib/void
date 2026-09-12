package world.gregs.voidps.tools.inv.item;/* Class348_Sub42_Sub8_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub42_Sub8_Sub2 extends Class348_Sub42_Sub8 {
    private final Object anObject10429;
    static int anInt10435;
    static long[][][] aLongArrayArrayArray10431;
    static Class76 aClass76_10436 = new Class76(true);
    static Class74 aClass74_10437 = new Class74(0, 3);
    static int anInt10438;

    Class348_Sub42_Sub8_Sub2(Object object, int i) {
        super(i);
        anObject10429 = object;
    }

    final boolean method3195(int i) {
        if (i != -4) method3202((byte) -58);
        anInt10438++;
        return false;
    }

    public static void method3202(byte i) {
        if (i > 38) {
            aLongArrayArrayArray10431 = null;
            aClass76_10436 = null;
            aClass74_10437 = null;
        }
    }

    final Object method3193(int i) {
        anInt10435++;
        if (i < 75) method3193(-128);
        return anObject10429;
    }
}
