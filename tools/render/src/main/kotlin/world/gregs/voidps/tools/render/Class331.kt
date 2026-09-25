package world.gregs.voidps.tools.render

/* Class331 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class331 {
    fun method2635(f: Float, f_0_: Float, f_1_: Float): Int {
        val f_2_ = if (!(f_1_ < 0.0f)) f_1_ else -f_1_
        val f_3_ = if (f < 0.0f) -f else f
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
}
