package world.gregs.voidps.tools.icon

/* Class25 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class25 {
    var anInt362: Int = 0

    fun method303(i: Int, i_3_: Int): Short {
        anInt362++
        val i_4_ = (i and 0xfe66) shr 10
        var i_5_ = i shr 3 and 0x70
        val i_6_ = i and 0x7f
        i_5_ = (if (i_6_ <= 64) i_6_ * i_5_ shr 7 else i_5_ * (127 + -i_6_) shr 7)
        val i_7_ = i_5_ + i_6_
        val i_8_: Int
        if (i_7_ != 0) i_8_ = (i_5_ shl 8) / i_7_
        else i_8_ = i_5_ shl 1
        val i_9_ = i_7_
        if (i_3_ != 30) return 79.toShort()
        return (i_9_ or (i_8_ shr 4 shl 7 or (i_4_ shl 10))).toShort()
    }
}
