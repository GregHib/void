package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub14 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub14 : Class348_Sub40(1, true) {
    private var anIntArray9208: IntArray? = null
    private var anIntArrayArray9210: Array<IntArray>? = null
    private var anInt9211 = 0
    private var anIntArray9214: IntArray? = null
    private var aShortArray9215: ShortArray? = ShortArray(257)

    override fun method3049(packet: Packet?, i: Int, i_0_: Int) {
        if (i == 0) {
            anInt9211 = packet!!.readUnsignedByte(i_0_ + -30760)
            anIntArrayArray9210 = Array(packet.readUnsignedByte(255)) { IntArray(2) }
            var i_1_ = 0
            while ((i_1_ < anIntArrayArray9210!!.size)) {
                anIntArrayArray9210!![i_1_][0] = packet.readUnsignedShort(842397944)
                anIntArrayArray9210!![i_1_][1] = packet.readUnsignedShort(842397944)
                i_1_++
            }
        }
        if (i_0_ == 31015) anInt9213++
    }

    private fun method3081(i: Int) {
        var i_2_ = anInt9211
        while_158_@ do {
            do {
                if (i_2_ == 2) {
                    i_2_ = 0
                    while (i_2_ < 257) {
                        val i_3_ = i_2_ shl 4
                        var i_4_: Int
                        i_4_ = 1
                        while (-1 + anIntArrayArray9210!!.size > i_4_) {
                            if (anIntArrayArray9210!![i_4_][0] > i_3_) break
                            i_4_++
                        }
                        val `is` = anIntArrayArray9210!![i_4_ - 1]
                        val is_5_ = anIntArrayArray9210!![i_4_]
                        val i_6_ = method3083(-2 + i_4_, (-120).toByte())!![1]
                        val i_7_ = `is`[1]
                        val i_8_ = is_5_[1]
                        val i_9_ = method3083(1 + i_4_, 81.toByte())!![1]
                        val i_10_ = ((-`is`[0] + i_3_ shl 12) / (is_5_[0] + -`is`[0]))
                        val i_11_ = i_10_ * i_10_ shr 12
                        val i_12_ = i_7_ + -i_8_ + (i_9_ + -i_6_)
                        val i_13_ = -i_12_ + i_6_ - i_7_
                        val i_14_ = i_8_ + -i_6_
                        val i_15_ = i_7_
                        val i_16_ = ((i_10_ * i_12_ shr 12) * i_11_ shr 12)
                        val i_17_ = i_13_ * i_11_ shr 12
                        val i_18_ = i_10_ * i_14_ shr 12
                        var i_19_ = i_15_ + (i_18_ + i_17_ + i_16_)
                        if (i_19_ <= -32768) i_19_ = -32767
                        if (i_19_ >= 32768) i_19_ = 32767
                        aShortArray9215!![i_2_] = i_19_.toShort()
                        i_2_++
                    }
                    break@while_158_
                } else if (i_2_ != 1) break
                i_2_ = 0
                while (i_2_ < 257) {
                    val i_20_ = i_2_ shl 4
                    var i_21_: Int
                    i_21_ = 1
                    while ((i_21_ < -1 + anIntArrayArray9210!!.size)) {
                        if (anIntArrayArray9210!![i_21_][0] > i_20_) break
                        i_21_++
                    }
                    val `is` = anIntArrayArray9210!![-1 + i_21_]
                    val is_22_ = anIntArrayArray9210!![i_21_]
                    val i_23_ = ((-`is`[0] + i_20_ shl 12) / (is_22_[0] - `is`[0]))
                    val i_24_ = (-(Class220.anIntArray4654!![(i_23_ and 0x1ff6) shr 5]) + 4096 shr 1)
                    val i_25_ = -i_24_ + 4096
                    var i_26_ = `is`[1] * i_25_ + is_22_[1] * i_24_ shr 12
                    if (i_26_ <= -32768) i_26_ = -32767
                    if (i_26_ >= 32768) i_26_ = 32767
                    aShortArray9215!![i_2_] = i_26_.toShort()
                    i_2_++
                }
                break@while_158_
            } while (false)
            i_2_ = 0
            while (i_2_ < 257) {
                val i_27_ = i_2_ shl 4
                var i_28_: Int
                i_28_ = 1
                while ((-1 + anIntArrayArray9210!!.size > i_28_)) {
                    if (anIntArrayArray9210!![i_28_][0] > i_27_) break
                    i_28_++
                }
                val `is` = anIntArrayArray9210!![i_28_ + -1]
                val is_29_ = anIntArrayArray9210!![i_28_]
                val i_30_ = (i_27_ + -`is`[0] shl 12) / (is_29_[0] + -`is`[0])
                val i_31_ = 4096 - i_30_
                var i_32_ = is_29_[1] * i_30_ + i_31_ * `is`[1] shr 12
                if (i_32_ <= -32768) i_32_ = -32767
                if (i_32_ >= 32768) i_32_ = 32767
                aShortArray9215!![i_2_] = i_32_.toShort()
                i_2_++
            }
        } while (false)
        if (i != -1) aShortArray9215 = null
        anInt9205++
    }

    override fun method3042(i: Int, i_33_: Int): IntArray {
        anInt9207++
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (i_33_ != 255) anIntArray9214 = null
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_34_ = this.method3048(i, 633706337, 0)
            for (i_35_ in 0..<Class348_Sub40_Sub6.Companion.anInt9139) {
                var i_36_ = is_34_!![i_35_] shr 4
                if (i_36_ < 0) i_36_ = 0
                if (i_36_ > 256) i_36_ = 256
                `is`!![i_35_] = aShortArray9215!![i_36_].toInt()
            }
        }
        return `is`!!
    }

    private fun method3082(i: Byte) {
        anInt9206++
        val `is` = anIntArrayArray9210!![0]
        val is_37_ = anIntArrayArray9210!![1]
        val is_38_ = anIntArrayArray9210!![-2 + anIntArrayArray9210!!.size]
        val is_39_ = anIntArrayArray9210!![-1 + anIntArrayArray9210!!.size]
        anIntArray9208 = intArrayOf(is_38_[0] - (is_39_[0] + -is_38_[0]), is_38_[1] - (-is_38_[1] + is_39_[1]))
        if (i.toInt() != 73) method3042(75, 39)
        anIntArray9214 = intArrayOf(`is`[0] + -is_37_[0] + `is`[0], `is`[1] - (-`is`[1] - -is_37_[1]))
    }

    override fun method3044(i: Int) {
        if (anIntArrayArray9210 == null) anIntArrayArray9210 = arrayOf(IntArray(2), intArrayOf(4096, 4096))
        if (i <= 108) anIntArrayArray9210 = null
        anInt9209++
        if (anIntArrayArray9210!!.size < 2) throw RuntimeException("Curve operation requires at least two markers")
        if (anInt9211 == 2) method3082(73.toByte())
        Class220.method1605(26188)
        method3081(-1)
    }

    private fun method3083(i: Int, i_40_: Byte): IntArray? {
        anInt9212++
        if (i < 0) return anIntArray9214
        val i_41_ = -48 % ((i_40_ - 13) / 56)
        if (anIntArrayArray9210!!.size <= i) return anIntArray9208
        return anIntArrayArray9210!![i]
    }

    companion object {
        var anInt9205: Int = 0
        var anInt9206: Int = 0
        var anInt9207: Int = 0
        var anInt9209: Int = 0
        var anInt9212: Int = 0
        var anInt9213: Int = 0
    }
}
