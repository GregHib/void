package world.gregs.voidps.tools.render

import kotlin.math.sqrt

/* Class348_Sub40_Sub36 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub36 : Class348_Sub40(0, true) {
    private var anInt9451 = 0
    private var anInt9453 = 1
    private var anInt9455 = 0

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9451 = packet!!.readUnsignedByte()
            1 -> anInt9455 = packet!!.readUnsignedByte()
            3 -> anInt9453 = packet!!.readUnsignedByte()
        }
    }

    override fun method3044() {
        Class220.method1605()
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_3_ = Class79.anIntArray6035!![i]
            val i_4_ = -2048 + i_3_ shr 1
            var i_5_ = 0
            while (i_5_ < Class348_Sub40_Sub6.anInt9139) {
                val i_6_ = Class348_Sub40_Sub8.anIntArray6432!![i_5_]
                val i_7_ = -2048 + i_6_ shr 1
                var i_8_: Int
                if (anInt9451 == 0) {
                    i_8_ = anInt9453 * (-i_3_ + i_6_)
                } else {
                    val i_9_ = i_7_ * i_7_ + i_4_ * i_4_ shr 12
                    i_8_ = (4096.0 * sqrt((i_9_.toFloat() / 4096.0f).toDouble())).toInt()
                    i_8_ = (3.141592653589793 * (anInt9453 * i_8_).toDouble()).toInt()
                }
                i_8_ -= 0xfff.inv() and i_8_
                if (anInt9455 != 0) {
                    if (anInt9455 == 2) {
                        i_8_ -= 2048
                        if (i_8_ < 0) i_8_ = -i_8_
                        i_8_ = 2048 - i_8_ shl 1
                    }
                } else {
                    i_8_ = 4096 + (Class220.anIntArray3068!![i_8_ shr 4 and 0xff]) shr 1
                }
                `is`!![i_5_] = i_8_
                i_5_++
            }
        }
        return `is`
    }
}
