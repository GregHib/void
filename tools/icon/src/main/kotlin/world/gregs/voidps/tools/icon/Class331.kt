package world.gregs.voidps.tools.icon

/* Class331 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class331 {
    var anIntArray4128: IntArray? = IntArray(5)
    var aStringArray4129: Array<String?>? = arrayOf<String?>("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    var aClass46_4130: Class46?
    var anInt4131: Int = 0

    fun method2635(f: Float, bool: Boolean, f_0_: Float, f_1_: Float): Int {
        anInt4131++
        val f_2_ = if (!(f_1_ < 0.0f)) f_1_ else -f_1_
        val f_3_ = if (f < 0.0f) -f else f
        if (bool != false) method2637(-85)
        val f_4_ = if (!(f_0_ < 0.0f)) f_0_ else -f_0_
        if (!(f_2_ < f_3_) || !(f_3_ > f_4_)) {
            if (!(f_4_ > f_2_) || !(f_3_ < f_4_)) {
                if (!(f_1_ > 0.0f)) return 5
                return 4
            }
            if (f_0_ > 0.0f) return 2
            return 3
        }
        if (f > 0.0f) return 0
        return 1
    }

    fun method2637(i: Int) {
        aStringArray4129 = null
        aClass46_4130 = null
        if (i != 0) aClass46_4130 = null
        anIntArray4128 = null
    }

    init {
        aClass46_4130 = null
    }
}
