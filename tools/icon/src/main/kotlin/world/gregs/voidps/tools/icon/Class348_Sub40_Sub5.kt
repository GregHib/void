package world.gregs.voidps.tools.icon

import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

/* Class348_Sub40_Sub5 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub5 : Class348_Sub40(0, true) {
    private var aShortArray9116 = ShortArray(512)
    private var anInt9117 = 5
    private var anInt9118 = 1
    private var aByteArray9119 = ByteArray(512)
    private var anInt9122 = 0
    private var anInt9124 = 2
    private var anInt9125 = 2048
    private var anInt9129 = 5

    private fun method3061(bool: Boolean) {
        anInt9120++
        if (bool == true) {
            val random = Random(anInt9122.toLong())
            aShortArray9116 = ShortArray(512)
            if (anInt9125 > 0) {
                for (i in 0..511) aShortArray9116[i] = Mesh.Companion.method1097(81.toByte(), anInt9125, random).toShort()
            }
        }
    }

    override fun method3044(i: Int) {
        if (i > 108) {
            aByteArray9119 = Toolkit.Companion.method3664(anInt9122, 124)!!
            anInt9126++
            method3061(true)
        }
    }

    override fun method3042(i: Int, i_0_: Int): IntArray {
        anInt9123++
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (i_0_ != 255) method3044(-57)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_1_ = anInt9117 * Class239_Sub18.anIntArray6035!![i] + 2048
            val i_2_ = i_1_ shr 12
            val i_3_ = 1 + i_2_
            var i_4_ = 0
            while_140_@ while ( /**/Class348_Sub40_Sub6.Companion.anInt9139 > i_4_) {
                Class135_Sub1.anInt4715 = 2147483647
                Class147.anInt2023 = Class135_Sub1.anInt4715
                Class215.anInt2835 = Class147.anInt2023
                Class348_Sub42_Sub20.anInt9715 = Class215.anInt2835
                val i_5_ = 2048 - -(Class318_Sub6.anIntArray6432!![i_4_] * anInt9129)
                val i_6_ = i_5_ shr 12
                val i_7_ = i_6_ + 1
                for (i_8_ in i_2_ + -1..i_3_) {
                    val i_9_ = 0xff and aByteArray9119[0xff and (if (i_8_ >= anInt9117) i_8_ + -anInt9117 else i_8_)].toInt()
                    var i_10_ = -1 + i_6_
                    while (i_7_ >= i_10_) {
                        var i_11_ = 2 * (0xff and aByteArray9119[((if (anInt9129 > i_10_) i_10_ else -anInt9129 + i_10_) + i_9_) and 0xff].toInt())
                        var i_12_ = (-(i_10_ shl 12) - (aShortArray9116[i_11_++] - i_5_))
                        var i_13_ = (-(i_8_ shl 12) + (-aShortArray9116[i_11_] + i_1_))
                        val i_14_ = anInt9118
                        var i_15_: Int
                        while_136_@ do {
                            while_135_@ do {
                                while_134_@ do {
                                    while_133_@ do {
                                        do {
                                            if (i_14_ == 1) {
                                                i_15_ = ((i_13_ * i_13_ + i_12_ * i_12_) shr 12)
                                                break@while_136_
                                            } else if (i_14_ != 3) {
                                                if (i_14_ != 4) {
                                                    if (i_14_ != 5) {
                                                        if (i_14_ == 2) break@while_134_
                                                        break@while_135_
                                                    }
                                                } else break
                                                break@while_133_
                                            }
                                            i_12_ = if (i_12_ < 0) -i_12_ else i_12_
                                            i_13_ = (if (i_13_ < 0) -i_13_ else i_13_)
                                            i_15_ = (max(i_12_, i_13_))
                                            break@while_136_
                                        } while (false)
                                        i_12_ = ((sqrt(((if (i_12_ < 0) -i_12_ else i_12_).toFloat() / 4096.0f).toDouble())) * 4096.0).toInt()
                                        i_13_ = (4096.0 * (sqrt(((if (i_13_ < 0) -i_13_ else i_13_).toFloat() / 4096.0f).toDouble()))).toInt()
                                        i_15_ = i_13_ + i_12_
                                        i_15_ = i_15_ * i_15_ shr 12
                                        break@while_136_
                                    } while (false)
                                    i_13_ *= i_13_
                                    i_12_ *= i_12_
                                    i_15_ = (4096.0 * (sqrt(sqrt(((i_12_ + i_13_).toFloat() / 1.6777216E7f).toDouble())))).toInt()
                                    break@while_136_
                                } while (false)
                                i_15_ = ((if (i_12_ < 0) -i_12_ else i_12_) - -(if (i_13_ >= 0) i_13_ else -i_13_))
                                break@while_136_
                            } while (false)
                            i_15_ = (4096.0 * (sqrt(((i_12_ * i_12_ - -(i_13_ * i_13_)).toFloat() / 1.6777216E7f).toDouble()))).toInt()
                        } while (false)
                        if (Class348_Sub42_Sub20.anInt9715 <= i_15_) {
                            if (i_15_ >= Class215.anInt2835) {
                                if (i_15_ < Class147.anInt2023) {
                                    Class135_Sub1.anInt4715 = Class147.anInt2023
                                    Class147.anInt2023 = i_15_
                                } else if (i_15_ < Class135_Sub1.anInt4715) Class135_Sub1.anInt4715 = i_15_
                            } else {
                                Class135_Sub1.anInt4715 = Class147.anInt2023
                                Class147.anInt2023 = Class215.anInt2835
                                Class215.anInt2835 = i_15_
                            }
                        } else {
                            Class135_Sub1.anInt4715 = Class147.anInt2023
                            Class147.anInt2023 = Class215.anInt2835
                            Class215.anInt2835 = Class348_Sub42_Sub20.anInt9715
                            Class348_Sub42_Sub20.anInt9715 = i_15_
                        }
                        i_10_++
                    }
                }
                val i_16_ = anInt9124
                while_138_@ do {
                    while_137_@ do {
                        do {
                            if (i_16_ == 0) {
                                `is`!![i_4_] = Class348_Sub42_Sub20.anInt9715
                                i_4_++
                                continue@while_140_
                            } else if (i_16_ != 1) {
                                if (i_16_ != 3) {
                                    if (i_16_ != 4) {
                                        if (i_16_ == 2) break@while_138_
                                        i_4_++
                                        continue@while_140_
                                    }
                                } else break
                                break@while_137_
                            }
                            `is`!![i_4_] = Class215.anInt2835
                            i_4_++
                            continue@while_140_
                        } while (false)
                        `is`!![i_4_] = Class147.anInt2023
                        i_4_++
                        continue@while_140_
                    } while (false)
                    `is`!![i_4_] = Class135_Sub1.anInt4715
                    i_4_++
                    continue@while_140_
                } while (false)
                `is`!![i_4_] = Class215.anInt2835 + -Class348_Sub42_Sub20.anInt9715
                i_4_++
            }
        }
        return `is`!!
    }

    override fun method3049(packet: Packet?, i: Int, i_17_: Int) {
        if (i_17_ == 31015) {
            val i_18_ = i
            while_145_@ do {
                while_144_@ do {
                    while_143_@ do {
                        while_142_@ do {
                            while_141_@ do {
                                do {
                                    if (i_18_ == 0) {
                                        anInt9117 = packet!!.readUnsignedByte(i_17_ xor 0x79d8)
                                        anInt9129 = anInt9117
                                        break@while_145_
                                    } else if (i_18_ != 1) {
                                        if (i_18_ != 2) {
                                            if (i_18_ != 3) {
                                                if (i_18_ != 4) {
                                                    if (i_18_ != 5) {
                                                        if (i_18_ == 6) break@while_144_
                                                        break@while_145_
                                                    }
                                                } else break@while_142_
                                                break@while_143_
                                            }
                                        } else break
                                        break@while_141_
                                    }
                                    anInt9122 = packet!!.readUnsignedByte(i_17_ + -30760)
                                    break@while_145_
                                } while (false)
                                anInt9125 = packet!!.readUnsignedShort(842397944)
                                break@while_145_
                            } while (false)
                            anInt9124 = packet!!.readUnsignedByte(i_17_ xor 0x79d8)
                            break@while_145_
                        } while (false)
                        anInt9118 = packet!!.readUnsignedByte(i_17_ xor 0x79d8)
                        break@while_145_
                    } while (false)
                    anInt9129 = packet!!.readUnsignedByte(255)
                    break@while_145_
                } while (false)
                anInt9117 = packet!!.readUnsignedByte(255)
            } while (false)
            anInt9127++
        }
    }

    companion object {
        var anInt9120: Int = 0
        var anInt9123: Int = 0
        var anInt9126: Int = 0
        var anInt9127: Int = 0
    }
}
