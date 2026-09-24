package world.gregs.voidps.tools.icon

/* Class252 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class252 {
    var anInt3243: Int = 0

    fun method1918(i: Int, i_0_: Int): Int {
        if (i != -3358) return 126
        anInt3243++
        val i_1_ = i_0_ * (i_0_ * i_0_ shr 12) shr 12
        val i_2_ = i_0_ * 6 + -61440
        val i_3_ = 40960 - -(i_2_ * i_0_ shr 12)
        return i_3_ * i_1_ shr 12
    }
}
