package world.gregs.voidps.tools.icon

/* Class214 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class214 {
    fun method1579(`is`: IntArray, i: Int, i_16_: Int, i_17_: Int) {
        var i = i
        var i_16_ = i_16_
        i_16_ = i + i_16_ - 7
        while (i < i_16_) {
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
        }
        i_16_ += 7
        while (i < i_16_) `is`[i++] = i_17_
    }
}
