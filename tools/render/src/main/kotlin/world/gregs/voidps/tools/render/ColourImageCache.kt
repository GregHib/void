package world.gregs.voidps.tools.render

/** Class322 **/
class ColourImageCache(i: Int, i_9_: Int, i_10_: Int) {
    private val anInt4020: Int
    private var anInt4022: Int
    private val anInt4024: Int
    private var anInt4025 = 0
    private var anIntArrayArrayArray4029: Array<Array<IntArray>>?
    private var aColourImageCacheEntryArray4033: Array<ColourImageCacheEntry?>?
    var aBoolean4035: Boolean
    private var aLinkedList_4021: LinkedList?

    fun method2557(i_6_: Int): Array<IntArray>? {
        if (anInt4020 != anInt4024) {
            if (anInt4020 == 1) {
                this.aBoolean4035 = i_6_ != anInt4022
                anInt4022 = i_6_
                return anIntArrayArrayArray4029!![0]
            }
            var class348_sub24 = aColourImageCacheEntryArray4033!![i_6_]
            if (class348_sub24 == null) {
                this.aBoolean4035 = true
                if (anInt4020 <= anInt4025) {
                    val colourImageCacheEntry_7_ = aLinkedList_4021!!.method1993() as ColourImageCacheEntry?
                    class348_sub24 = ColourImageCacheEntry(i_6_, colourImageCacheEntry_7_!!.anInt6875)
                    aColourImageCacheEntryArray4033!![colourImageCacheEntry_7_.anInt6872] = null
                    colourImageCacheEntry_7_.unlink()
                } else {
                    class348_sub24 = ColourImageCacheEntry(i_6_, anInt4025)
                    anInt4025++
                }
                aColourImageCacheEntryArray4033!![i_6_] = class348_sub24
            } else this.aBoolean4035 = false
            aLinkedList_4021!!.method2001(class348_sub24)
            return (anIntArrayArrayArray4029!![class348_sub24.anInt6875])
        }
        this.aBoolean4035 = aColourImageCacheEntryArray4033!![i_6_] == null
        aColourImageCacheEntryArray4033!![i_6_] = aColourImageCacheEntry_4226
        return anIntArrayArrayArray4029!![i_6_]
    }

    fun method2553(): Array<Array<IntArray>>? {
        if (anInt4024 != anInt4020) throw RuntimeException("Can only retrieve a full image cache")
        for (i_2_ in 0..<anInt4020) aColourImageCacheEntryArray4033!![i_2_] = aColourImageCacheEntry_4226
        return anIntArrayArrayArray4029
    }

    fun method2558() {
        aColourImageCacheEntryArray4033 = null
        anIntArrayArrayArray4029 = null
        aLinkedList_4021!!.method1996()
        aLinkedList_4021 = null
    }

    init {
        anInt4022 = -1
        aLinkedList_4021 = LinkedList()
        this.aBoolean4035 = false
        anInt4020 = i
        anInt4024 = i_9_
        aColourImageCacheEntryArray4033 = arrayOfNulls<ColourImageCacheEntry>(anInt4024)
        anIntArrayArrayArray4029 = Array(anInt4020) { Array(3) { IntArray(i_10_) } }
    }

    companion object {
        var aColourImageCacheEntry_4226: ColourImageCacheEntry? = null
    }
}
