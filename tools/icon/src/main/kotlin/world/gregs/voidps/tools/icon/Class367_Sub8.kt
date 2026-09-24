package world.gregs.voidps.tools.icon

/* Class367_Sub8 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class367_Sub8 {
    var anInt7349: Int = 0

    fun method3546(`is`: ByteArray, i: Int, i_0_: Int, i_1_: Int): String {
        anInt7349++
        val cs = CharArray(i_0_)
        var i_2_ = 0
        for (i_3_ in i..<i_0_) {
            var i_4_ = 0xff and `is`[i_3_ + i_1_].toInt()
            if (i_4_ != 0) {
                if (i_4_ >= 128 && i_4_ < 160) {
                    var i_5_ = Class44.aCharArray625[i_4_ - 128].code
                    if (i_5_ == 0) i_5_ = 63
                    i_4_ = i_5_
                }
                cs[i_2_++] = i_4_.toChar()
            }
        }
        return String(cs, 0, i_2_)
    }
}
