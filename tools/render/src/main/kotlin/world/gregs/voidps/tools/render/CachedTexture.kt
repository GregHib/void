package world.gregs.voidps.tools.render

/** Class348_Sub25 **/
internal class CachedTexture(var anInt6883: Int, private val anInt6880: Int, `is`: IntArray, bool: Boolean) {
    private var anIntArray6884: IntArray?

    fun method2997(): IntArray? {
        return anIntArray6884
    }

    init {
        anIntArray6884 = `is`
        if (bool) {
            val is_12_ = IntArray(anInt6880)
            val is_13_ = IntArray(anInt6880)
            val is_14_ = IntArray(anInt6880)
            val is_15_ = IntArray(anInt6880)
            if (anIntArray6881 == null || anIntArray6881!!.size != anIntArray6884!!.size) anIntArray6881 = IntArray(anIntArray6884!!.size)
            val i_16_ = anInt6880
            val i_17_ = anInt6880
            val i_18_ = i_16_ - 1
            val i_19_ = i_17_ - 1
            val i_20_ = i_16_ * i_17_
            var i_22_: Int
            i_22_ = i_16_
            var i_21_ = i_22_
            for (i_23_ in 2 downTo 0) {
                for (i_24_ in i_18_ downTo 0) {
                    val i_25_ = anIntArray6884!![--i_22_]
                    is_12_[i_24_] += i_25_ shr 24 and 0xff
                    is_13_[i_24_] += i_25_ shr 16 and 0xff
                    is_14_[i_24_] += i_25_ shr 8 and 0xff
                    is_15_[i_24_] += i_25_ and 0xff
                }
                if (i_22_ == 0) i_22_ = i_20_
            }
            var i_26_ = i_20_
            for (i_27_ in i_19_ downTo 0) {
                var i_28_ = 1
                var i_29_ = 1
                var i_31_: Int
                var i_32_: Int
                var i_33_: Int
                i_33_ = 0
                i_32_ = i_33_
                i_31_ = i_32_
                var i_30_ = i_31_
                for (i_34_ in 2 downTo 0) {
                    i_29_--
                    i_30_ += is_12_[i_29_]
                    i_31_ += is_13_[i_29_]
                    i_33_ += is_14_[i_29_]
                    i_32_ += is_15_[i_29_]
                    if (i_29_ == 0) i_29_ = i_16_
                }
                for (i_35_ in i_18_ downTo 0) {
                    i_29_--
                    i_28_--
                    val i_36_ = i_30_ / 9
                    val i_37_ = i_31_ / 9
                    val i_38_ = i_33_ / 9
                    val i_39_ = i_32_ / 9
                    anIntArray6881!![--i_26_] = i_36_ shl 24 or (i_37_ shl 16) or (i_38_ shl 8) or i_39_
                    i_30_ += is_12_[i_29_] - is_12_[i_28_]
                    i_31_ += is_13_[i_29_] - is_13_[i_28_]
                    i_32_ += is_15_[i_29_] - is_15_[i_28_]
                    i_33_ += is_14_[i_29_] - is_14_[i_28_]
                    if (i_29_ == 0) i_29_ = i_16_
                    if (i_28_ == 0) i_28_ = i_16_
                }
                for (i_40_ in i_18_ downTo 0) {
                    val i_41_ = anIntArray6884!![--i_22_]
                    val i_42_ = anIntArray6884!![--i_21_]
                    is_12_[i_40_] += (i_41_ shr 24 and 0xff) - (i_42_ shr 24 and 0xff)
                    is_13_[i_40_] += (i_41_ shr 16 and 0xff) - (i_42_ shr 16 and 0xff)
                    is_14_[i_40_] += (i_41_ shr 8 and 0xff) - (i_42_ shr 8 and 0xff)
                    is_15_[i_40_] += (i_41_ and 0xff) - (i_42_ and 0xff)
                }
                if (i_22_ == 0) i_22_ = i_20_
                if (i_21_ == 0) i_21_ = i_20_
            }
            val is_43_ = anIntArray6884
            anIntArray6884 = anIntArray6881
            anIntArray6881 = is_43_
        }
    }

    companion object {
        private var anIntArray6881: IntArray? = null
    }
}
