package world.gregs.voidps.tools.icon

/* Class299 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class299 {
    var anInt3814: Int = 0

    fun method2253(i: Int, i_0_: Int): Int {
        var i = i
        anInt3814++
        val i_1_ = -124 % ((i_0_ - 55) / 63)
        var i_2_ = 0
        if (i < 0 || i >= 65536) {
            i_2_ += 16
            i = i ushr 16
        }
        if (i >= 256) {
            i = i ushr 8
            i_2_ += 8
        }
        if (i >= 16) {
            i_2_ += 4
            i = i ushr 4
        }
        if (i >= 4) {
            i_2_ += 2
            i = i ushr 2
        }
        if (i >= 1) {
            i = i ushr 1
            i_2_++
        }
        return i_2_ - -i
    }
}
