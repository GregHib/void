package world.gregs.voidps.tools.inv.item;/* Class73 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class73 {
    static float[] aFloatArray4772;
    static int anInt4777;
    static Class114 aClass114_4779;
    static int[] anIntArray4780;
    static Class219 aClass219_4782;
    static int anInt4775;

    /* NOTE: called only from method742 behind "if (i != 104)"; every real
     * caller of method742 across the original client passes i==104, so this
     * is unreachable in practice but kept (with method743) so the genuine
     * method742 call site resolves at compile time. */
    public static void method741(byte i) {
        aFloatArray4772 = null;
        anIntArray4780 = null;
        if (i != -128) method743(113, -98);
        aClass114_4779 = null;
        aClass219_4782 = null;
    }

    static final void method743(int i, int i_2_) {
        anInt4775++;
        Class348_Sub42_Sub15 class348_sub42_sub15 = Class318_Sub9_Sub1.method2516(i_2_, (byte) 105, i);//9
        class348_sub42_sub15.method3251(i ^ ~0x3eb0);
    }

    static final Class189 method742(int i, int i_0_) {
        anInt4777++;
        Class189 class189 = (Class189) Class217.aClass60_2844.method583(i_0_, -104);
        if (class189 != null) return class189;
        byte[] is = Class369_Sub3.aClass45_8601.method410(-1860, 0, i_0_);
        if (i != 104) method741((byte) 98);
        class189 = new Class189();
        if (is != null) class189.method1419(i_0_, new Class348_Sub49(is), (byte) 64);
        Class217.aClass60_2844.method582(class189, i_0_, (byte) -114);
        return class189;
    }

    static {
        aFloatArray4772 = new float[16];
        anIntArray4780 = new int[]{104, 120, 136, 168};
        aClass114_4779 = new Class114(76, 6);
    }
}
