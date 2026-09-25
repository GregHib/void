package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub13 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub13 : Class348_Sub40(1, true) {
    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_1_ = this.method3039(i, 0)!!
            val is_2_ = is_1_[0]
            val is_3_ = is_1_[1]
            val is_4_ = is_1_[2]
            for (i_5_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                `is`!![i_5_] = (is_2_[i_5_] - (-is_3_[i_5_] - is_4_[i_5_])) / 3
            }
        }
        return `is`
    }
}
