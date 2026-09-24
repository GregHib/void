package world.gregs.voidps.tools.icon

/* Class89 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class89 {
    var anIntArray1508: IntArray = IntArray(256)

    init {
        for (i in 0..255) {
            var i_23_ = i
            for (i_24_ in 0..7) {
                if ((i_23_ and 0x1) != 1) i_23_ = i_23_ ushr 1
                else i_23_ = i_23_ ushr 1 xor 0x12477cdf.inv()
            }
            anIntArray1508[i] = i_23_
        }
    }
}
