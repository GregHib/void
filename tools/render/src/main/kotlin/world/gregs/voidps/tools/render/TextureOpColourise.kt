package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub26 **/
internal class TextureOpColourise : TextureOp(1, false) {
    private var anInt9344 = 4096
    private var anInt9347 = 4096
    private var anInt9354 = 4096

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aColourImageCache_7033!!.method2557(i)
        if (this.aColourImageCache_7033!!.aBoolean4035) {
            val is_4_ = this.method3039(i, 0)!!
            val is_5_ = is_4_[0]
            val is_6_ = is_4_[1]
            val is_7_ = is_4_[2]
            val is_8_ = `is`!![0]
            val is_9_ = `is`[1]
            val is_10_ = `is`[2]
            var i_11_ = 0
            while (i_11_ < TextureOpPolarDistortion.anInt9139) {
                val i_12_ = is_5_[i_11_]
                val i_13_ = is_7_[i_11_]
                val i_14_ = is_6_[i_11_]
                if (i_13_ != i_12_ || i_13_ != i_14_) {
                    is_8_[i_11_] = anInt9344
                    is_9_[i_11_] = anInt9354
                    is_10_[i_11_] = anInt9347
                } else {
                    is_8_[i_11_] = i_12_ * anInt9344 shr 12
                    is_9_[i_11_] = anInt9354 * i_13_ shr 12
                    is_10_[i_11_] = anInt9347 * i_14_ shr 12
                }
                i_11_++
            }
        }
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9344 = packet!!.readUnsignedShort()
            1 -> anInt9354 = packet!!.readUnsignedShort()
            2 -> anInt9347 = packet!!.readUnsignedShort()
        }
    }

    companion object {
        var aBooleanArray9351: BooleanArray? = BooleanArray(8)
    }
}
