package world.gregs.voidps.tools.render

import kotlin.math.min

/* Class348_Sub40_Sub38 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub38 : Class348_Sub40(1, false) {
    private var anInt9470 = 4096
    private var anInt9474 = 0

    override fun method3049(packet: Packet?, i: Int) {
        val i_1_ = i
        while_213_@ do {
            do {
                if (i_1_ == 0) {
                    anInt9474 = packet!!.readUnsignedShort()
                    break@while_213_
                } else if (i_1_ != 1) {
                    if (i_1_ == 2) break
                    break@while_213_
                }
                anInt9470 = packet!!.readUnsignedShort()
                break@while_213_
            } while (false)
            this.aBoolean7045 = packet!!.readUnsignedByte() == 1
        } while (false)
    }

    override fun method3047(i: Int): Array<IntArray> {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_3_ = this.method3039(i, 0)
            val is_4_ = is_3_!![0]
            val is_5_ = is_3_[1]
            val is_6_ = is_3_[2]
            val is_7_ = `is`!![0]
            val is_8_ = `is`[1]
            val is_9_ = `is`[2]
            var i_10_ = 0
            while ((i_10_ < Class348_Sub40_Sub6.anInt9139)) {
                val i_11_ = is_4_[i_10_]
                val i_12_ = is_5_[i_10_]
                val i_13_ = is_6_[i_10_]
                if (i_11_ < anInt9474) is_7_[i_10_] = anInt9474
                else is_7_[i_10_] = min(i_11_, anInt9470)
                if (anInt9474 > i_12_) is_8_[i_10_] = anInt9474
                else is_8_[i_10_] = min(i_12_, anInt9470)
                if (anInt9474 <= i_13_) {
                    is_9_[i_10_] = min(i_13_, anInt9470)
                } else is_9_[i_10_] = anInt9474
                i_10_++
            }
        }
        return `is`!!
    }
}
