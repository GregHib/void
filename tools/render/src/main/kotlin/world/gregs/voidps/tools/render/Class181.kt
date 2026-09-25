package world.gregs.voidps.tools.render

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/* Class181 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class181 {
    fun sphereMap(i: Int, i_0_: Int, f: Float, i_1_: Int, fs: FloatArray?, i_2_: Int, i_3_: Int, i_4_: Int, i_5_: Int, fs_7_: FloatArray?) {
        var i_1_ = i_1_
        var i_2_ = i_2_
        var i_4_ = i_4_
        i_2_ -= i
        i_1_ -= i_3_
        i_4_ -= i_5_
        val f_8_ = i_2_.toFloat() * fs_7_!![2] + (fs_7_[1] * i_4_.toFloat() + fs_7_[0] * i_1_.toFloat())
        val f_9_ = i_2_.toFloat() * fs_7_[5] + (i_1_.toFloat() * fs_7_[3] + i_4_.toFloat() * fs_7_[4])
        val f_10_ = (fs_7_[6] * i_1_.toFloat() + i_4_.toFloat() * fs_7_[7] + i_2_.toFloat() * fs_7_[8])
        val f_11_ = sqrt((f_8_ * f_8_ + f_9_ * f_9_ + f_10_ * f_10_).toDouble()).toFloat()
        var f_12_ = 0.5f + (atan2(f_8_.toDouble(), f_10_.toDouble()).toFloat() / 6.2831855f)
        var f_13_ = f + (0.5f + (asin((f_9_ / f_11_).toDouble()).toFloat() / 3.1415927f))
        if (i_0_ == 1) {
            val f_15_ = f_12_
            f_12_ = -f_13_
            f_13_ = f_15_
        } else if (i_0_ == 2) {
            f_13_ = -f_13_
            f_12_ = -f_12_
        } else if (i_0_ == 3) {
            val f_14_ = f_12_
            f_12_ = f_13_
            f_13_ = -f_14_
        }
        fs!![0] = f_12_
        fs[1] = f_13_
    }
}
