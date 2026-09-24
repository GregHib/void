package world.gregs.voidps.tools.icon

import kotlin.math.min
import kotlin.math.pow

/* Class348_Sub42_Sub13 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class348_Sub42_Sub13 : Class348_Sub42() {
    var anInt9618: Int = 0

    fun method3232(d: Double, i: Byte) {
        if (i <= -54) {
            if (d != Class299_Sub2_Sub1.aDouble8713) {
                for (i_0_ in 0..255) {
                    val i_1_ = (255.0 * (i_0_.toDouble() / 255.0).pow(d)).toInt()
                    Class318_Sub1_Sub3_Sub3.anIntArray10266[i_0_] = min(i_1_, 255)
                }
                Class299_Sub2_Sub1.aDouble8713 = d
            }
            anInt9618++
        }
    }
}
