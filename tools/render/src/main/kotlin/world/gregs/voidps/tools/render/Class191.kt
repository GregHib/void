package world.gregs.voidps.tools.render

class Class191(i: Int, i_16_: Int, i_17_: Int) {
    private var anInt2557 = 0
    private val anInt2559: Int
    private var aClass262_2561: Class262?
    private var anInt2562 = -1
    private var aClass348_Sub6Array2563: Array<Class348_Sub6?>?
    private var anIntArrayArray2564: Array<IntArray?>?
    private var anInt2565: Int
    var aBoolean2570: Boolean

    fun method1427(): Array<IntArray?>? {
        if (anInt2559 != anInt2565) throw RuntimeException("Can only retrieve a full image cache")
        var i_0_ = 0
        while (anInt2559 > i_0_) {
            aClass348_Sub6Array2563!![i_0_] = aClass348_Sub6_5206
            i_0_++
        }
        return anIntArrayArray2564
    }

    fun method1432() {
        for (i_13_ in 0..<anInt2559) anIntArrayArray2564!![i_13_] = null
        aClass348_Sub6Array2563 = null
        anIntArrayArray2564 = null
        aClass262_2561!!.method1996()
        aClass262_2561 = null
    }

    fun method1433(i_14_: Int): IntArray? {
        if (anInt2559 == anInt2565) {
            this.aBoolean2570 = aClass348_Sub6Array2563!![i_14_] == null
            aClass348_Sub6Array2563!![i_14_] = aClass348_Sub6_5206
            return anIntArrayArray2564!![i_14_]
        }
        if (anInt2559 != 1) {
            var class348_sub6 = aClass348_Sub6Array2563!![i_14_]
            if (class348_sub6 == null) {
                this.aBoolean2570 = true
                if (anInt2557 < anInt2559) {
                    class348_sub6 = Class348_Sub6(i_14_, anInt2557)
                    anInt2557++
                } else {
                    val class348_sub6_15_ = aClass262_2561!!.method1993() as Class348_Sub6?
                    class348_sub6 = Class348_Sub6(i_14_, class348_sub6_15_!!.anInt6636)
                    aClass348_Sub6Array2563!![class348_sub6_15_.anInt6630] = null
                    class348_sub6_15_.unlink()
                }
                aClass348_Sub6Array2563!![i_14_] = class348_sub6
            } else this.aBoolean2570 = false
            aClass262_2561!!.method2001(class348_sub6)
            return (anIntArrayArray2564!![class348_sub6.anInt6636])
        }
        this.aBoolean2570 = i_14_ != anInt2562
        anInt2562 = i_14_
        return anIntArrayArray2564!![0]
    }

    init {
        aClass262_2561 = Class262()
        this.aBoolean2570 = false
        anInt2559 = i
        anInt2565 = i_16_
        aClass348_Sub6Array2563 = arrayOfNulls<Class348_Sub6>(anInt2565)
        anIntArrayArray2564 = Array<IntArray?>(anInt2559) { IntArray(i_17_) }
    }

    companion object {
        /** aa_Sub3.aClass348_Sub6_5206 */
        internal var aClass348_Sub6_5206: Class348_Sub6 = Class348_Sub6(0, 0)
    }
}
