package world.gregs.voidps.tools.icon

import kotlin.math.pow

/* Class348_Sub40_Sub8 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub8 : Class348_Sub40(0, true) {
    var anInt9149: Int = 1638
    var anInt9150: Int = 4
    private var aByteArray9152 = ByteArray(512)
    var anInt9156: Int = 0
    var anInt9158: Int = 4
    private var aShortArray9159: ShortArray? = null
    var aBoolean9160: Boolean = true
    private var aShortArray9162: ShortArray? = null
    var anInt9164: Int = 4
    override fun method3049(packet: Packet?, i: Int, i_0_: Int) {
        var i_1_ = i
        while_151_@ do {
            while_150_@ do {
                while_149_@ do {
                    while_148_@ do {
                        while_147_@ do {
                            do {
                                if (i_1_ == 0) {
                                    this.aBoolean9160 = packet!!.readUnsignedByte(i_0_ xor 0x79d8) == 1
                                    break@while_151_
                                } else if (i_1_ != 1) {
                                    if (i_1_ != 2) {
                                        if (i_1_ != 3) {
                                            if (i_1_ != 4) {
                                                if (i_1_ != 5) {
                                                    if (i_1_ == 6) break@while_150_
                                                    break@while_151_
                                                }
                                            } else break@while_148_
                                            break@while_149_
                                        }
                                    } else break
                                    break@while_147_
                                }
                                this.anInt9150 = packet!!.readUnsignedByte(i_0_ xor 0x79d8)
                                break@while_151_
                            } while (false)
                            this.anInt9149 = packet!!.readShort(13638)
                            if (this.anInt9149 < 0) {
                                aShortArray9159 = ShortArray((this.anInt9150))
                                i_1_ = 0
                                while ((this.anInt9150 > i_1_)) {
                                    aShortArray9159!![i_1_] = packet.readShort(13638).toShort()
                                    i_1_++
                                }
                            }
                            break@while_151_
                        } while (false)
                        this.anInt9164 = packet!!.readUnsignedByte(255)
                        this.anInt9158 = this.anInt9164
                        break@while_151_
                    } while (false)
                    this.anInt9156 = packet!!.readUnsignedByte(255)
                    break@while_151_
                } while (false)
                this.anInt9158 = packet!!.readUnsignedByte(255)
                break@while_151_
            } while (false)
            this.anInt9164 = packet!!.readUnsignedByte(255)
        } while (false)
        if (i_0_ != 31015) method3070(20, 127, -38, 124, -110, true, 16)
        anInt9153++
    }

    override fun method3042(i: Int, i_2_: Int): IntArray {
        anInt9154++
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (this.aClass191_7032!!.aBoolean2570) method3069(i, `is`!!, 98.toByte())
        return `is`!!
    }

    override fun method3044(i: Int) {
        aByteArray9152 = Toolkit.Companion.method3664(this.anInt9156, 95)!!
        anInt9148++
        method3067((-98).toByte())
        var i_3_ = this.anInt9150 + -1
        if (i < 108) aClass238_9165 = null
        while ( /**/i_3_ >= 1) {
            val i_4_ = aShortArray9159!![i_3_]
            if (i_4_ > 8) break
            if (i_4_ < -8) break
            this.anInt9150--
            i_3_--
        }
    }

    private fun method3067(i: Byte) {
        val i_5_ = 25 / ((i - -28) / 57)
        if (this.anInt9149 > 0) {
            aShortArray9162 = ShortArray(this.anInt9150)
            aShortArray9159 = ShortArray(this.anInt9150)
            var i_6_ = 0
            while (this.anInt9150 > i_6_) {
                aShortArray9159!![i_6_] = (((this.anInt9149.toFloat() / 4096.0f).toDouble().pow(i_6_.toDouble())) * 4096.0).toInt().toShort()
                aShortArray9162!![i_6_] = 2.0.pow(i_6_.toDouble()).toInt().toShort()
                i_6_++
            }
        } else if (aShortArray9159 != null && (this.anInt9150 == aShortArray9159!!.size)) {
            aShortArray9162 = ShortArray(this.anInt9150)
            var i_7_ = 0
            while ((i_7_ < this.anInt9150)) {
                aShortArray9162!![i_7_] = 2.0.pow(i_7_.toDouble()).toInt().toShort()
                i_7_++
            }
        }
        anInt9151++
    }

    fun method3069(i: Int, `is`: IntArray, i_8_: Byte) {
        anInt9161++
        val i_9_ = (Class239_Sub18.anIntArray6035!![i] * this.anInt9164)
        if (i_8_ > 91) {
            if (this.anInt9150 == 1) {
                val i_39_ = aShortArray9162!![0].toInt() shl 12
                val i_40_ = aShortArray9159!![0].toInt()
                var i_41_ = i_39_ * i_9_ shr 12
                val i_42_ = (i_39_ * this.anInt9158 shr 12)
                val i_43_ = (this.anInt9164 * i_39_ shr 12)
                val i_44_ = i_41_ shr 12
                var i_45_ = 1 + i_44_
                i_41_ = i_41_ and 0xfff
                if (i_45_ >= i_43_) i_45_ = 0
                val i_46_ = aByteArray9152[0xff and i_45_].toInt() and 0xff
                val i_47_ = Class199.anIntArray2631[i_41_]
                val i_48_ = aByteArray9152[0xff and i_44_].toInt() and 0xff
                if (this.aBoolean9160) {
                    var i_52_ = 0
                    while ((Class348_Sub40_Sub6.Companion.anInt9139 > i_52_)) {
                        val i_53_ = (this.anInt9158 * Class318_Sub6.anIntArray6432!![i_52_])
                        var i_54_ = method3070(i_46_, i_41_, i_42_, i_53_ * i_39_ shr 12, i_47_, true, i_48_)
                        i_54_ = i_40_ * i_54_ shr 12
                        `is`[i_52_] = (i_54_ shr 1) + 2048
                        i_52_++
                    }
                } else {
                    for (i_49_ in 0..<Class348_Sub40_Sub6.Companion.anInt9139) {
                        val i_50_ = (this.anInt9158 * Class318_Sub6.anIntArray6432!![i_49_])
                        val i_51_ = method3070(i_46_, i_41_, i_42_, i_39_ * i_50_ shr 12, i_47_, true, i_48_)
                        `is`[i_49_] = i_51_ * i_40_ shr 12
                    }
                }
            } else {
                var i_10_ = aShortArray9159!![0].toInt()
                if (i_10_ > 8 || i_10_ < -8) {
                    val i_11_ = aShortArray9162!![0].toInt() shl 12
                    var i_12_ = i_11_ * i_9_ shr 12
                    val i_13_ = (this.anInt9158 * i_11_ shr 12)
                    val i_14_ = (this.anInt9164 * i_11_ shr 12)
                    val i_15_ = i_12_ shr 12
                    var i_16_ = 1 + i_15_
                    if (i_16_ >= i_14_) i_16_ = 0
                    i_12_ = i_12_ and 0xfff
                    val i_17_ = aByteArray9152[0xff and i_16_].toInt() and 0xff
                    val i_18_ = aByteArray9152[0xff and i_15_].toInt() and 0xff
                    val i_19_ = Class199.anIntArray2631[i_12_]
                    var i_20_ = 0
                    while (Class348_Sub40_Sub6.Companion.anInt9139 > i_20_) {
                        val i_21_ = (Class318_Sub6.anIntArray6432!![i_20_] * this.anInt9158)
                        val i_22_ = method3070(i_17_, i_12_, i_13_, i_11_ * i_21_ shr 12, i_19_, true, i_18_)
                        `is`[i_20_] = i_10_ * i_22_ shr 12
                        i_20_++
                    }
                }
                var i_23_ = 1
                while (this.anInt9150 > i_23_) {
                    i_10_ = aShortArray9159!![i_23_].toInt()
                    if (i_10_ > 8 || i_10_ < -8) {
                        val i_24_ = aShortArray9162!![i_23_].toInt() shl 12
                        var i_25_ = i_24_ * i_9_ shr 12
                        val i_26_ = (this.anInt9158 * i_24_ shr 12)
                        val i_27_ = (this.anInt9164 * i_24_ shr 12)
                        val i_28_ = i_25_ shr 12
                        var i_29_ = 1 + i_28_
                        i_25_ = i_25_ and 0xfff
                        if (i_29_ >= i_27_) i_29_ = 0
                        val i_30_ = aByteArray9152[i_28_ and 0xff].toInt() and 0xff
                        val i_31_ = Class199.anIntArray2631[i_25_]
                        val i_32_ = 0xff and aByteArray9152[i_29_ and 0xff].toInt()
                        if (this.aBoolean9160 && (this.anInt9150 - 1 == i_23_)) {
                            var i_33_ = 0
                            while (Class348_Sub40_Sub6.Companion.anInt9139 > i_33_) {
                                val i_34_ = (Class318_Sub6.anIntArray6432!![i_33_] * (this.anInt9158))
                                var i_35_ = method3070(i_32_, i_25_, i_26_, i_34_ * i_24_ shr 12, i_31_, true, i_30_)
                                i_35_ = `is`[i_33_] - -(i_35_ * i_10_ shr 12)
                                `is`[i_33_] = (i_35_ shr 1) + 2048
                                i_33_++
                            }
                        } else {
                            var i_36_ = 0
                            while ((i_36_ < Class348_Sub40_Sub6.Companion.anInt9139)) {
                                val i_37_ = (this.anInt9158 * Class318_Sub6.anIntArray6432!![i_36_])
                                val i_38_ = method3070(i_32_, i_25_, i_26_, i_37_ * i_24_ shr 12, i_31_, true, i_30_)
                                `is`[i_36_] += i_10_ * i_38_ shr 12
                                i_36_++
                            }
                        }
                    }
                    i_23_++
                }
            }
        }
    }

    private fun method3070(i: Int, i_55_: Int, i_56_: Int, i_57_: Int, i_58_: Int, bool: Boolean, i_59_: Int): Int {
        var i_57_ = i_57_
        anInt9155++
        var i_60_ = i_57_ shr 12
        var i_61_ = i_60_ - -1
        i_60_ = i_60_ and 0xff
        i_57_ = i_57_ and 0xfff
        if (i_56_ <= i_61_) i_61_ = 0
        i_61_ = i_61_ and 0xff
        val i_62_ = -4096 + i_57_
        val i_63_ = i_55_ + -4096
        var i_64_ = 0x3 and aByteArray9152[i_59_ + i_60_].toInt()
        val i_65_ = Class199.anIntArray2631[i_57_]
        var i_66_: Int
        if (i_64_ > 1) i_66_ = if (i_64_ == 2) i_57_ - i_55_ else -i_55_ + -i_57_
        else i_66_ = if (i_64_ != 0) i_55_ - i_57_ else i_57_ + i_55_
        i_64_ = 0x3 and aByteArray9152[i_59_ + i_61_].toInt()
        var i_67_: Int
        if (i_64_ <= 1) i_67_ = if (i_64_ == 0) i_62_ + i_55_ else i_55_ + -i_62_
        else i_67_ = if (i_64_ != 2) -i_55_ + -i_62_ else -i_55_ + i_62_
        i_64_ = aByteArray9152[i + i_60_].toInt() and 0x3
        if (bool != true) return -82
        val i_68_ = i_66_ - -(i_65_ * (i_67_ + -i_66_) shr 12)
        if (i_64_ > 1) i_66_ = if (i_64_ == 2) -i_63_ + i_57_ else -i_57_ - i_63_
        else i_66_ = if (i_64_ != 0) -i_57_ + i_63_ else i_57_ - -i_63_
        i_64_ = 0x3 and aByteArray9152[i + i_61_].toInt()
        if (i_64_ > 1) i_67_ = if (i_64_ == 2) -i_63_ + i_62_ else -i_62_ - i_63_
        else i_67_ = if (i_64_ == 0) i_62_ - -i_63_ else i_63_ + -i_62_
        val i_69_ = ((-i_66_ + i_67_) * i_65_ shr 12) + i_66_
        return i_68_ - -(i_58_ * (-i_68_ + i_69_) shr 12)
    }

    companion object {
        var anInt9148: Int = 0
        var anInt9151: Int = 0
        var anInt9153: Int = 0
        var anInt9154: Int = 0
        var anInt9155: Int = 0
        var anInt9161: Int = 0
        var aClass238_9165: Class238? = null
    }
}
