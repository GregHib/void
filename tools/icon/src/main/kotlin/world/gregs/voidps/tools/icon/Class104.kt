package world.gregs.voidps.tools.icon

import java.awt.Canvas

/* Class104 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class104 {
    var anInt1610: Int = 0
    var aClass221_1620: Class221? = Class221()

    fun method958(bool: Boolean, i: Int, var_textureSource: TextureSource?, i_61_: Int, canvas: Canvas?): Toolkit {
        try {
            if (bool != true) aClass221_1620 = null
            anInt1610++
            var i_62_ = 0
            var i_63_ = 0
            if (canvas != null) {
                val dimension = canvas.getSize()
                i_63_ = dimension.height
                i_62_ = dimension.width
            }
            return Toolkit.Companion.method3692(i_61_, i_63_, i_62_, 0, var_textureSource, canvas, i)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("mha.E(" + bool + ',' + i + ',' + (if (var_textureSource != null) "{...}" else "null") + ',' + i_61_ + ',' + (if (canvas != null) "{...}" else "null") + ')'))
        }
    }
}
