package world.gregs.voidps.tools.inv.item;/* Class191 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class191 {
    static int anInt2556;
    private int anInt2557 = 0;
    private final int anInt2559;
    private Class262 aClass262_2561;
    private int anInt2562 = -1;
    private Class348_Sub6[] aClass348_Sub6Array2563;
    private int[][] anIntArrayArray2564;
    private int anInt2565;
    static int anInt2558;
    static int anInt2566;
    boolean aBoolean2570;

    final int[][] method1427(byte i) {
        anInt2558++;
        if (anInt2559 != anInt2565) throw new RuntimeException("Can only retrieve a full image cache");
        for (int i_0_ = 0; anInt2559 > i_0_; i_0_++)
            aClass348_Sub6Array2563[i_0_] = aa_Sub3.aClass348_Sub6_5206;
        if (i != 16) method1427((byte) -42);
        return anIntArrayArray2564;
    }

    final void method1432(byte i) {
        for (int i_13_ = 0; i_13_ < anInt2559; i_13_++)
            anIntArrayArray2564[i_13_] = null;
        anInt2556++;
        aClass348_Sub6Array2563 = null;
        anIntArrayArray2564 = null;
        aClass262_2561.method1996(112);
        if (i != 124) anInt2565 = -126;
        aClass262_2561 = null;
    }

    final int[] method1433(int i, int i_14_) {
        if (i != 0) method1427((byte) 108);
        anInt2566++;
        if (anInt2559 == anInt2565) {
            this.aBoolean2570 = aClass348_Sub6Array2563[i_14_] == null;
            aClass348_Sub6Array2563[i_14_] = aa_Sub3.aClass348_Sub6_5206;
            return anIntArrayArray2564[i_14_];
        }
        if (anInt2559 != 1) {
            Class348_Sub6 class348_sub6 = aClass348_Sub6Array2563[i_14_];
            if (class348_sub6 == null) {
                this.aBoolean2570 = true;
                if (anInt2557 < anInt2559) {
                    class348_sub6 = new Class348_Sub6(i_14_, anInt2557);
                    anInt2557++;
                } else {
                    Class348_Sub6 class348_sub6_15_ = (Class348_Sub6) aClass262_2561.method1993(i + -123);
                    class348_sub6 = new Class348_Sub6(i_14_, class348_sub6_15_.anInt6636);
                    aClass348_Sub6Array2563[class348_sub6_15_.anInt6630] = null;
                    class348_sub6_15_.unlink((byte) 80);
                }
                aClass348_Sub6Array2563[i_14_] = class348_sub6;
            } else this.aBoolean2570 = false;
            aClass262_2561.method2001(class348_sub6, -90);
            return (anIntArrayArray2564[class348_sub6.anInt6636]);
        }
        this.aBoolean2570 = i_14_ != anInt2562;
        anInt2562 = i_14_;
        return anIntArrayArray2564[0];
    }

    Class191(int i, int i_16_, int i_17_) {
        aClass262_2561 = new Class262();
        this.aBoolean2570 = false;
        anInt2559 = i;
        anInt2565 = i_16_;
        aClass348_Sub6Array2563 = new Class348_Sub6[anInt2565];
        anIntArrayArray2564 = new int[anInt2559][i_17_];
    }
}
