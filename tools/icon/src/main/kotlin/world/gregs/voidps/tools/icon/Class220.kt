package world.gregs.voidps.tools.icon

import kotlin.math.cos
import kotlin.math.sin

/* Class220 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class220 {
    var anIntArray3068: IntArray? = null

    var anIntArray4654: IntArray? = null


    fun method1605() {
        if (anIntArray3068 == null || anIntArray4654 == null) {
            anIntArray4654 = IntArray(256)
            anIntArray3068 = IntArray(256)
            for (i_0_ in 0..255) {
                val d = 6.283185307179586 * (i_0_.toDouble() / 255.0)
                anIntArray3068!![i_0_] = (4096.0 * sin(d)).toInt()
                anIntArray4654!![i_0_] = (4096.0 * cos(d)).toInt()
            }
        }
    }
}
