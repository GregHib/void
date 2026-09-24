package world.gregs.voidps.tools.icon;/* Class348_Sub5 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;

abstract class Class348_Sub5 extends Class348 {
    static int anInt6628;
    static byte[] aByteArray6624 = new byte[2048];

    static final Toolkit method2753(boolean bool, int i, int i_4_, Canvas canvas, TextureSource var_textureSource) {
        try {
            anInt6628++;
            if (bool != true) aByteArray6624 = null;
            return new JavaToolkit(canvas, var_textureSource, i_4_, i);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("fba.M(" + bool + ',' + i + ',' + i_4_ + ',' + (canvas != null ? "{...}" : "null") + ',' + (var_textureSource != null ? "{...}" : "null") + ')'));
        }
    }
}
