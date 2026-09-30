package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub17_Sub1 **/
internal class TextureOpTiledSprite : TextureOpSprite() {
    override fun method3047(i: Int): Array<IntArray> {
        val `is` = this.aColourImageCache_7033!!.method2557(i)!!
        if (this.aColourImageCache_7033!!.aBoolean4035 && this.method3090()) {
            val is_1_ = `is`[0]
            val is_2_ = `is`[1]
            val is_3_ = `is`[2]
            val i_4_ = (this.anInt9241 * (i % this.anInt9241))
            for (i_5_ in 0..<TextureOpPolarDistortion.anInt9139) {
                val i_6_ = this.anIntArray9232!![i_5_ % this.anInt9237 + i_4_]
                is_3_[i_5_] = 4080 and (i_6_ shl 4)
                is_2_[i_5_] = (65280 and i_6_) shr 4
                is_1_[i_5_] = 4080 and (i_6_ shr 12)
            }
        }
        return `is`
    }
}
