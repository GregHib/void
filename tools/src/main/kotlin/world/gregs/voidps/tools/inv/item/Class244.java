package world.gregs.voidps.tools.inv.item;/* Class244 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class244 implements d {
    static int anInt4612;
    static int anInt4614;
    static int anInt4616;
    static int anInt4617;
    static int anInt4618;
    static long aLong4615;
    private final Class12[] aClass12Array4611;
    static int anInt4620;
    static int anInt4621;
    private final Class45 aClass45_4619;
    private final Class308 aClass308_4622 = new Class308(256);
    private final Class45 aClass45_4624;
    private final int anInt4625;

    public final int[] method6(int i, int i_0_, float f, int i_1_, boolean bool, int i_2_) {
        if (i != -21540) method3(-46, 6);
        anInt4617++;
        return method1881(i_1_, false).method3183(this, i_2_, i_0_, (aClass12Array4611[i_1_].aBoolean207), f, aClass45_4624, (byte) 11);
    }

    private final Class348_Sub42_Sub5 method1881(int i, boolean bool) {
        anInt4620++;
        Class348_Sub42 class348_sub42 = aClass308_4622.method2302(i, (byte) -34);
        if (class348_sub42 != null) return (Class348_Sub42_Sub5) class348_sub42;
        byte[] is = aClass45_4619.method415((byte) 73, i);
        if (is == null) return null;
        if (bool != false) method1(-58, 1.9039171F, false, -106, -22, -18);
        Class348_Sub42_Sub5 class348_sub42_sub5 = new Class348_Sub42_Sub5(new Class348_Sub49(is));
        aClass308_4622.method2305(i, class348_sub42_sub5, -1);
        return class348_sub42_sub5;
    }

    public final Class12 method3(int i, int i_3_) {
        anInt4621++;
        if (i_3_ != -6662) return null;
        return aClass12Array4611[i];
    }

    public final int[] method5(boolean bool, int i, float f, int i_4_, int i_5_, int i_6_) {
        int i_7_ = 41 / ((i_6_ - -69) / 48);
        anInt4614++;
        return method1881(i, false).method3185(i_4_, this, 0, aClass12Array4611[i].aBoolean207, f, bool, aClass45_4624, i_5_);
    }

    public final boolean method4(int i, int i_8_) {
        anInt4616++;
        Class348_Sub42_Sub5 class348_sub42_sub5 = method1881(i_8_, false);
        if (i != -7953) method4(56, -109);
        return class348_sub42_sub5 != null && class348_sub42_sub5.method3184(this, aClass45_4624, -85);
    }

    /* NOTE: method1 is NOT in the genuine-methods list (0 JaCoCo hits) for
     * this renderer, so it is stubbed rather than pulling in
     * Class348_Sub42_Sub5.method3186 (also not genuine) to compile a path
     * that never executes here. */
    public final float[] method1(int i, float f, boolean bool, int i_9_, int i_10_, int i_11_) {
        if (i_9_ != -30824) return null;
        anInt4612++;
        throw new IllegalStateException(); // unreachable per JaCoCo coverage
    }

    public final int method2(boolean bool) {
        anInt4618++;
        if (bool != true) aLong4615 = -52L;
        return anInt4625;
    }

    Class244(Class45 class45, Class45 class45_12_, Class45 class45_13_) {
        try {
            aClass45_4619 = class45_12_;
            aClass45_4624 = class45_13_;
            Class348_Sub49 class348_sub49 = new Class348_Sub49(class45.method410(-1860, 0, 0));
            anInt4625 = class348_sub49.readUnsignedShort(842397944);
            aClass12Array4611 = new Class12[anInt4625];
            for (int i = 0; anInt4625 > i; i++) {
                if (class348_sub49.readUnsignedByte(255) == 1) aClass12Array4611[i] = new Class12();
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean209 = class348_sub49.readUnsignedByte(255) == 0;
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean199 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean204 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte216 = class348_sub49.readByte(-85);
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte201 = class348_sub49.readByte(-113);
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte213 = class348_sub49.readByte(-97);
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte202 = class348_sub49.readByte(-82);
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aShort208 = (short) class348_sub49.readUnsignedShort(842397944);
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte198 = class348_sub49.readByte(-86);
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte211 = class348_sub49.readByte(-104);
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean212 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean207 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aByte205 = class348_sub49.readByte(-77);
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean217 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean215 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].aBoolean218 = class348_sub49.readUnsignedByte(255) == 1;
            }
            for (int i = 0; i < anInt4625; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].anInt203 = class348_sub49.readUnsignedByte(255);
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].anInt206 = class348_sub49.readInt((byte) -126);
            }
            for (int i = 0; anInt4625 > i; i++) {
                if (aClass12Array4611[i] != null) aClass12Array4611[i].anInt200 = class348_sub49.readUnsignedByte(255);
            }
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("tda.<init>(" + (class45 != null ? "{...}" : "null") + ',' + (class45_12_ != null ? "{...}" : "null") + ',' + (class45_13_ != null ? "{...}" : "null") + ')'));
        }
    }
}
