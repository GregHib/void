package world.gregs.voidps.tools.render

/** Class191 **/
class MonochromeImageCache(i: Int, i_16_: Int, i_17_: Int) {
    private var anInt2557 = 0
    private val anInt2559: Int
    private var aLinkedList_2561: LinkedList?
    private var anInt2562 = -1
    private var aMonochromeImageCacheEntryArray2563: Array<MonochromeImageCacheEntry?>?
    private var anIntArrayArray2564: Array<IntArray?>?
    private var anInt2565: Int
    var aBoolean2570: Boolean

    fun method1427(): Array<IntArray?>? {
        if (anInt2559 != anInt2565) throw RuntimeException("Can only retrieve a full image cache")
        var i_0_ = 0
        while (anInt2559 > i_0_) {
            aMonochromeImageCacheEntryArray2563!![i_0_] = aMonochromeImageCacheEntry_5206
            i_0_++
        }
        return anIntArrayArray2564
    }

    fun method1432() {
        for (i_13_ in 0..<anInt2559) anIntArrayArray2564!![i_13_] = null
        aMonochromeImageCacheEntryArray2563 = null
        anIntArrayArray2564 = null
        aLinkedList_2561!!.method1996()
        aLinkedList_2561 = null
    }

    fun method1433(i_14_: Int): IntArray? {
        if (anInt2559 == anInt2565) {
            this.aBoolean2570 = aMonochromeImageCacheEntryArray2563!![i_14_] == null
            aMonochromeImageCacheEntryArray2563!![i_14_] = aMonochromeImageCacheEntry_5206
            return anIntArrayArray2564!![i_14_]
        }
        if (anInt2559 != 1) {
            var class348_sub6 = aMonochromeImageCacheEntryArray2563!![i_14_]
            if (class348_sub6 == null) {
                this.aBoolean2570 = true
                if (anInt2557 < anInt2559) {
                    class348_sub6 = MonochromeImageCacheEntry(i_14_, anInt2557)
                    anInt2557++
                } else {
                    val monochromeImageCacheEntry_15_ = aLinkedList_2561!!.method1993() as MonochromeImageCacheEntry?
                    class348_sub6 = MonochromeImageCacheEntry(i_14_, monochromeImageCacheEntry_15_!!.anInt6636)
                    aMonochromeImageCacheEntryArray2563!![monochromeImageCacheEntry_15_.anInt6630] = null
                    monochromeImageCacheEntry_15_.unlink()
                }
                aMonochromeImageCacheEntryArray2563!![i_14_] = class348_sub6
            } else this.aBoolean2570 = false
            aLinkedList_2561!!.method2001(class348_sub6)
            return (anIntArrayArray2564!![class348_sub6.anInt6636])
        }
        this.aBoolean2570 = i_14_ != anInt2562
        anInt2562 = i_14_
        return anIntArrayArray2564!![0]
    }

    init {
        aLinkedList_2561 = LinkedList()
        this.aBoolean2570 = false
        anInt2559 = i
        anInt2565 = i_16_
        aMonochromeImageCacheEntryArray2563 = arrayOfNulls<MonochromeImageCacheEntry>(anInt2565)
        anIntArrayArray2564 = Array<IntArray?>(anInt2559) { IntArray(i_17_) }
    }

    companion object {
        /** aa_Sub3.aClass348_Sub6_5206 */
        internal var aMonochromeImageCacheEntry_5206: MonochromeImageCacheEntry = MonochromeImageCacheEntry(0, 0)
    }
}
