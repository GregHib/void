package world.gregs.voidps.tools.icon

/* Class79 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class79 {
    var anInt1379: Int = 0
    var anInt1387: Int = 0
    var aSprite_1365: Sprite? = null
    var aClient1367: client? = null

    fun method797(i: Int, i_29_: Int, i_30_: Byte) {
        anInt1379++
        if (Class348_Sub40_Sub6.Companion.anInt9139 != i_29_) {
            Class318_Sub6.anIntArray6432 = IntArray(i_29_)
            for (i_31_ in 0..<i_29_) Class318_Sub6.anIntArray6432!![i_31_] = (i_31_ shl 12) / i_29_
            Class239_Sub22.anInt6076 = i_29_ + -1
            Class348_Sub40_Sub6.Companion.anInt9139 = i_29_
            Class248.anInt3201 = 32 * i_29_
        }
        if (i_30_ <= 108) aClient1367 = null
        if (Class286_Sub2.anInt6212 != i) {
            if (Class348_Sub40_Sub6.Companion.anInt9139 != i) {
                Class239_Sub18.anIntArray6035 = IntArray(i)
                for (i_32_ in 0..<i) Class239_Sub18.anIntArray6035!![i_32_] = (i_32_ shl 12) / i
            } else Class239_Sub18.anIntArray6035 = Class318_Sub6.anIntArray6432
            Class286_Sub2.anInt6212 = i
            Class299_Sub2.anInt6325 = -1 + i
        }
    }

    fun method804(i: Int) {
        aSprite_1365 = null
        aClient1367 = null
        if (i != -3752) anInt1387 = 14
    }
}
