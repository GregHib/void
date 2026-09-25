package world.gregs.voidps.tools.render

import kotlin.math.atan2

/* Class348_Sub40_Sub33 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub33 : Class348_Sub40(1, false) {

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_3_ = `is`!![0]
            val is_4_ = `is`[1]
            val is_5_ = `is`[2]
            var i_6_ = 0
            while (Class348_Sub40_Sub6.anInt9139 > i_6_) {
                method3139(i_6_, i)
                val is_7_ = this.method3039(Class344.anInt4265, 0)!!
                is_3_[i_6_] = is_7_[0][Class121.anInt1796]
                is_4_[i_6_] = is_7_[1][Class121.anInt1796]
                is_5_[i_6_] = is_7_[2][Class121.anInt1796]
                i_6_++
            }
        }
        return `is`
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            var i_9_ = 0
            while (i_9_ < Class348_Sub40_Sub6.anInt9139) {
                method3139(i_9_, i)
                val is_10_ = this.method3048(Class344.anInt4265, 0)!!
                `is`!![i_9_] = is_10_[Class121.anInt1796]
                i_9_++
            }
        }
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) this.aBoolean7045 = packet!!.readUnsignedByte() == 1
    }

    private fun method3139(i: Int, i_12_: Int) {
        val i_14_ = Class348_Sub40_Sub8.anIntArray6432!![i]
        val i_15_ = Class79.anIntArray6035!![i_12_]
        val f = atan2((i_14_ + -2048).toDouble(), (i_15_ + -2048).toDouble()).toFloat()
        val d = f.toDouble()
        if (d >= -3.141592653589793 && d <= -2.356194490192345) {
            Class344.anInt4265 = i_12_
            Class121.anInt1796 = i
        } else if (!(d <= -1.5707963267948966) || !(d >= -2.356194490192345)) {
            if (!(d <= -0.7853981633974483) || !(d >= -1.5707963267948966)) {
                if (!(f <= 0.0f) || !(d >= -0.7853981633974483)) {
                    if (!(f >= 0.0f) || !(d <= 0.7853981633974483)) {
                        if (!(d >= 0.7853981633974483) || !(d <= 1.5707963267948966)) {
                            if (d >= 1.5707963267948966 && d <= 2.356194490192345) {
                                Class344.anInt4265 = Class79.anInt6212 + -i
                                Class121.anInt1796 = i_12_
                            } else if (d >= 2.356194490192345 && d <= 3.141592653589793) {
                                Class344.anInt4265 = i_12_
                                Class121.anInt1796 = -i + Class348_Sub40_Sub6.anInt9139
                            }
                        } else {
                            Class121.anInt1796 = -i_12_ + Class348_Sub40_Sub6.anInt9139
                            Class344.anInt4265 = -i + Class79.anInt6212
                        }
                    } else {
                        Class121.anInt1796 = -i + Class348_Sub40_Sub6.anInt9139
                        Class344.anInt4265 = -i_12_ + Class79.anInt6212
                    }
                } else {
                    Class121.anInt1796 = i
                    Class344.anInt4265 = Class79.anInt6212 - i_12_
                }
            } else {
                Class121.anInt1796 = Class348_Sub40_Sub6.anInt9139 - i_12_
                Class344.anInt4265 = i
            }
        } else {
            Class344.anInt4265 = i
            Class121.anInt1796 = i_12_
        }
        Class344.anInt4265 = Class344.anInt4265 and Class348_Sub40_Sub6.anInt6325
        Class121.anInt1796 = Class121.anInt1796 and Class348_Sub40_Sub37.anInt6076
    }
}
