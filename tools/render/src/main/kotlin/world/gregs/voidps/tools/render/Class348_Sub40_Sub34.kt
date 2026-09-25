package world.gregs.voidps.tools.render

import kotlin.math.sqrt

/* Class348_Sub40_Sub34 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub34 : Class348_Sub40(1, false) {
    private var anInt9438 = 4096
    private var aBoolean9439 = true

    override fun method3049(packet: Packet?, i: Int) {
        val i_6_ = i
        if (i_6_ == 0) {
            anInt9438 = packet!!.readUnsignedShort()
        } else if (i_6_ == 1) {
            aBoolean9439 = packet!!.readUnsignedByte() == 1
        }
    }

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_8_ = this.method3048(-1 + i and Class348_Sub40_Sub6.anInt6325, 0)!!
            val is_9_ = this.method3048(i, 0)!!
            val is_10_ = this.method3048(Class348_Sub40_Sub6.anInt6325 and 1 + i, 0)!!
            val is_11_ = `is`!![0]
            val is_12_ = `is`[1]
            val is_13_ = `is`[2]
            var i_14_ = 0
            while (Class348_Sub40_Sub6.anInt9139 > i_14_) {
                val i_15_ = (-is_8_[i_14_] + is_10_[i_14_]) * anInt9438
                val i_16_ = (anInt9438 * (is_9_[1 + i_14_ and Class348_Sub40_Sub37.anInt6076] - is_9_[Class348_Sub40_Sub37.anInt6076 and -1 + i_14_]))
                val i_17_ = i_16_ shr 12
                val i_18_ = i_15_ shr 12
                val i_19_ = i_17_ * i_17_ shr 12
                val i_20_ = i_18_ * i_18_ shr 12
                val i_21_ = (sqrt(((i_19_ - (-i_20_ + -4096)).toFloat() / 4096.0f).toDouble()) * 4096.0).toInt()
                var i_22_: Int
                var i_23_: Int
                var i_24_: Int
                if (i_21_ == 0) {
                    i_22_ = 0
                    i_23_ = 0
                    i_24_ = 0
                } else {
                    i_23_ = i_16_ / i_21_
                    i_22_ = i_15_ / i_21_
                    i_24_ = 16777216 / i_21_
                }
                if (aBoolean9439) {
                    i_24_ = (i_24_ shr 1) + 2048
                    i_23_ = (i_23_ shr 1) + 2048
                    i_22_ = (i_22_ shr 1) + 2048
                }
                is_11_[i_14_] = i_23_
                is_12_[i_14_] = i_22_
                is_13_[i_14_] = i_24_
                i_14_++
            }
        }
        return `is`
    }
}
