package world.gregs.voidps.tools.icon

/* Class318_Sub9_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class318_Sub9_Sub1 {
    var anInt8788: Int = 0
    var aFloat8784: Float = 0f

    fun method2516(i: Int, i_7_: Byte, i_8_: Int): Class348_Sub42_Sub15 {
        anInt8788++
        var class348_sub42_sub15 = (Class100.aIterableHashTable_1585.method3480((i_8_.toLong() shl 32 or i.toLong()), i_7_.toInt() xor 0x171e.inv()) as Class348_Sub42_Sub15?)
        if (i_7_.toInt() != 105) aFloat8784 = 0.99212307f
        if (class348_sub42_sub15 == null) {
            class348_sub42_sub15 = Class348_Sub42_Sub15(i_8_, i)
            Class100.aIterableHashTable_1585.put(91.toByte(), (class348_sub42_sub15.aLong4291), class348_sub42_sub15)
        }
        return class348_sub42_sub15
    }
}
