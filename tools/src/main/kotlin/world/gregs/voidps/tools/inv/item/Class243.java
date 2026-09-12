package world.gregs.voidps.tools.inv.item;/* Class243 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class243 {
    static int anInt3158;
    static int anInt3159;
    static int anInt3160;
    static int anInt3162;
    static int anInt3163;
    private final Class318 aClass318_3166 = new Class318();
    private Class318 aClass318_3167;

    final void method1869(int i, Class318 class318) {
        if (class318.aClass318_3976 != null) class318.method2373(false);
        anInt3158++;
        class318.aClass318_3976 = aClass318_3166.aClass318_3976;
        if (i > -81) aClass318_3167 = null;
        class318.aClass318_3970 = aClass318_3166;
        class318.aClass318_3976.aClass318_3970 = class318;
        class318.aClass318_3970.aClass318_3976 = class318;
    }

    final Class318 method1870(int i) {
        if (i > -103) aClass318_3167 = null;
        anInt3162++;
        Class318 class318 = aClass318_3166.aClass318_3976;
        if (aClass318_3166 == class318) {
            aClass318_3167 = null;
            return null;
        }
        aClass318_3167 = class318.aClass318_3976;
        return class318;
    }

    final Class318 method1872(int i) {
        anInt3163++;
        Class318 class318 = aClass318_3166.aClass318_3970;
        if (i != 8) method1878((byte) 126);
        if (class318 == aClass318_3166) {
            aClass318_3167 = null;
            return null;
        }
        aClass318_3167 = class318.aClass318_3970;
        return class318;
    }

    final Class318 method1875(int i) {
        anInt3160++;
        Class318 class318 = aClass318_3166.aClass318_3970;
        if (class318 == aClass318_3166) return null;
        class318.method2373(false);
        if (i != 60) method1878((byte) 16);
        return class318;
    }

    final Class318 method1878(byte i) {
        anInt3159++;
        Class318 class318 = aClass318_3167;
        int i_1_ = -59 % ((67 - i) / 55);
        if (class318 == aClass318_3166) {
            aClass318_3167 = null;
            return null;
        }
        aClass318_3167 = class318.aClass318_3970;
        return class318;
    }

    public Class243() {
        aClass318_3166.aClass318_3976 = aClass318_3166;
        aClass318_3166.aClass318_3970 = aClass318_3166;
    }
}
