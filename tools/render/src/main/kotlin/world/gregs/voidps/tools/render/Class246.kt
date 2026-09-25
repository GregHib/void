package world.gregs.voidps.tools.render

import kotlin.math.atan2

/* Class246 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class246 {
    fun cylinderMap(i: Int, i_0_: Int, i_1_: Int, i_3_: Int, fs: FloatArray?, i_4_: Int, f: Float, i_5_: Int, i_6_: Int, f_7_: Float, fs_8_: FloatArray?) {
        var i_0_ = i_0_
        var i_3_ = i_3_
        var i_4_ = i_4_
        i_4_ -= i_5_
        i_0_ -= i
        i_3_ -= i_6_
        val f_9_ = fs_8_!![2] * i_0_.toFloat() + (fs_8_[1] * i_4_.toFloat() + i_3_.toFloat() * fs_8_[0])
        val f_10_ = (fs_8_[5] * i_0_.toFloat() + (fs_8_[3] * i_3_.toFloat() + i_4_.toFloat() * fs_8_[4]))
        val f_11_ = (i_3_.toFloat() * fs_8_[6] + fs_8_[7] * i_4_.toFloat() + i_0_.toFloat() * fs_8_[8])
        var f_12_ = 0.5f + (atan2(f_9_.toDouble(), f_11_.toDouble()).toFloat() / 6.2831855f)
        if (f_7_ != 1.0f) f_12_ *= f_7_
        var f_13_ = 0.5f + f_10_ + f
        if (i_1_ == 1) {
            val f_14_ = f_12_
            f_12_ = -f_13_
            f_13_ = f_14_
        } else if (i_1_ == 2) {
            f_12_ = -f_12_
            f_13_ = -f_13_
        } else if (i_1_ == 3) {
            val f_15_ = f_12_
            f_12_ = f_13_
            f_13_ = -f_15_
        }
        fs!![1] = f_13_
        fs[0] = f_12_
    }
}
