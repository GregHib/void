package world.gregs.voidps.tools.icon

import kotlin.math.sqrt

/* Class46 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class46 {
    var aByteArray821: ByteArray? = ByteArray(32896)

    init {
        var i = 0
        for (i_69_ in 0..255) {
            var i_70_ = 0
            while (i_69_ >= i_70_) {
                aByteArray821!![i++] = (255.0 / sqrt((((i_69_ * i_69_) + (i_70_ * i_70_) - -65535).toFloat() / 65535.0f).toDouble())).toInt().toByte()
                i_70_++
            }
        }
    }
}
