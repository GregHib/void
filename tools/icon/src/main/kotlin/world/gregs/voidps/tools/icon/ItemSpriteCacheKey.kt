package world.gregs.voidps.tools.icon

/* Class126 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class ItemSpriteCacheKey : CacheKey {
    var itemNumMode: Int = 0
    var graphicShadow: Int = 0
    var invCount: Int = 0
    var itemWearCol: Boolean = false
    var toolkitIndex: Int = 0
    var itemId: Int = 0
    var outline: Int = 0
    override fun toLong(i: Byte): Long {
        anInt4988++
        val ls = Class348_Sub40_Sub21.crc64table
        var l = -1L
        l = (ls[((l xor this.toolkitIndex.toLong()) and 0xffL).toInt()] xor (l ushr 8))
        l = (l ushr 8 xor ls[(0xffL and ((this.itemId shr 8).toLong() xor l)).toInt()])
        l = (l ushr 8 xor ls[((l xor this.itemId.toLong()) and 0xffL).toInt()])
        l = (l ushr 8 xor ls[(0xffL and (l xor (this.invCount shr 24).toLong())).toInt()])
        l = ls[(0xffL and ((this.invCount shr 16).toLong() xor l)).toInt()] xor (l ushr 8)
        l = (l ushr 8 xor ls[(0xffL and (l xor (this.invCount shr 8).toLong())).toInt()])
        l = (l ushr 8 xor ls[((l xor this.invCount.toLong()) and 0xffL).toInt()])
        l = (l ushr 8 xor ls[(0xffL and (this.outline.toLong() xor l)).toInt()])
        l = ls[((l xor (this.graphicShadow shr 24).toLong()) and 0xffL).toInt()] xor (l ushr 8)
        l = (l ushr 8 xor ls[((l xor (this.graphicShadow shr 16).toLong()) and 0xffL).toInt()])
        if (i < 46) return -94L
        l = ls[((l xor (this.graphicShadow shr 8).toLong()) and 0xffL).toInt()] xor (l ushr 8)
        l = (l ushr 8 xor ls[(0xffL and (this.graphicShadow.toLong() xor l)).toInt()])
        l = (ls[(0xffL and (l xor this.itemNumMode.toLong())).toInt()] xor (l ushr 8))
        l = (l ushr 8 xor ls[(0xffL and ((if (this.itemWearCol) 1 else 0).toLong() xor l)).toInt()])
        return l
    }

    /* Not in the genuine-methods list, but Class126 implements Interface14
     * (declaring method53), so it must be present for compilation
     * regardless of whether it was hit at runtime. */
    override fun matches(i: Int, other: CacheKey?): Boolean {
        anInt4994++
        if (other !is ItemSpriteCacheKey) return false
        val data = other
        if (this.toolkitIndex != data.toolkitIndex) return false
        if (i <= 50) return true
        if (this.itemId != data.itemId) return false
        if (this.invCount != data.invCount) return false
        if (this.outline != data.outline) return false
        if (data.graphicShadow != this.graphicShadow) return false
        if (this.itemNumMode != data.itemNumMode) return false
        return !data.itemWearCol == !this.itemWearCol
    }

    companion object {
        var HSV_TO_RGB: IntArray? = null
        var anInt4988: Int = 0
        var anInt4994: Int = 0
    }
}
