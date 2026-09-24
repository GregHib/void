package world.gregs.voidps.tools.icon

/* Class33 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class33 {
    var anInt459: Int = 0

    fun method340(i: Int, i_5_: Byte): Int {
        var i = i
        anInt459++
        i = --i or (i ushr 1)
        i = i or (i ushr 2)
        i = i or (i ushr 4)
        if (i_5_.toInt() != 108) return 34
        i = i or (i ushr 8)
        i = i or (i ushr 16)
        return 1 + i
    }
}
