package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub4 **/
class TextureOpVerticalGradient : TextureOp(0, true) {
    override fun method3042(i: Int): IntArray? {
        val `is` = this.aMonochromeImageCache_7032!!.method1433(i)
        if (this.aMonochromeImageCache_7032!!.aBoolean2570) TextureOpIrregularBricks.method1579(`is`!!, 0, TextureOpPolarDistortion.anInt9139, TextureOpKaleidoscope.anIntArray6035!![i])
        return `is`
    }

    companion object {
        var aTextureSource9113: TextureSource? = null
    }
}
