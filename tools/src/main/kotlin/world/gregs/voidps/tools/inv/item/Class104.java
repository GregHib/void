package world.gregs.voidps.tools.inv.item;/* Class104 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;

final class Class104 {
    static int anInt1610;
    static Class221 aClass221_1620 = new Class221();

    static final ha method958(boolean bool, int i, d var_d, int i_61_, Canvas canvas, Class45 class45) {
        try {
            if (bool != true) aClass221_1620 = null;
            anInt1610++;
            int i_62_ = 0;
            int i_63_ = 0;
            if (canvas != null) {
                Dimension dimension = canvas.getSize();
                i_63_ = dimension.height;
                i_62_ = dimension.width;
            }
            return ha.method3692(i_61_, i_63_, i_62_, class45, 0, var_d, canvas, i);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("mha.E(" + bool + ',' + i + ',' + (var_d != null ? "{...}" : "null") + ',' + i_61_ + ',' + (canvas != null ? "{...}" : "null") + ',' + (class45 != null ? "{...}" : "null") + ')'));
        }
    }
}
