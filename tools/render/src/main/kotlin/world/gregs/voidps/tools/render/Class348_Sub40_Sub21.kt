package world.gregs.voidps.tools.render

internal class Class348_Sub40_Sub21 : Class348_Sub40(0, true) {
    private var anInt9266 = 0
    private var anInt9269 = 2048
    private var anInt9276 = 4096
    private var anInt9277 = 8192
    private var anInt9278 = 0
    private var anInt9279 = 12288
    private var anInt9281 = 2048

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_1_ = -2048 + Class348_Sub40_Sub33.anIntArray6035!![i]
            var i_2_ = 0
            while (i_2_ < Class348_Sub40_Sub6.anInt9139) {
                val i_3_ = -2048 + Class348_Sub40_Sub8.anIntArray6432!![i_2_]
                var i_4_ = i_3_ + anInt9269
                i_4_ = if (i_4_ < -2048) 4096 + i_4_ else i_4_
                i_4_ = if (i_4_ <= 2048) i_4_ else -4096 + i_4_
                var i_5_ = anInt9278 + i_1_
                i_5_ = if (i_5_ < -2048) i_5_ + 4096 else i_5_
                i_5_ = if (i_5_ <= 2048) i_5_ else i_5_ - 4096
                var i_6_ = i_3_ + anInt9266
                i_6_ = if (i_6_ < -2048) i_6_ + 4096 else i_6_
                i_6_ = if (i_6_ > 2048) i_6_ - 4096 else i_6_
                var i_7_ = anInt9281 + i_1_
                i_7_ = if (i_7_ < -2048) i_7_ + 4096 else i_7_
                i_7_ = if (i_7_ > 2048) -4096 + i_7_ else i_7_
                `is`!![i_2_] = if (method3108(i_5_, i_4_) || method3105(i_7_, i_6_)) 4096 else 0
                i_2_++
            }
        }
        return `is`
    }

    override fun method3044() {
        Class348_Sub40_Sub6.method1605()
    }

    private fun method3105(i: Int, i_9_: Int): Boolean {
        val i_10_ = (i_9_ + i) * anInt9279 shr 12
        var i_11_ = Class348_Sub40_Sub6.anIntArray4654!![(i_10_ * 255 and 0xfff2f) shr 12]
        i_11_ = (i_11_ shl 12) / anInt9279
        i_11_ = (i_11_ shl 12) / anInt9277
        i_11_ = i_11_ * anInt9276 shr 12
        return i_11_ > i - i_9_ && i - i_9_ > -i_11_
    }

    private fun method3108(i: Int, i_21_: Int): Boolean {
        val i_22_ = anInt9279 * (i - i_21_) shr 12
        var i_23_ = Class348_Sub40_Sub6.anIntArray4654!![(0xff530 and 255 * i_22_) shr 12]
        i_23_ = (i_23_ shl 12) / anInt9279
        i_23_ = (i_23_ shl 12) / anInt9277
        i_23_ = anInt9276 * i_23_ shr 12
        return i_23_ > i_21_ + i && -i_23_ < i + i_21_
    }

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9269 = packet!!.readUnsignedShort()
            1 -> anInt9278 = packet!!.readUnsignedShort()
            2 -> anInt9266 = packet!!.readUnsignedShort()
            3 -> anInt9281 = packet!!.readUnsignedShort()
            4 -> anInt9279 = packet!!.readUnsignedShort()
            5 -> anInt9276 = packet!!.readUnsignedShort()
            6 -> anInt9277 = packet!!.readUnsignedShort()
        }
    }

    companion object {
        var crc64table: LongArray = LongArray(256)

        init {
            for (i in 0..255) {
                var l = i.toLong()
                for (i_24_ in 0..7) {
                    if ((0x1L and l) == 1L) l = 0x3693a86a2878f0bdL.inv() xor (l ushr 1)
                    else l = l ushr 1
                }
                crc64table[i] = l
            }
        }
    }
}
