package world.gregs.voidps.tools.icon

/* Class244 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Js5TextureSource(js5: Js5?, js5_12_: Js5?, js5_13_: Js5?) : TextureSource {
    private val textureMetrics: Array<TextureMetrics?>
    private val sprites: Js5?
    private val aClass308_4622 = Class308(256)
    private val textures: Js5?
    private val textureCount: Int

    override fun method6(i: Int, i_0_: Int, f: Float, i_1_: Int, bool: Boolean, i_2_: Int): IntArray {
        if (i != -21540) getMetrics(-46, 6)
        anInt4617++
        return method1881(i_1_, false)!!.method3183(this, i_2_, i_0_, (textureMetrics[i_1_]!!.aBoolean207), f.toDouble(), textures, 11.toByte())
    }

    private fun method1881(i: Int, bool: Boolean): Class348_Sub42_Sub5? {
        anInt4620++
        val class348_sub42 = aClass308_4622.method2302(i.toLong(), (-34).toByte())
        if (class348_sub42 != null) return class348_sub42 as Class348_Sub42_Sub5
        val `is` = sprites!!.getFile(73.toByte(), i)
        if (`is` == null) return null
        if (bool != false) method1(-58, 1.9039171f, false, -106, -22, -18)
        val class348_sub42_sub5 = Class348_Sub42_Sub5(Packet(`is`))
        aClass308_4622.method2305(i.toLong(), class348_sub42_sub5, -1)
        return class348_sub42_sub5
    }

    override fun getMetrics(i: Int, i_3_: Int): TextureMetrics? {
        anInt4621++
        if (i_3_ != -6662) return null
        return textureMetrics[i]
    }

    override fun method5(bool: Boolean, i: Int, f: Float, i_4_: Int, i_5_: Int, i_6_: Int): IntArray {
        val i_7_ = 41 / ((i_6_ - -69) / 48)
        anInt4614++
        return method1881(i, false)!!.method3185(i_4_, this, 0, textureMetrics[i]!!.aBoolean207, f.toDouble(), bool, textures, i_5_)
    }

    override fun method4(i: Int, i_8_: Int): Boolean {
        anInt4616++
        val class348_sub42_sub5 = method1881(i_8_, false)
        if (i != -7953) method4(56, -109)
        return class348_sub42_sub5 != null && class348_sub42_sub5.method3184(this, textures, -85)
    }

    /* NOTE: method1 is NOT in the genuine-methods list (0 JaCoCo hits) for
     * this renderer, so it is stubbed rather than pulling in
     * Class348_Sub42_Sub5.method3186 (also not genuine) to compile a path
     * that never executes here. */
    override fun method1(i: Int, f: Float, bool: Boolean, i_9_: Int, i_10_: Int, i_11_: Int): FloatArray? {
        if (i_9_ != -30824) return null
        anInt4612++
        throw IllegalStateException() // unreachable per JaCoCo coverage
    }

    override fun method2(bool: Boolean): Int {
        anInt4618++
        if (bool != true) aLong4615 = -52L
        return textureCount
    }

    init {
        try {
            sprites = js5_12_
            textures = js5_13_
            val packet = Packet(js5!!.getFile(-1860, 0, 0)!!)
            textureCount = packet.readUnsignedShort(842397944)
            textureMetrics = arrayOfNulls<TextureMetrics>(textureCount)
            run {
                var i = 0
                while (textureCount > i) {
                    if (packet.readUnsignedByte(255) == 1) textureMetrics[i] = TextureMetrics()
                    i++
                }
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.disableable = packet.readUnsignedByte(255) == 0
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.small = packet.readUnsignedByte(255) == 1
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean204 = packet.readUnsignedByte(255) == 1
                    i++
                }
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.aByte216 = packet.readByte(-85)
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.alpha = packet.readByte(-113)
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.effectType = packet.readByte(-97)
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.effectParam1 = packet.readByte(-82)
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.aShort208 = packet.readUnsignedShort(842397944).toShort()
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.speedU = packet.readByte(-86)
                    i++
                }
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.speedV = packet.readByte(-104)
                    i++
                }
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean212 = packet.readUnsignedByte(255) == 1
                    i++
                }
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean207 = packet.readUnsignedByte(255) == 1
                    i++
                }
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.aByte205 = packet.readByte(-77)
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean217 = packet.readUnsignedByte(255) == 1
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean215 = packet.readUnsignedByte(255) == 1
                    i++
                }
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean218 = packet.readUnsignedByte(255) == 1
                    i++
                }
            }
            for (i in 0..<textureCount) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.colourOp = packet.readUnsignedByte(255)
            }
            run {
                var i = 0
                while (textureCount > i) {
                    if (textureMetrics[i] != null) textureMetrics[i]!!.effectParam2 = packet.readInt((-126).toByte())
                    i++
                }
            }
            var i = 0
            while (textureCount > i) {
                if (textureMetrics[i] != null) textureMetrics[i]!!.alphaBlendMode = packet.readUnsignedByte(255)
                i++
            }
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("tda.<init>(" + (if (js5 != null) "{...}" else "null") + ',' + (if (js5_12_ != null) "{...}" else "null") + ',' + (if (js5_13_ != null) "{...}" else "null") + ')'))
        }
    }

    companion object {
        var anInt4612: Int = 0
        var anInt4614: Int = 0
        var anInt4616: Int = 0
        var anInt4617: Int = 0
        var anInt4618: Int = 0
        var aLong4615: Long = 0
        var anInt4620: Int = 0
        var anInt4621: Int = 0
    }
}
