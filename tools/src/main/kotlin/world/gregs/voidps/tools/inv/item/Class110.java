package world.gregs.voidps.tools.inv.item;/* Class110 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;

abstract class Class110 {
    static int anInt1705;

    static final Class348_Sub31 method1035(int i, int i_16_, Canvas canvas, int i_17_) {
        anInt1705++;
        if (i != 9029) return null;
        try {
            Class348_Sub31 class348_sub31 = new Class348_Sub31_Sub1();
            class348_sub31.method3008(canvas, i_17_, -90, i_16_);
            return class348_sub31;
        } catch (Throwable throwable) {
            Class348_Sub31_Sub2 class348_sub31_sub2 = new Class348_Sub31_Sub2();
            class348_sub31_sub2.method3008(canvas, i_17_, -128, i_16_);
            return class348_sub31_sub2;
        }
    }
}
