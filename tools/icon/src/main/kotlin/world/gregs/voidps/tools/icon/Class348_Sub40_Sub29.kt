package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub29 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub29 : Class348_Sub40(0, false) {
    private var anInt9374 = 0
    private var anIntArray9375: IntArray? = null
    private var anInt9379 = 0
    private var anInt9380 = -1
    override fun method3047(i: Int, i_0_: Int): Array<IntArray>? {
        if (i_0_ != -1564599039) return null
        anInt9378++
        val `is` = this.aClass322_7033!!.method2557(-94, i)
        if (this.aClass322_7033!!.aBoolean4035) {
            var i_1_ = (anInt9374 * (if (Class79.anInt6212 == anInt9379) i else anInt9379 * i / Class79.anInt6212))
            val is_2_ = `is`!![0]
            val is_3_ = `is`[1]
            val is_4_ = `is`[2]
            if (Class348_Sub40_Sub6.Companion.anInt9139 == anInt9374) {
                var i_8_ = 0
                while ((Class348_Sub40_Sub6.Companion.anInt9139 > i_8_)) {
                    val i_9_ = anIntArray9375!![i_1_++]
                    is_4_[i_8_] = Class348_Sub40_Sub17.method1166(255, i_9_) shl 4
                    is_3_[i_8_] = Class348_Sub40_Sub17.method1166(i_9_ shr 4, 4080)
                    is_2_[i_8_] = Class348_Sub40_Sub17.method1166(16711680, i_9_) shr 12
                    i_8_++
                }
            } else {
                for (i_5_ in 0..<Class348_Sub40_Sub6.Companion.anInt9139) {
                    val i_6_: Int = anInt9374 * i_5_ / Class348_Sub40_Sub6.Companion.anInt9139
                    val i_7_ = anIntArray9375!![i_6_ + i_1_]
                    is_4_[i_5_] = Class348_Sub40_Sub17.method1166(i_7_, 255) shl 4
                    is_3_[i_5_] = Class348_Sub40_Sub17.method1166(65280, i_7_) shr 4
                    is_2_[i_5_] = Class348_Sub40_Sub17.method1166(i_7_ shr 12, 4080)
                }
            }
        }
        return `is`
    }

    override fun method3045(i: Int, i_10_: Int, i_11_: Int) {
        super.method3045(i, i_10_, i_11_)
        anInt9382++
        if (anInt9380 >= 0 && aTextureSource6247 != null) {
            val i_12_ = (if (!(aTextureSource6247!!.getMetrics(anInt9380, -6662)!!.small)) 128 else 64)
            anIntArray9375 = aTextureSource6247!!.method5(false, anInt9380, 1.0f, i_12_, i_12_, -123)
            anInt9379 = i_12_
            anInt9374 = i_12_
        }
    }

    override fun method3046(i: Byte) {
        if (i >= -102) anInt9374 = -104
        anInt9373++
        super.method3046((-107).toByte())
        anIntArray9375 = null
    }

    override fun method3049(packet: Packet?, i: Int, i_15_: Int) {
        if (i_15_ == 31015) {
            if (i == 0) anInt9380 = packet!!.readUnsignedShort(842397944)
            anInt9376++
        }
    }

    override fun method3043(i: Int): Int {
        if (i != -1) anInt9379 = 10
        anInt9381++
        return anInt9380
    }

    companion object {
        var aTextureSource6247: TextureSource? = null
        var anInt9373: Int = 0
        var anInt9376: Int = 0
        var anInt9378: Int = 0
        var anInt9381: Int = 0
        var anInt9382: Int = 0
    }
}
