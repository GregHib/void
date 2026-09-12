package world.gregs.voidps.tools.inv.item;/* Class322 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class322 {
    static int anInt4019;
    private final int anInt4020;
    private int anInt4022;
    static int anInt4023;
    private final int anInt4024;
    private int anInt4025 = 0;
    private int[][][] anIntArrayArrayArray4029;
    static int anInt4030;
    static int anInt4032 = -1;
    private Class348_Sub24[] aClass348_Sub24Array4033;
    static int anInt4034;
    boolean aBoolean4035;
    private Class262 aClass262_4021;

    static final void method2554(byte i) {
        if (i != -45) anInt4032 = 61;
        anInt4030++;
        if (Class312.anInt3931 == 1 || Class312.anInt3931 == 3 || (Class312.anInt3931 != Class83.anInt1447 && (Class312.anInt3931 == 0 || Class83.anInt1447 == 0))) {
            Class348_Sub32.anInt6930 = 0;
            Class150.anInt2057 = 0;
            Class282.aIterableHashTable_3654.method3481(0);
        }
        Class83.anInt1447 = Class312.anInt3931;
    }

    final int[][] method2557(int i, int i_6_) {
        anInt4034++;
        if (i >= -75) method2554((byte) -61);
        if (anInt4020 != anInt4024) {
            if (anInt4020 == 1) {
                this.aBoolean4035 = i_6_ != anInt4022;
                anInt4022 = i_6_;
                return anIntArrayArrayArray4029[0];
            }
            Class348_Sub24 class348_sub24 = aClass348_Sub24Array4033[i_6_];
            if (class348_sub24 == null) {
                this.aBoolean4035 = true;
                if (anInt4020 <= anInt4025) {
                    Class348_Sub24 class348_sub24_7_ = (Class348_Sub24) aClass262_4021.method1993(-126);
                    class348_sub24 = new Class348_Sub24(i_6_, class348_sub24_7_.anInt6875);
                    aClass348_Sub24Array4033[class348_sub24_7_.anInt6872] = null;
                    class348_sub24_7_.unlink((byte) 56);
                } else {
                    class348_sub24 = new Class348_Sub24(i_6_, anInt4025);
                    anInt4025++;
                }
                aClass348_Sub24Array4033[i_6_] = class348_sub24;
            } else this.aBoolean4035 = false;
            aClass262_4021.method2001(class348_sub24, -110);
            return (anIntArrayArrayArray4029[class348_sub24.anInt6875]);
        }
        this.aBoolean4035 = aClass348_Sub24Array4033[i_6_] == null;
        aClass348_Sub24Array4033[i_6_] = Class341.aClass348_Sub24_4226;
        return anIntArrayArrayArray4029[i_6_];
    }

    final void method2558(int i) {
        anInt4019++;
        if (i != 6144) anIntArrayArrayArray4029 = null;
        for (int i_8_ = 0; anInt4020 > i_8_; i_8_++) {
            anIntArrayArrayArray4029[i_8_][0] = null;
            anIntArrayArrayArray4029[i_8_][1] = null;
            anIntArrayArrayArray4029[i_8_][2] = null;
            anIntArrayArrayArray4029[i_8_] = null;
        }
        aClass348_Sub24Array4033 = null;
        anIntArrayArrayArray4029 = null;
        aClass262_4021.method1996(99);
        aClass262_4021 = null;
    }

    Class322(int i, int i_9_, int i_10_) {
        anInt4022 = -1;
        aClass262_4021 = new Class262();
        this.aBoolean4035 = false;
        anInt4020 = i;
        anInt4024 = i_9_;
        aClass348_Sub24Array4033 = new Class348_Sub24[anInt4024];
        anIntArrayArrayArray4029 = new int[anInt4020][3][i_10_];
    }
}
