package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index

/* Class244 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Js5TextureSource(private val cache: Cache) : TextureSource {
    private val textureMetrics: Array<TextureMetrics?>
    private val aClass308_4622 = Class308(256)
    private val textureCount: Int

    override fun method6(i_0_: Int, f: Float, i_1_: Int, i_2_: Int): IntArray {
        return method1881(i_1_)!!.method3183(this, i_2_, i_0_, (textureMetrics[i_1_]!!.aBoolean207), f.toDouble(), cache)
    }

    private fun method1881(i: Int): Class348_Sub42_Sub5? {
        val class348_sub42 = aClass308_4622.method2302(i.toLong())
        if (class348_sub42 != null) return class348_sub42 as Class348_Sub42_Sub5
        val `is` = cache.data(Index.TEXTURES, i)
        if (`is` == null) return null
        val class348_sub42_sub5 = Class348_Sub42_Sub5(Packet(`is`))
        aClass308_4622.method2305(i.toLong(), class348_sub42_sub5)
        return class348_sub42_sub5
    }

    override fun getMetrics(i: Int): TextureMetrics? {
        return textureMetrics[i]
    }

    override fun method5(i: Int, f: Float, i_4_: Int, i_5_: Int): IntArray {
        return method1881(i)!!.method3185(i_4_, this, textureMetrics[i]!!.aBoolean207, f.toDouble(), cache, i_5_)
    }

    override fun method4(i_8_: Int): Boolean {
        val class348_sub42_sub5 = method1881(i_8_)
        return class348_sub42_sub5 != null && class348_sub42_sub5.method3184(this, cache)
    }

    init {
        val packet = Packet(cache.data(Index.TEXTURE_DEFINITIONS, 0, 0)!!)
        textureCount = packet.readUnsignedShort()
        textureMetrics = arrayOfNulls<TextureMetrics>(textureCount)
        for (i in 0..<textureCount) {
            if (packet.readUnsignedByte() == 1) textureMetrics[i] = TextureMetrics()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.disableable = packet.readUnsignedByte() == 0
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.small = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean204 = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aByte216 = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.alpha = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.effectType = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.effectParam1 = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aShort208 = packet.readUnsignedShort().toShort()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.speedU = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.speedV = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean212 = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean207 = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aByte205 = packet.readByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean217 = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean215 = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.aBoolean218 = packet.readUnsignedByte() == 1
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.colourOp = packet.readUnsignedByte()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.effectParam2 = packet.readInt()
        }
        for (i in 0..<textureCount) {
            if (textureMetrics[i] != null) textureMetrics[i]!!.alphaBlendMode = packet.readUnsignedByte()
        }
    }
}
