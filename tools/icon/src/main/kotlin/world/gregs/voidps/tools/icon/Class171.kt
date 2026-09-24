package world.gregs.voidps.tools.icon

/* Class171 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class171 {
    var anInt2267: Int = 0

    fun method1319(i: Int, bool: Boolean, `is`: ByteArray, i_0_: Int): Int {
        anInt2267++
        if (bool != true) return 63
        var i_1_ = -1
        for (i_2_ in i_0_..<i) i_1_ = (i_1_ ushr 8 xor Class89.anIntArray1508[(i_1_ xor `is`[i_2_].toInt()) and 0xff])
        i_1_ = i_1_ xor -0x1
        return i_1_
    }
}
