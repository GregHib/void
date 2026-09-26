package world.gregs.voidps.tools.render

/*
 * Vector shape used by Class348_Sub40_Sub39 (texture op 29). Subclasses and their
 * packet readers (Class182.method1374, Class348_Sub23_Sub1.method2970,
 * Class348_Sub40.method3036, Class265.method2022) are kept together here.
 */
/** Class50 **/
internal abstract class TextureOpRasterizerShape(i: Int, i_7_: Int, i_8_: Int) {
    var anInt862: Int = i_8_
    var anInt864: Int = i
    var anInt865: Int = i_7_

    /** Java method455(i, i_0_, dummy) */
    abstract fun method455(i: Int, i_0_: Int)

    /** Java method456(dummy, i_2_, i_3_) */
    abstract fun method456(i_2_: Int, i_3_: Int)

    /** Java method457(i_4_, i_5_, dummy) */
    abstract fun method457(i: Int, i_4_: Int)

    companion object {
        /** Class182.method1374 */
        fun method1374(packet: Packet): TextureOpRasterizerLine = TextureOpRasterizerLine(packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readMedium(), packet.readUnsignedByte())

        /** Class348_Sub23_Sub1.method2970 */
        fun method2970(packet: Packet): TextureOpRasterizerBezierCurve = TextureOpRasterizerBezierCurve(packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readMedium(), packet.readUnsignedByte())

        /** Class348_Sub40.method3036 */
        fun method3036(packet: Packet): TextureOpRasterizerRectangle = TextureOpRasterizerRectangle(packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readMedium(), packet.readMedium(), packet.readUnsignedByte())

        /** Class265.method2022 */
        fun method2022(packet: Packet): TextureOpRasterizerEllipse = TextureOpRasterizerEllipse(packet.readShort(), packet.readShort(), packet.readShort(), packet.readShort(), packet.readMedium(), packet.readMedium(), packet.readUnsignedByte())
    }
}

/* Ellipse */
internal class TextureOpRasterizerEllipse(i: Int, i_6_: Int, i_7_: Int, i_8_: Int, i_9_: Int, i_10_: Int, i_11_: Int) : TextureOpRasterizerShape(i_9_, i_10_, i_11_) {
    private val anInt5212: Int = i_6_
    private val anInt5213: Int = i_7_
    private val anInt5214: Int = i
    private val anInt5222: Int = i_8_

    override fun method455(i: Int, i_0_: Int) {
        val i_2_ = anInt5214 * i shr 12
        val i_3_ = i * anInt5213 shr 12
        val i_4_ = anInt5212 * i_0_ shr 12
        val i_5_ = i_0_ * anInt5222 shr 12
        ShapeRasterizer.method2267(i_5_, i_2_, this.anInt865, this.anInt864, this.anInt862, i_4_, i_3_)
    }

    override fun method456(i_2_: Int, i_3_: Int) {
        val i_16_ = i_2_ * anInt5214 shr 12
        val i_17_ = anInt5213 * i_2_ shr 12
        val i_18_ = anInt5212 * i_3_ shr 12
        val i_19_ = i_3_ * anInt5222 shr 12
        ShapeRasterizer.method3641(i_18_, i_19_, this.anInt864, i_17_, i_16_)
    }

    override fun method457(i: Int, i_4_: Int) {
    }
}

/* Rectangle */
internal class TextureOpRasterizerRectangle(i: Int, i_6_: Int, i_7_: Int, i_8_: Int, i_9_: Int, i_10_: Int, i_11_: Int) : TextureOpRasterizerShape(i_9_, i_10_, i_11_) {
    private val anInt5227: Int = i_6_
    private val anInt5230: Int = i_8_
    private val anInt5231: Int = i
    private val anInt5232: Int = i_7_

    override fun method455(i: Int, i_0_: Int) {
        val i_2_ = i * anInt5231 shr 12
        val i_3_ = anInt5232 * i shr 12
        val i_4_ = i_0_ * anInt5227 shr 12
        val i_5_ = i_0_ * anInt5230 shr 12
        ShapeRasterizer.method1308(this.anInt864, i_4_, i_2_, this.anInt865, this.anInt862, i_3_, i_5_)
    }

    override fun method456(i_2_: Int, i_3_: Int) {
        val i_20_ = i_2_ * anInt5231 shr 12
        val i_21_ = anInt5232 * i_2_ shr 12
        val i_22_ = i_3_ * anInt5227 shr 12
        val i_23_ = i_3_ * anInt5230 shr 12
        ShapeRasterizer.method2486(i_21_, this.anInt864, i_23_, i_20_, i_22_)
    }

    override fun method457(i: Int, i_4_: Int) {
        val i_14_ = i_4_ * anInt5231 shr 12
        val i_15_ = i_4_ * anInt5232 shr 12
        val i_16_ = i * anInt5227 shr 12
        val i_17_ = i * anInt5230 shr 12
        ShapeRasterizer.method872(this.anInt865, this.anInt862, i_16_, i_17_, i_15_, i_14_)
    }
}

/* Cubic bezier curve (outline only) */
internal class TextureOpRasterizerBezierCurve(i: Int, i_22_: Int, i_23_: Int, i_24_: Int, i_25_: Int, i_26_: Int, i_27_: Int, i_28_: Int, i_29_: Int, i_30_: Int) : TextureOpRasterizerShape(-1, i_29_, i_30_) {
    private val anInt5236: Int = i_25_
    private val anInt5237: Int = i_26_
    private val anInt5243: Int = i_27_
    private val anInt5245: Int = i_23_
    private val anInt5246: Int = i
    private val anInt5247: Int = i_24_
    private val anInt5249: Int = i_28_
    private val anInt5250: Int = i_22_

    override fun method455(i: Int, i_0_: Int) {
    }

    override fun method456(i_2_: Int, i_3_: Int) {
    }

    override fun method457(i: Int, i_4_: Int) {
        val i_14_ = anInt5246 * i_4_ shr 12
        val i_15_ = anInt5250 * i shr 12
        val i_16_ = anInt5245 * i_4_ shr 12
        val i_17_ = anInt5247 * i shr 12
        val i_18_ = i_4_ * anInt5236 shr 12
        val i_19_ = anInt5237 * i shr 12
        val i_20_ = i_4_ * anInt5243 shr 12
        val i_21_ = anInt5249 * i shr 12
        ShapeRasterizer.method3540(i_15_, i_17_, this.anInt865, i_21_, i_16_, i_14_, i_18_, i_19_, i_20_)
    }
}

/* Line (outline only) */
internal class TextureOpRasterizerLine(i: Int, i_10_: Int, i_11_: Int, i_12_: Int, i_13_: Int, i_14_: Int) : TextureOpRasterizerShape(-1, i_13_, i_14_) {
    private val anInt5255: Int = i
    private val anInt5258: Int = i_12_
    private val anInt5259: Int = i_10_
    private val anInt5264: Int = i_11_

    override fun method455(i: Int, i_0_: Int) {
    }

    override fun method456(i_2_: Int, i_3_: Int) {
    }

    override fun method457(i: Int, i_4_: Int) {
        val i_4 = i_4_ * anInt5255 shr 12
        val i_5_ = i_4_ * anInt5264 shr 12
        val i_6_ = anInt5259 * i shr 12
        val i_7_ = anInt5258 * i shr 12
        ShapeRasterizer.method2665(i_4, this.anInt865, i_6_, i_7_, i_5_)
    }
}
