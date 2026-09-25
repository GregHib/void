package world.gregs.voidps.tools.render

/* Class79 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class79 {
    var anInt6212: Int = 0

    var anInt3201: Int = 0

    var anIntArray6035: IntArray? = null


    fun method797(i: Int, i_29_: Int) {
        if (Class348_Sub40_Sub6.anInt9139 != i_29_) {
            Class348_Sub40_Sub8.anIntArray6432 = IntArray(i_29_)
            for (i_31_ in 0..<i_29_) Class348_Sub40_Sub8.anIntArray6432!![i_31_] = (i_31_ shl 12) / i_29_
            Class348_Sub40_Sub37.anInt6076 = i_29_ + -1
            Class348_Sub40_Sub6.anInt9139 = i_29_
            anInt3201 = 32 * i_29_
        }
        if (anInt6212 != i) {
            if (Class348_Sub40_Sub6.anInt9139 != i) {
                anIntArray6035 = IntArray(i)
                for (i_32_ in 0..<i) anIntArray6035!![i_32_] = (i_32_ shl 12) / i
            } else anIntArray6035 = Class348_Sub40_Sub8.anIntArray6432
            anInt6212 = i
            Class348_Sub40_Sub6.anInt6325 = -1 + i
        }
    }
}
