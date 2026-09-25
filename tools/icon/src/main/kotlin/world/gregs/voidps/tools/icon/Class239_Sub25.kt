package world.gregs.voidps.tools.icon

import kotlin.math.pow

/* Class239_Sub25 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class239_Sub25 {

    fun method1827() {
        if (ItemSpriteCacheKey.HSV_TO_RGB == null) ItemSpriteCacheKey.HSV_TO_RGB = IntArray(65536)
        else return
        val d = 0.7 + (0.03 * Math.random() - 0.015)
        var i_5_ = 0
        for (i_6_ in 0..511) {
            val f = (((i_6_ shr 3).toFloat() / 64.0f + 0.0078125f) * 360.0f)
            val f_7_ = (i_6_ and 0x7).toFloat() / 8.0f + 0.0625f
            for (i_8_ in 0..127) {
                val f_9_ = i_8_.toFloat() / 128.0f
                var f_10_ = 0.0f
                var f_11_ = 0.0f
                var f_12_ = 0.0f
                val f_13_ = f / 60.0f
                val i_14_ = f_13_.toInt()
                val i_15_ = i_14_ % 6
                val f_16_ = -i_14_.toFloat() + f_13_
                val f_17_ = f_9_ * (-f_7_ + 1.0f)
                val f_18_ = f_9_ * (1.0f - f_16_ * f_7_)
                val f_19_ = (1.0f - (1.0f - f_16_) * f_7_) * f_9_
                if (i_15_ == 0) {
                    f_10_ = f_9_
                    f_11_ = f_19_
                    f_12_ = f_17_
                } else if (i_15_ == 1) {
                    f_11_ = f_9_
                    f_10_ = f_18_
                    f_12_ = f_17_
                } else if (i_15_ == 2) {
                    f_12_ = f_19_
                    f_10_ = f_17_
                    f_11_ = f_9_
                } else if (i_15_ == 3) {
                    f_12_ = f_9_
                    f_11_ = f_18_
                    f_10_ = f_17_
                } else if (i_15_ == 4) {
                    f_12_ = f_9_
                    f_11_ = f_17_
                    f_10_ = f_19_
                } else if (i_15_ == 5) {
                    f_10_ = f_9_
                    f_11_ = f_17_
                    f_12_ = f_18_
                }
                f_10_ = f_10_.toDouble().pow(d).toFloat()
                f_11_ = f_11_.toDouble().pow(d).toFloat()
                f_12_ = f_12_.toDouble().pow(d).toFloat()
                val i_20_ = (f_10_ * 256.0f).toInt()
                val i_21_ = (256.0f * f_11_).toInt()
                val i_22_ = (256.0f * f_12_).toInt()
                val i_23_ = ((i_21_ shl 8) + ((i_20_ shl 16) + (-16777216 + i_22_)))
                ItemSpriteCacheKey.HSV_TO_RGB!![i_5_++] = i_23_
            }
        }
    }
}
