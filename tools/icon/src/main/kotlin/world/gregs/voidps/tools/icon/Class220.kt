package world.gregs.voidps.tools.icon

import kotlin.math.cos
import kotlin.math.sin

/* Class220 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class220 {
    var anInt2876: Int = 0
    var anInt2878: Int = 0

    fun method1605(i: Int) {
        if (Class235.anIntArray3068 == null || Class127.anIntArray4654 == null) {
            Class127.anIntArray4654 = IntArray(256)
            Class235.anIntArray3068 = IntArray(256)
            for (i_0_ in 0..255) {
                val d = 6.283185307179586 * (i_0_.toDouble() / 255.0)
                Class235.anIntArray3068[i_0_] = (4096.0 * sin(d)).toInt()
                Class127.anIntArray4654[i_0_] = (4096.0 * cos(d)).toInt()
            }
        }
        anInt2878++
        if (i != 26188) method1606(-76, 98, 86)
    }

    fun method1606(i: Int, i_1_: Int, i_2_: Int): Byte {
        anInt2876++
        if (i_2_ != 9) return 0.toByte()
        if (i_1_ != -27939) return -50.toByte()
        if ((i and 0x1) == 0) return 1.toByte()
        return 2.toByte()
    }
}
