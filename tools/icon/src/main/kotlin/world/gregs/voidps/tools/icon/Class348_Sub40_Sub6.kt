package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub6 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub6 : Class348_Sub40(3, false) {
    private var anInt9133 = 32768
    override fun method3042(i: Int): IntArray {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_1_ = this.method3048(i, 1)
            val is_2_ = this.method3048(i, 2)
            for (i_3_ in 0..<anInt9139) {
                val i_4_ = 0xff and (is_1_!![i_3_] shr 4)
                val i_5_ = anInt9133 * is_2_!![i_3_] shr 12
                val i_6_ = Class220.anIntArray4654!![i_4_] * i_5_ shr 12
                val i_7_ = Class220.anIntArray3068!![i_4_] * i_5_ shr 12
                val i_8_ = i_3_ - -(i_6_ shr 12) and Class348_Sub40_Sub37.anInt6076
                val i_9_ = i - -(i_7_ shr 12) and anInt6325
                val is_10_ = this.method3048(i_9_, 0)
                `is`!![i_3_] = is_10_!![i_8_]
            }
        }
        return `is`!!
    }

    override fun method3049(packet: Packet?, i: Int) {
        val i_12_ = i
        do {
            if (i_12_ == 0) {
                anInt9133 = packet!!.readUnsignedShort() shl 4
                break
            } else if (i_12_ != 1) break
            this.aBoolean7045 = packet!!.readUnsignedByte() == 1
        } while (false)
    }

    override fun method3044() {
        Class220.method1605()
    }

    override fun method3047(i: Int): Array<IntArray> {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_14_ = this.method3048(i, 1)
            val is_15_ = this.method3048(i, 2)
            val is_16_ = `is`!![0]
            val is_17_ = `is`[1]
            val is_18_ = `is`[2]
            var i_19_ = 0
            while (anInt9139 > i_19_) {
                val i_20_ = 0xff and (255 * is_14_!![i_19_] shr 12)
                val i_21_ = anInt9133 * is_15_!![i_19_] shr 12
                val i_22_ = i_21_ * Class220.anIntArray4654!![i_20_] shr 12
                val i_23_ = i_21_ * Class220.anIntArray3068!![i_20_] shr 12
                val i_24_ = i_19_ + (i_22_ shr 12) and Class348_Sub40_Sub37.anInt6076
                val i_25_ = (i_23_ shr 12) + i and anInt6325
                val is_26_ = this.method3039(i_25_, 0)
                is_16_[i_19_] = is_26_!![0][i_24_]
                is_17_[i_19_] = is_26_[1][i_24_]
                is_18_[i_19_] = is_26_[2][i_24_]
                i_19_++
            }
        }
        return `is`!!
    }

    companion object {
        var anInt6325: Int = 0
        var anInt9139: Int = 0
    }
}
