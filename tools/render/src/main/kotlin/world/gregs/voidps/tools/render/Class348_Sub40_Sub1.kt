package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub1 : Class348_Sub40(1, false) {
    private var anInt9084 = 4096
    private val anIntArray9086 = IntArray(3)
    private var anInt9091 = 4096
    private var anInt9092 = 4096
    private var anInt9094 = 409

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9094 = packet!!.readUnsignedShort()
            1 -> anInt9084 = packet!!.readUnsignedShort()
            2 -> anInt9091 = packet!!.readUnsignedShort()
            3 -> anInt9092 = packet!!.readUnsignedShort()
            4 -> {
                val i_2_ = packet!!.readMedium()
                // Class139.method1166(0, i_2_ >> 12) in the client, which is always 0
                anIntArray9086[2] = 0 and (i_2_ shr 12)
                anIntArray9086[1] = (i_2_ and 65280) shr 4
                anIntArray9086[0] = (i_2_ shl 4) and 267386880
            }
        }
    }

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_6_ = this.method3039(i, 0)!!
            val is_7_ = is_6_[0]
            val is_8_ = is_6_[1]
            val is_9_ = is_6_[2]
            val is_10_ = `is`!![0]
            val is_11_ = `is`[1]
            val is_12_ = `is`[2]
            for (i_13_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                val i_14_ = is_7_[i_13_]
                var i_15_ = i_14_ - anIntArray9086[0]
                if (i_15_ < 0) i_15_ = -i_15_
                if (anInt9094 < i_15_) {
                    is_10_[i_13_] = i_14_
                    is_11_[i_13_] = is_8_[i_13_]
                    is_12_[i_13_] = is_9_[i_13_]
                } else {
                    val i_16_ = is_8_[i_13_]
                    i_15_ = -anIntArray9086[1] + i_16_
                    if (i_15_ < 0) i_15_ = -i_15_
                    if (anInt9094 < i_15_) {
                        is_10_[i_13_] = i_14_
                        is_11_[i_13_] = i_16_
                        is_12_[i_13_] = is_9_[i_13_]
                    } else {
                        val i_17_ = is_9_[i_13_]
                        i_15_ = -anIntArray9086[2] + i_17_
                        if (i_15_ < 0) i_15_ = -i_15_
                        if (i_15_ > anInt9094) {
                            is_10_[i_13_] = i_14_
                            is_11_[i_13_] = i_16_
                            is_12_[i_13_] = i_17_
                        } else {
                            is_10_[i_13_] = i_14_ * anInt9092 shr 12
                            is_11_[i_13_] = i_16_ * anInt9091 shr 12
                            is_12_[i_13_] = i_17_ * anInt9084 shr 12
                        }
                    }
                }
            }
        }
        return `is`
    }
}
