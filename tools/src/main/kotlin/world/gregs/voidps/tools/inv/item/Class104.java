package world.gregs.voidps.tools.inv.item;/* Class104 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;

final class Class104 {
    static int anInt1610;
    static Class221 aClass221_1620 = new Class221();

    static final Toolkit method958(boolean bool, int i, TextureSource var_textureSource, int i_61_, Canvas canvas, Js5 js5) {
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
            return Toolkit.method3692(i_61_, i_63_, i_62_, js5, 0, var_textureSource, canvas, i);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("mha.E(" + bool + ',' + i + ',' + (var_textureSource != null ? "{...}" : "null") + ',' + i_61_ + ',' + (canvas != null ? "{...}" : "null") + ',' + (js5 != null ? "{...}" : "null") + ')'));
        }
    }
}
