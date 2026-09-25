package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub27 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub27 : Class348_Sub40(3, false) {
    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_1_ = this.method3048(i, 2)
            val is_2_ = this.method3039(i, 0)
            val is_3_ = this.method3039(i, 1)
            val is_4_ = `is`!![0]
            val is_5_ = `is`[1]
            val is_6_ = `is`[2]
            val is_7_ = is_2_!![0]
            val is_8_ = is_2_[1]
            val is_9_ = is_2_[2]
            val is_10_ = is_3_!![0]
            val is_11_ = is_3_[1]
            val is_12_ = is_3_[2]
            var i_13_ = 0
            while ((i_13_ < Class348_Sub40_Sub6.anInt9139)) {
                val i_14_ = is_1_!![i_13_]
                if (i_14_ == 4096) {
                    is_4_[i_13_] = is_7_[i_13_]
                    is_5_[i_13_] = is_8_[i_13_]
                    is_6_[i_13_] = is_9_[i_13_]
                } else if (i_14_ == 0) {
                    is_4_[i_13_] = is_10_[i_13_]
                    is_5_[i_13_] = is_11_[i_13_]
                    is_6_[i_13_] = is_12_[i_13_]
                } else {
                    val i_15_ = 4096 + -i_14_
                    is_4_[i_13_] = (is_10_[i_13_] * i_15_ + is_7_[i_13_] * i_14_ shr 12)
                    is_5_[i_13_] = (is_11_[i_13_] * i_15_ + is_8_[i_13_] * i_14_ shr 12)
                    is_6_[i_13_] = (is_9_[i_13_] * i_14_ - -(is_12_[i_13_] * i_15_) shr 12)
                }
                i_13_++
            }
        }
        return `is`
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_17_ = this.method3048(i, 0)
            val is_18_ = this.method3048(i, 1)
            val is_19_ = this.method3048(i, 2)
            var i_20_ = 0
            while (Class348_Sub40_Sub6.anInt9139 > i_20_) {
                val i_21_ = is_19_!![i_20_]
                if (i_21_ != 4096) {
                    if (i_21_ == 0) `is`!![i_20_] = is_18_!![i_20_]
                    else `is`!![i_20_] = (i_21_ * is_17_!![i_20_] - -(is_18_!![i_20_] * (-i_21_ + 4096)) shr 12)
                } else `is`!![i_20_] = is_17_!![i_20_]
                i_20_++
            }
        }
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) this.aBoolean7045 = packet!!.readUnsignedByte() == 1
    }
}
