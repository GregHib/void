package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub32 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub32 : Class348_Sub40(1, false) {
    override fun method3047(i: Int): Array<IntArray> {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_10_ = this.method3039(i, 0)
            val is_11_ = is_10_!![0]
            val is_12_ = is_10_[1]
            val is_13_ = is_10_[2]
            val is_14_ = `is`!![0]
            val is_15_ = `is`[1]
            val is_16_ = `is`[2]
            var i_17_ = 0
            while (Class348_Sub40_Sub6.anInt9139 > i_17_) {
                is_14_[i_17_] = -is_11_[i_17_] + 4096
                is_15_[i_17_] = 4096 + -is_12_[i_17_]
                is_16_[i_17_] = -is_13_[i_17_] + 4096
                i_17_++
            }
        }
        return `is`!!
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_19_ = this.method3048(i, 0)!!
            var i_20_ = 0
            while (i_20_ < Class348_Sub40_Sub6.anInt9139) {
                `is`!![i_20_] = 4096 + -is_19_[i_20_]
                i_20_++
            }
        }
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) this.aBoolean7045 = packet!!.readUnsignedByte() == 1
    }
}
