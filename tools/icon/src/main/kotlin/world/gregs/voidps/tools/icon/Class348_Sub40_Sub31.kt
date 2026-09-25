package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub31 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub31 : Class348_Sub40(1, false) {
    private var anInt9405 = 4
    private var anInt9410 = 4
    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val i_1_: Int = Class348_Sub40_Sub6.anInt9139 / anInt9405
            val i_2_ = Class79.anInt6212 / anInt9410
            val is_3_: Array<IntArray>
            if (i_2_ > 0) {
                val i_4_ = i % i_2_
                is_3_ = this.method3039(Class79.anInt6212 * i_4_ / i_2_, 0)!!
            } else is_3_ = this.method3039(0, 0)!!
            val is_5_ = is_3_[0]
            val is_6_ = is_3_[1]
            val is_7_ = is_3_[2]
            val is_8_ = `is`!![0]
            val is_9_ = `is`[1]
            val is_10_ = `is`[2]
            var i_11_ = 0
            while ((i_11_ < Class348_Sub40_Sub6.anInt9139)) {
                val i_12_: Int
                if (i_1_ <= 0) i_12_ = 0
                else {
                    val i_13_ = i_11_ % i_1_
                    i_12_ = Class348_Sub40_Sub6.anInt9139 * i_13_ / i_1_
                }
                is_8_[i_11_] = is_5_[i_12_]
                is_9_[i_11_] = is_6_[i_12_]
                is_10_[i_11_] = is_7_[i_12_]
                i_11_++
            }
        }
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        val i_15_ = i
        do {
            if (i_15_ == 0) {
                anInt9405 = packet!!.readUnsignedByte()
                break
            } else if (i_15_ != 1) break
            anInt9410 = packet!!.readUnsignedByte()
        } while (false)
    }
}
