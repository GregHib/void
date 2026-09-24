package world.gregs.voidps.tools.icon

import java.awt.Canvas

/* Class348_Sub5 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class348_Sub5 : Class348() {
    var anInt6628: Int = 0
    var aByteArray6624: ByteArray? = ByteArray(2048)

    fun method2753(bool: Boolean, i: Int, i_4_: Int, canvas: Canvas?, var_textureSource: TextureSource?): Toolkit {
        try {
            anInt6628++
            if (bool != true) aByteArray6624 = null
            return JavaToolkit(canvas, var_textureSource, i_4_, i)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("fba.M(" + bool + ',' + i + ',' + i_4_ + ',' + (if (canvas != null) "{...}" else "null") + ',' + (if (var_textureSource != null) "{...}" else "null") + ')'))
        }
    }
}
