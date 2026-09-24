package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub20 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub20 : Class348_Sub40(0, true) {
    private fun method3103(i: Int, i_0_: Int, i_1_: Int): Int {
        anInt9262++
        if (i_1_ < 14) aClass348_Sub4_9264 = null
        var i_2_ = i_0_ - -(57 * i)
        i_2_ = i_2_ xor (i_2_ shl 1)
        return (-(((789221 + 15731 * (i_2_ * i_2_)) * i_2_ + 1376312589 and 0x7fffffff) / 262144) + 4096)
    }

    override fun method3042(i: Int, i_3_: Int): IntArray {
        anInt9261++
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_4_ = Class79.anIntArray6035!![i]
            var i_5_ = 0
            while ((i_5_ < Class348_Sub40_Sub6.Companion.anInt9139)) {
                `is`!![i_5_] = method3103(i_4_, Class348_Sub40_Sub8.anIntArray6432!![i_5_], 22) % 4096
                i_5_++
            }
        }
        if (i_3_ != 255) method3042(38, -42)
        return `is`!!
    }

    companion object {
        var anInt9261: Int = 0
        var anInt9262: Int = 0
        var aClass348_Sub4_9264: Class348_Sub4? = null
    }
}
