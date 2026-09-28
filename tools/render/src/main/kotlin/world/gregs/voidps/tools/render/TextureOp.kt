package world.gregs.voidps.tools.render

/** Class348_Sub40 **/
abstract class TextureOp(i: Int, var aBoolean7045: Boolean) {
    var aTextureOpArray7031: Array<TextureOp?> = arrayOfNulls(i)
    var aMonochromeImageCache_7032: MonochromeImageCache? = null
    var aColourImageCache_7033: ColourImageCache? = null
    var anInt7036: Int = 0

    open fun method3037(): Int {
        return -1
    }

    fun method3039(i_1_: Int, i_2_: Int): Array<IntArray>? {
        if (this.aTextureOpArray7031[i_2_]!!.aBoolean7045) {
            val `is` = this.aTextureOpArray7031[i_2_]!!.method3042(i_1_)!!
            return arrayOf(`is`, `is`, `is`)
        }
        return this.aTextureOpArray7031[i_2_]!!.method3047(i_1_)
    }

    open fun method3042(i: Int): IntArray? {
        throw IllegalStateException("This operation does not have a monochrome output")
    }

    open fun method3043(): Int {
        return -1
    }

    open fun method3044() {
    }

    open fun method3045(i: Int, i_54_: Int) {
        val i_56_ = (if (this.anInt7036 != 255) this.anInt7036 else i_54_)
        if (this.aBoolean7045) this.aMonochromeImageCache_7032 = MonochromeImageCache(i_56_, i_54_, i)
        else this.aColourImageCache_7033 = ColourImageCache(i_56_, i_54_, i)
    }

    open fun method3046() {
        if (this.aBoolean7045) {
            this.aMonochromeImageCache_7032!!.method1432()
            this.aMonochromeImageCache_7032 = null
        } else {
            this.aColourImageCache_7033!!.method2558()
            this.aColourImageCache_7033 = null
        }
    }

    open fun method3047(i: Int): Array<IntArray>? {
        throw IllegalStateException("This operation does not have a colour output")
    }

    fun method3048(i: Int, i_59_: Int): IntArray? {
        if (!this.aTextureOpArray7031[i_59_]!!.aBoolean7045) return (this.aTextureOpArray7031[i_59_]!!.method3047(i)!![0])
        return this.aTextureOpArray7031[i_59_]!!.method3042(i)
    }

    open fun method3049(packet: Packet?, i: Int) {
    }

    companion object {
        /** Class59_Sub1_Sub1.method557 */
        internal fun method557(i: Int): TextureOp? = when (i) {
            0 -> TextureOpMonochromeFill()
            1 -> TextureOpColourFill()
            2 -> TextureOpHorizontalGradient()
            3 -> TextureOpVerticalGradient()
            4 -> TextureOpBricks()
            5 -> TextureOpBoxBlur()
            6 -> TextureOpClamp()
            7 -> TextureOpCombine()
            8 -> TextureOpCurve()
            9 -> TextureOpFlip()
            10 -> TextureOpColorGradient()
            11 -> TextureOpColourise()
            12 -> TextureOpWaveform()
            13 -> TextureOpNoise()
            14 -> TextureOpWeave()
            15 -> TextureOpVoronoiNoise()
            16 -> TextureOpHerringbone()
            17 -> TextureOpHslAdjust()
            18 -> TextureOpTiledSprite()
            19 -> TextureOpPolarDistortion()
            20 -> TextureOpTile()
            21 -> TextureOpInterpolate()
            22 -> TextureOpInvert()
            23 -> TextureOpKaleidoscope()
            24 -> TextureOpMonochrome()
            25 -> TextureOpBrightness()
            26 -> TextureOpBinary()
            27 -> TextureOpSquareWaveform()
            28 -> TextureOpIrregularBricks()
            29 -> TextureOpRasterizer()
            30 -> TextureOpRange()
            31 -> TextureOpMandelbrot()
            32 -> TextureOpEmboss()
            33 -> TextureOpColorEdgeDetector()
            34 -> TextureOpPerlinNoise()
            35 -> TextureOpMonochromeEdgeDetector()
            36 -> TextureOpTexture()
            37 -> TextureOp37()
            38 -> TextureOpLineNoise()
            39 -> TextureOpSprite()
            else -> null
        }

        fun method3031(packet: Packet): TextureOp? {
            packet.readUnsignedByte()
            val i_0_ = packet.readUnsignedByte()
            val class348_sub40 = method557(i_0_)
            class348_sub40!!.anInt7036 = packet.readUnsignedByte()
            val i_1_ = packet.readUnsignedByte()
            var i_2_ = 0
            while (i_1_ > i_2_) {
                val i_3_ = packet.readUnsignedByte()
                class348_sub40.method3049(packet, i_3_)
                i_2_++
            }
            class348_sub40.method3044()
            return class348_sub40
        }
    }
}
