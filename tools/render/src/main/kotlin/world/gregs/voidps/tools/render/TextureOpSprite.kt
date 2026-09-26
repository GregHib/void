package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub17 **/
internal open class TextureOpSprite : TextureOp(0, false) {
    var anIntArray9232: IntArray? = null
    var anInt9237: Int = 0
    var anInt9241: Int = 0
    private var anInt9243 = -1
    fun method3090(): Boolean {
        if (this.anIntArray9232 != null) return true
        if (anInt9243 >= 0) {
            val indexedSprite: IndexedSprite? = IndexedSprite.method1512(Node.aCache_4286!!, anInt9243)
            indexedSprite!!.method1524()
            this.anIntArray9232 = indexedSprite.method1516()
            this.anInt9237 = indexedSprite.anInt2702
            this.anInt9241 = indexedSprite.anInt2696
            return true
        }
        return false
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) anInt9243 = packet!!.readUnsignedShort()
    }

    override fun method3037(): Int {
        return anInt9243
    }

    override fun method3046() {
        super.method3046()
        this.anIntArray9232 = null
    }

    override fun method3047(i: Int): Array<IntArray> {
        val `is` = this.aColourImageCache_7033!!.method2557(i)
        if (this.aColourImageCache_7033!!.aBoolean4035 && method3090()) {
            val is_2_ = `is`!![0]
            val is_3_ = `is`[1]
            val is_4_ = `is`[2]
            var i_5_ = (this.anInt9237 * (if (this.anInt9241 != TextureOpKaleidoscope.anInt6212) (this.anInt9241 * i / TextureOpKaleidoscope.anInt6212) else i))
            if (TextureOpPolarDistortion.anInt9139 == this.anInt9237) {
                var i_6_ = 0
                while ((TextureOpPolarDistortion.anInt9139 > i_6_)) {
                    val i_7_ = this.anIntArray9232!![i_5_++]
                    is_4_[i_6_] = method1166(4080, i_7_ shl 4)
                    is_3_[i_6_] = method1166(65280, i_7_) shr 4
                    is_2_[i_6_] = method1166(4080, i_7_ shr 12)
                    i_6_++
                }
            } else {
                var i_8_ = 0
                while ((TextureOpPolarDistortion.anInt9139 > i_8_)) {
                    val i_9_: Int = (this.anInt9237 * i_8_ / TextureOpPolarDistortion.anInt9139)
                    val i_10_ = (this.anIntArray9232!![i_9_ + i_5_])
                    is_4_[i_8_] = method1166(i_10_, 255) shl 4
                    is_3_[i_8_] = method1166(i_10_ shr 4, 4080)
                    is_2_[i_8_] = method1166(i_10_, 16711680) shr 12
                    i_8_++
                }
            }
        }
        return `is`!!
    }

    companion object {
        fun method1166(i: Int, i_12_: Int): Int {
            return i and i_12_
        }
    }
}
