package world.gregs.voidps.tools.render

internal class Class348_Sub40_Sub29 : Class348_Sub40(0, false) {
    private var anInt9374 = 0
    private var anIntArray9375: IntArray? = null
    private var anInt9379 = 0
    private var anInt9380 = -1
    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            var i_1_ = (anInt9374 * (if (Class348_Sub40_Sub33.anInt6212 == anInt9379) i else anInt9379 * i / Class348_Sub40_Sub33.anInt6212))
            val is_2_ = `is`!![0]
            val is_3_ = `is`[1]
            val is_4_ = `is`[2]
            if (Class348_Sub40_Sub6.anInt9139 == anInt9374) {
                var i_8_ = 0
                while ((Class348_Sub40_Sub6.anInt9139 > i_8_)) {
                    val i_9_ = anIntArray9375!![i_1_++]
                    is_4_[i_8_] = Class348_Sub40_Sub17.method1166(255, i_9_) shl 4
                    is_3_[i_8_] = Class348_Sub40_Sub17.method1166(i_9_ shr 4, 4080)
                    is_2_[i_8_] = Class348_Sub40_Sub17.method1166(16711680, i_9_) shr 12
                    i_8_++
                }
            } else {
                for (i_5_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                    val i_6_: Int = anInt9374 * i_5_ / Class348_Sub40_Sub6.anInt9139
                    val i_7_ = anIntArray9375!![i_6_ + i_1_]
                    is_4_[i_5_] = Class348_Sub40_Sub17.method1166(i_7_, 255) shl 4
                    is_3_[i_5_] = Class348_Sub40_Sub17.method1166(65280, i_7_) shr 4
                    is_2_[i_5_] = Class348_Sub40_Sub17.method1166(i_7_ shr 12, 4080)
                }
            }
        }
        return `is`
    }

    override fun method3045(i: Int, i_10_: Int) {
        super.method3045(i, i_10_)
        if (anInt9380 >= 0 && aTextureSource6247 != null) {
            val i_12_ = (if (!(aTextureSource6247!!.getMetrics(anInt9380)!!.small)) 128 else 64)
            anIntArray9375 = aTextureSource6247!!.method5(anInt9380, 1.0f, i_12_, i_12_)
            anInt9379 = i_12_
            anInt9374 = i_12_
        }
    }

    override fun method3046() {
        super.method3046()
        anIntArray9375 = null
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) anInt9380 = packet!!.readUnsignedShort()
    }

    override fun method3043(): Int {
        return anInt9380
    }

    companion object {
        var aTextureSource6247: TextureSource? = null
    }
}
