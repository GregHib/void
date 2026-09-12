package world.gregs.voidps.tools.inv.item;/* Class348_Sub5 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;

abstract class Class348_Sub5 extends Class348 {
    static int anInt6628;
    static byte[] aByteArray6624 = new byte[2048];

    static final ha method2753(boolean bool, int i, int i_4_, Canvas canvas, d var_d) {
        try {
            anInt6628++;
            if (bool != true) aByteArray6624 = null;
            return new ha_Sub1(canvas, var_d, i_4_, i);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("fba.M(" + bool + ',' + i + ',' + i_4_ + ',' + (canvas != null ? "{...}" : "null") + ',' + (var_d != null ? "{...}" : "null") + ')'));
        }
    }
}
