package world.gregs.voidps.tools.icon

import kotlin.math.sqrt

/* Class46 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class46 {
    var anInt698: Int = 0
    var anInt709: Int = 0
    var anInt789: Int = 0
    var aClass46Array798: Array<Class46?>? = null
    var anInt791: Int = 0
    var anInt830: Int = 0

    companion object {
        var aByteArray821: ByteArray? = ByteArray(32896)
        var aClass196_838: Class196?

        fun method442(i: Byte) {
            if (i <= -8) {
                aClass196_838 = null
                aByteArray821 = null
            }
        }

        init {
            var i = 0
            for (i_69_ in 0..255) {
                var i_70_ = 0
                while (i_69_ >= i_70_) {
                    aByteArray821!![i++] = (255.0 / sqrt((((i_69_ * i_69_) + (i_70_ * i_70_) - -65535).toFloat() / 65535.0f).toDouble())).toInt().toByte()
                    i_70_++
                }
            }
            aClass196_838 = Class196()
        }
    }
}
