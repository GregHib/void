package world.gregs.voidps.tools.render

import java.util.Random
import kotlin.math.max
import kotlin.math.min

/* Class348_Sub40_Sub23 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub23 : Class348_Sub40(0, true) {
    private var anInt9306 = 0
    private var anInt9310 = 1024
    private var anInt9311 = 1024
    private var anInt9312 = 0
    private var anInt9314 = 1024
    private var anInt9317 = 1024
    private var anInt9318 = 0
    private var anInt9320 = 2048
    private var anInt9322 = 409
    private var anInt9323 = 819

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9318 = packet!!.readUnsignedByte()
            1 -> anInt9317 = packet!!.readUnsignedShort()
            2 -> anInt9320 = packet!!.readUnsignedShort()
            3 -> anInt9322 = packet!!.readUnsignedShort()
            4 -> anInt9323 = packet!!.readUnsignedShort()
            5 -> anInt9311 = packet!!.readUnsignedShort()
            6 -> anInt9312 = packet!!.readUnsignedByte()
            7 -> anInt9314 = packet!!.readUnsignedShort()
            8 -> anInt9310 = packet!!.readUnsignedShort()
        }
    }

    private fun method3112(i_2_: Int, `is`: Array<IntArray?>, random: Random, i_3_: Int, i_4_: Int, i_5_: Int) {
        var i_3_ = i_3_
        val width = Class348_Sub40_Sub6.anInt9139
        val mask = Class348_Sub40_Sub37.anInt6076
        val i_6_ = if (anInt9310 <= 0) 4096 else 4096 - Mesh.method1097(anInt9310, random)
        val i_8_ = anInt9306 * anInt9314 shr 12
        val i_9_ = anInt9306 - (if (i_8_ > 0) Mesh.method1097(i_8_, random) else 0)
        if (width <= i_3_) i_3_ -= width
        if (i_9_ <= 0) {
            if (width < i_3_ + i_5_) {
                val i_10_ = width - i_3_
                for (i_11_ in 0..<i_2_) {
                    val is_12_ = `is`[i_11_ + i_4_]!!
                    method1579(is_12_, i_3_, i_10_, i_6_)
                    method1579(is_12_, 0, i_5_ - i_10_, i_6_)
                }
            } else {
                for (i_13_ in 0..<i_2_) method1579(`is`[i_13_ + i_4_]!!, i_3_, i_5_, i_6_)
            }
        } else if (i_2_ > 0 && i_5_ > 0) {
            val i_14_ = i_5_ / 2
            val i_15_ = i_2_ / 2
            val i_16_ = min(i_14_, i_9_)
            val i_17_ = min(i_15_, i_9_)
            val i_18_ = i_3_ + i_16_
            val i_19_ = i_5_ - i_16_ * 2
            for (i_20_ in 0..<i_2_) {
                val is_21_ = `is`[i_20_ + i_4_]!!
                if (i_20_ < i_17_) {
                    val i_22_ = i_6_ * i_20_ / i_17_
                    if (anInt9312 == 0) {
                        for (i_23_ in 0..<i_16_) {
                            val i_24_ = i_6_ * i_23_ / i_16_
                            val v = i_22_ * i_24_ shr 12
                            is_21_[(i_3_ + i_5_ - i_23_ - 1) and mask] = v
                            is_21_[(i_23_ + i_3_) and mask] = v
                        }
                    } else {
                        for (i_25_ in 0..<i_16_) {
                            val i_26_ = i_6_ * i_25_ / i_16_
                            val v = min(i_26_, i_22_)
                            is_21_[(i_3_ + i_5_ - i_25_ - 1) and mask] = v
                            is_21_[(i_25_ + i_3_) and mask] = v
                        }
                    }
                    if (width >= i_19_ + i_18_) {
                        method1579(is_21_, i_18_, i_19_, i_22_)
                    } else {
                        val i_27_ = width - i_18_
                        method1579(is_21_, i_18_, i_27_, i_22_)
                        method1579(is_21_, 0, i_19_ - i_27_, i_22_)
                    }
                } else {
                    val i_28_ = i_2_ - i_20_ - 1
                    if (i_17_ > i_28_) {
                        val i_29_ = i_28_ * i_6_ / i_17_
                        if (anInt9312 == 0) {
                            for (i_30_ in 0..<i_16_) {
                                val i_31_ = i_6_ * i_30_ / i_16_
                                val v = i_31_ * i_29_ shr 12
                                is_21_[(i_5_ + i_3_ - 1 - i_30_) and mask] = v
                                is_21_[(i_30_ + i_3_) and mask] = v
                            }
                        } else {
                            for (i_32_ in 0..<i_16_) {
                                val i_33_ = i_32_ * i_6_ / i_16_
                                val v = min(i_29_, i_33_)
                                is_21_[(i_5_ + i_3_ - 1 - i_32_) and mask] = v
                                is_21_[(i_3_ + i_32_) and mask] = v
                            }
                        }
                        if (i_19_ + i_18_ <= width) {
                            method1579(is_21_, i_18_, i_19_, i_29_)
                        } else {
                            val i_34_ = width - i_18_
                            method1579(is_21_, i_18_, i_34_, i_29_)
                            method1579(is_21_, 0, i_19_ - i_34_, i_29_)
                        }
                    } else {
                        for (i_35_ in 0..<i_16_) {
                            val v = i_6_ * i_35_ / i_16_
                            is_21_[(i_3_ + i_5_ - 1 - i_35_) and mask] = v
                            is_21_[(i_35_ + i_3_) and mask] = v
                        }
                        if (width >= i_18_ + i_19_) {
                            method1579(is_21_, i_18_, i_19_, i_6_)
                        } else {
                            val i_36_ = width - i_18_
                            method1579(is_21_, i_18_, i_36_, i_6_)
                            method1579(is_21_, 0, i_19_ - i_36_, i_6_)
                        }
                    }
                }
            }
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val width = Class348_Sub40_Sub6.anInt9139
            val height = Class348_Sub40_Sub33.anInt6212
            val is_42_ = this.aClass191_7032!!.method1427()!!
            var i_43_ = 0
            var i_44_ = 0
            var i_45_ = 0
            var i_46_ = 0
            var i_47_ = 0
            var bool = true
            var bool_48_ = true
            var i_49_ = 0
            var i_50_ = 0
            val i_51_ = anInt9317 * width shr 12
            val i_52_ = width * anInt9320 shr 12
            val i_53_ = anInt9322 * height shr 12
            val i_54_ = height * anInt9323 shr 12
            if (i_54_ <= 1) return is_42_[i]
            anInt9306 = width / 8 * anInt9311 shr 12
            val i_55_ = 1 + width / i_51_
            var is_56_ = Array(i_55_) { IntArray(3) }
            var is_57_ = Array(i_55_) { IntArray(3) }
            val random = Random(anInt9318.toLong())
            while (true) {
                var i_58_ = i_51_ + Mesh.method1097(i_52_ - i_51_, random)
                var i_59_ = Mesh.method1097(i_54_ - i_53_, random) + i_53_
                var i_60_ = i_46_ + i_58_
                if (i_60_ > width) {
                    i_60_ = width
                    i_58_ = width - i_46_
                }
                val i_61_: Int
                if (bool_48_) {
                    i_61_ = 0
                } else {
                    var i_62_ = i_47_
                    val is_63_ = is_57_[i_47_]
                    var i_64_ = 0
                    var i_65_ = i_43_ + i_60_
                    if (i_65_ < 0) i_65_ += width
                    if (i_65_ > width) i_65_ -= width
                    while (true) {
                        val is_66_ = is_57_[i_62_]
                        if (i_65_ >= is_66_[0] && is_66_[1] >= i_65_) break
                        if (i_49_ <= ++i_62_) i_62_ = 0
                        i_64_++
                    }
                    var max61 = is_63_[2]
                    if (i_62_ != i_47_) {
                        var i_67_ = i_46_ + i_43_
                        if (i_67_ < 0) i_67_ += width
                        if (width < i_67_) i_67_ -= width
                        for (i_68_ in 1..i_64_) {
                            val is_69_ = is_57_[(i_68_ + i_47_) % i_49_]
                            max61 = max(max61, is_69_[2])
                        }
                        for (i_70_ in 0..i_64_) {
                            val is_71_ = is_57_[(i_47_ + i_70_) % i_49_]
                            val i_72_ = is_71_[2]
                            if (max61 != i_72_) {
                                val i_73_ = is_71_[0]
                                val i_74_ = is_71_[1]
                                val i_75_: Int
                                val i_76_: Int
                                if (i_67_ >= i_65_) {
                                    if (i_73_ == 0) {
                                        i_75_ = 0
                                        i_76_ = min(i_65_, i_74_)
                                    } else {
                                        i_75_ = max(i_67_, i_73_)
                                        i_76_ = width
                                    }
                                } else {
                                    i_75_ = max(i_67_, i_73_)
                                    i_76_ = min(i_65_, i_74_)
                                }
                                method3112(max61 - i_72_, is_42_, random, i_45_ + i_75_, i_72_, i_76_ - i_75_)
                            }
                        }
                    }
                    i_61_ = max61
                    i_47_ = i_62_
                }
                if (height >= i_59_ + i_61_) {
                    bool = false
                } else {
                    i_59_ = height - i_61_
                }
                if (i_60_ == width) {
                    method3112(i_59_, is_42_, random, i_46_ + i_44_, i_61_, i_58_)
                    if (bool) break
                    bool = true
                    val is_78_ = is_56_[i_50_++]
                    is_78_[1] = i_60_
                    is_78_[2] = i_59_ + i_61_
                    is_78_[0] = i_46_
                    val is_79_ = is_57_
                    is_57_ = is_56_
                    is_56_ = is_79_
                    i_49_ = i_50_
                    i_45_ = i_44_
                    i_50_ = 0
                    i_44_ = Mesh.method1097(width, random)
                    i_43_ = i_44_ - i_45_
                    i_46_ = 0
                    var i_80_ = i_43_
                    if (i_80_ < 0) i_80_ += width
                    i_47_ = 0
                    if (i_80_ > width) i_80_ -= width
                    bool_48_ = false
                    while (true) {
                        val is_81_ = is_57_[i_47_]
                        if (i_80_ >= is_81_[0] && i_80_ <= is_81_[1]) break
                        if (i_49_ <= ++i_47_) i_47_ = 0
                    }
                } else {
                    val is_77_ = is_56_[i_50_++]
                    is_77_[1] = i_60_
                    is_77_[0] = i_46_
                    is_77_[2] = i_59_ + i_61_
                    method3112(i_59_, is_42_, random, i_46_ + i_44_, i_61_, i_58_)
                    i_46_ = i_60_
                }
            }
        }
        return `is`
    }

    companion object {
        /** Class214.method1579 */
        fun method1579(`is`: IntArray, i: Int, i_16_: Int, i_17_: Int) {
            var i = i
            var i_16_ = i_16_
            i_16_ = i + i_16_ - 7
            while (i < i_16_) {
                `is`[i++] = i_17_
                `is`[i++] = i_17_
                `is`[i++] = i_17_
                `is`[i++] = i_17_
                `is`[i++] = i_17_
                `is`[i++] = i_17_
                `is`[i++] = i_17_
                `is`[i++] = i_17_
            }
            i_16_ += 7
            while (i < i_16_) `is`[i++] = i_17_
        }
    }
}
