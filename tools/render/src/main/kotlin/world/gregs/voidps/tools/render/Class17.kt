package world.gregs.voidps.tools.render

import kotlin.math.min

/*
 * Software rasteriser for drawing vector shapes (Class50) into an int[][] (row-major, [y][x])
 * target, used by texture op 29 (Class348_Sub40_Sub39).
 */

internal object Class17 {
    var anIntArrayArray255: Array<IntArray?>? = null

    /** Class369.anInt4960 */
    var anInt4960: Int = 0

    /** Class38.anInt513 */
    var anInt513: Int = 100

    /** Class113.anInt1745 */
    var anInt1745: Int = 100

    /** Class132.anInt1910 */
    var anInt1910: Int = 0

    /** Class348_Sub49_Sub2.anIntArray9757 */
    var anIntArray9757: IntArray? = null

    private fun row(y: Int): IntArray = anIntArrayArray255!![y]!!

    /** Class12.method224 */
    fun method224(`is`: Array<IntArray?>?) {
        anIntArrayArray255 = `is`
    }

    /** Class348_Sub27.method3000 */
    fun method3000(i: Int, i_0_: Int, i_1_: Int, i_2_: Int) {
        anInt4960 = i_2_
        anInt513 = i_1_
        anInt1745 = i
        anInt1910 = i_0_
    }

    /** Class85.method831 */
    fun method831(i: Int, i_25_: Int, i_26_: Int): Int {
        if (i_26_ > i_25_) return i_26_
        return min(i_25_, i)
    }

    /** Class135_Sub2.method1156 */
    fun method1156(i_3_: Int, `is`: IntArray, i_4_: Int, i_5_: Int) {
        var i_4 = i_4_ - 1
        val i_3 = i_3_ - 1
        val i_6_ = i_3 - 7
        while (i_6_ > i_4) {
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
            `is`[++i_4] = i_5_
        }
        while (i_3 > i_4) `is`[++i_4] = i_5_
    }

    /** Class117.method1070 */
    fun method1070(i_6_: Int) {
        if (anIntArray9757 == null || anIntArray9757!!.size < i_6_) anIntArray9757 = IntArray(i_6_)
    }

    /** Class12.method223 */
    fun method223(i: Int, i_0_: Int, i_1_: Int, i_2_: Int) {
        if (i_2_ > i) method1156(i_2_, row(i_1_), i, i_0_)
        else method1156(i, row(i_1_), i_2_, i_0_)
    }

    /** Class332.method2641 */
    fun method2641(i: Int, i_0_: Int, i_2_: Int, i_3_: Int) {
        if (i < i_0_) {
            for (i_4_ in i..<i_0_) row(i_4_)[i_2_] = i_3_
        } else {
            for (i_5_ in i_0_..<i) row(i_5_)[i_2_] = i_3_
        }
    }

    /* ---------------- Ellipse (Class50_Sub1) ---------------- */

    /** Class299_Sub2.method2267 */
    fun method2267(i_0_: Int, i_1_: Int, i_2_: Int, i_3_: Int, i_4_: Int, i_5_: Int, i_6_: Int) {
        if (i_6_ == i_0_) method2441(i_2_, i_6_, i_4_, i_5_, i_3_, i_1_)
        else if (anInt4960 > -i_6_ + i_1_ || i_6_ + i_1_ > anInt1745 || -i_0_ + i_5_ < anInt1910 || i_0_ + i_5_ > anInt513) method3041(i_6_, i_1_, i_5_, i_0_, i_3_, i_2_, i_4_)
        else method3012(i_0_, i_3_, i_6_, i_2_, i_1_, i_5_, i_4_)
    }

    /** Npc.method2441 */
    fun method2441(i: Int, i_0_: Int, i_1_: Int, i_3_: Int, i_4_: Int, i_5_: Int) {
        if (i_5_ - i_0_ >= anInt4960 && anInt1745 >= i_0_ + i_5_ && -i_0_ + i_3_ >= anInt1910 && i_3_ + i_0_ <= anInt513) method2255(i_0_, i_5_, i_1_, i_3_, i_4_, i)
        else method1496(i, i_3_, i_4_, i_0_, i_1_, i_5_)
    }

    /** Class299.method2255 */
    fun method2255(i: Int, i_3_: Int, i_5_: Int, i_6_: Int, i_7_: Int, i_8_: Int) {
        method1070(i)
        val table = anIntArray9757!!
        var i_9_ = 0
        var i_10_ = -i_5_ + i
        if (i_10_ < 0) i_10_ = 0
        var i_11_ = i
        var i_12_ = -i
        var i_13_ = i_10_
        var i_14_ = -i_10_
        var i_15_ = -1
        var i_16_ = -1
        val `is` = row(i_6_)
        val i_17_ = i_3_ + -i_10_
        val i_18_ = i_10_ + i_3_
        method1156(i_17_, `is`, -i + i_3_, i_8_)
        method1156(i_18_, `is`, i_17_, i_7_)
        method1156(i + i_3_, `is`, i_18_, i_8_)
        while (i_11_ > i_9_) {
            i_16_ += 2
            i_15_ += 2
            i_14_ += i_16_
            i_12_ += i_15_
            if (i_14_ >= 0 && i_13_ >= 1) {
                table[i_13_] = i_9_
                i_13_--
                i_14_ -= i_13_ shl 1
            }
            i_9_++
            if (i_12_ >= 0) {
                i_11_--
                i_12_ -= i_11_ shl 1
                if (i_10_ <= i_11_) {
                    val is_19_ = row(i_11_ + i_6_)
                    val is_20_ = row(i_6_ + -i_11_)
                    val i_21_ = i_3_ - -i_9_
                    val i_22_ = -i_9_ + i_3_
                    method1156(i_21_, is_19_, i_22_, i_8_)
                    method1156(i_21_, is_20_, i_22_, i_8_)
                } else {
                    val is_23_ = row(i_6_ - -i_11_)
                    val is_24_ = row(-i_11_ + i_6_)
                    val i_25_ = table[i_11_]
                    val i_26_ = i_9_ + i_3_
                    val i_27_ = i_3_ - i_9_
                    val i_28_ = i_3_ - -i_25_
                    val i_29_ = -i_25_ + i_3_
                    method1156(i_29_, is_23_, i_27_, i_8_)
                    method1156(i_28_, is_23_, i_29_, i_7_)
                    method1156(i_26_, is_23_, i_28_, i_8_)
                    method1156(i_29_, is_24_, i_27_, i_8_)
                    method1156(i_28_, is_24_, i_29_, i_7_)
                    method1156(i_26_, is_24_, i_28_, i_8_)
                }
            }
            val is_30_ = row(i_6_ + i_9_)
            val is_31_ = row(-i_9_ + i_6_)
            val i_32_ = i_3_ - -i_11_
            val i_33_ = i_3_ - i_11_
            if (i_10_ > i_9_) {
                val i_34_ = (if (i_9_ <= i_13_) i_13_ else table[i_9_])
                val i_35_ = i_34_ + i_3_
                val i_36_ = i_3_ + -i_34_
                method1156(i_36_, is_30_, i_33_, i_8_)
                method1156(i_35_, is_30_, i_36_, i_7_)
                method1156(i_32_, is_30_, i_35_, i_8_)
                method1156(i_36_, is_31_, i_33_, i_8_)
                method1156(i_35_, is_31_, i_36_, i_7_)
                method1156(i_32_, is_31_, i_35_, i_8_)
            } else {
                method1156(i_32_, is_30_, i_33_, i_8_)
                method1156(i_32_, is_31_, i_33_, i_8_)
            }
        }
    }

    /** Class205.method1496 (dummy i_3_ == 2 dropped) */
    fun method1496(i: Int, i_0_: Int, i_1_: Int, i_2_: Int, i_4_: Int, i_5_: Int) {
        method1070(i_2_)
        val table = anIntArray9757!!
        var i_6_ = 0
        var i_7_ = -i_4_ + i_2_
        if (i_7_ < 0) i_7_ = 0
        var i_8_ = i_2_
        var i_9_ = -i_2_
        var i_10_ = i_7_
        var i_11_ = -i_7_
        var i_12_ = -1
        if (i_0_ >= anInt1910 && i_0_ <= anInt513) {
            val `is` = row(i_0_)
            val i_13_ = method831(anInt1745, -i_2_ + i_5_, anInt4960)
            val i_14_ = method831(anInt1745, i_5_ - -i_2_, anInt4960)
            val i_15_ = method831(anInt1745, i_5_ - i_7_, anInt4960)
            val i_16_ = method831(anInt1745, i_5_ - -i_7_, anInt4960)
            method1156(i_15_, `is`, i_13_, i)
            method1156(i_16_, `is`, i_15_, i_1_)
            method1156(i_14_, `is`, i_16_, i)
        }
        var i_17_ = -1
        while (i_6_ < i_8_) {
            i_12_ += 2
            i_17_ += 2
            i_11_ += i_17_
            i_9_ += i_12_
            if (i_11_ >= 0 && i_10_ >= 1) {
                i_10_--
                table[i_10_] = i_6_
                i_11_ -= i_10_ shl 1
            }
            i_6_++
            if (i_9_ >= 0) {
                i_8_--
                i_9_ -= i_8_ shl 1
                val i_18_ = i_0_ - i_8_
                val i_19_ = i_0_ + i_8_
                if (anInt1910 <= i_19_ && anInt513 >= i_18_) {
                    if (i_8_ >= i_7_) {
                        val i_20_ = method831(anInt1745, i_6_ + i_5_, anInt4960)
                        val i_21_ = method831(anInt1745, i_5_ - i_6_, anInt4960)
                        if (i_19_ <= anInt513) method1156(i_20_, row(i_19_), i_21_, i)
                        if (anInt1910 <= i_18_) method1156(i_20_, row(i_18_), i_21_, i)
                    } else {
                        val i_22_ = table[i_8_]
                        val i_23_ = method831(anInt1745, i_6_ + i_5_, anInt4960)
                        val i_24_ = method831(anInt1745, -i_6_ + i_5_, anInt4960)
                        val i_25_ = method831(anInt1745, i_22_ + i_5_, anInt4960)
                        val i_26_ = method831(anInt1745, i_5_ - i_22_, anInt4960)
                        if (anInt513 >= i_19_) {
                            val `is` = row(i_19_)
                            method1156(i_26_, `is`, i_24_, i)
                            method1156(i_25_, `is`, i_26_, i_1_)
                            method1156(i_23_, `is`, i_25_, i)
                        }
                        if (anInt1910 <= i_18_) {
                            val `is` = row(i_18_)
                            method1156(i_26_, `is`, i_24_, i)
                            method1156(i_25_, `is`, i_26_, i_1_)
                            method1156(i_23_, `is`, i_25_, i)
                        }
                    }
                }
            }
            val i_27_ = -i_6_ + i_0_
            val i_28_ = i_0_ + i_6_
            if (anInt1910 <= i_28_ && i_27_ <= anInt513) {
                var i_29_ = i_8_ + i_5_
                var i_30_ = i_5_ + -i_8_
                if (anInt4960 <= i_29_ && i_30_ <= anInt1745) {
                    i_29_ = method831(anInt1745, i_29_, anInt4960)
                    i_30_ = method831(anInt1745, i_30_, anInt4960)
                    if (i_6_ >= i_7_) {
                        if (anInt513 >= i_28_) method1156(i_29_, row(i_28_), i_30_, i)
                        if (i_27_ >= anInt1910) method1156(i_29_, row(i_27_), i_30_, i)
                    } else {
                        val i_31_ = (if (i_6_ > i_10_) table[i_6_] else i_10_)
                        val i_32_ = method831(anInt1745, i_31_ + i_5_, anInt4960)
                        val i_33_ = method831(anInt1745, -i_31_ + i_5_, anInt4960)
                        if (i_28_ <= anInt513) {
                            val `is` = row(i_28_)
                            method1156(i_33_, `is`, i_30_, i)
                            method1156(i_32_, `is`, i_33_, i_1_)
                            method1156(i_29_, `is`, i_32_, i)
                        }
                        if (anInt1910 <= i_27_) {
                            val `is` = row(i_27_)
                            method1156(i_33_, `is`, i_30_, i)
                            method1156(i_32_, `is`, i_33_, i_1_)
                            method1156(i_29_, `is`, i_32_, i)
                        }
                    }
                }
            }
        }
    }

    /** Class348_Sub40.method3041 (dummy i_7_ == -1 inlined) */
    fun method3041(i: Int, i_5_: Int, i_6_: Int, i_8_: Int, i_9_: Int, i_10_: Int, i_11_: Int) {
        val i_7_ = -1
        var i_12_ = 0
        var i_13_ = i_8_
        var i_14_ = 0
        val i_15_ = -i_11_ + i
        val i_16_ = i_8_ - i_11_
        val i_17_ = i * i
        val i_18_ = i_8_ * i_8_
        val i_19_ = i_15_ * i_15_
        val i_20_ = i_16_ * i_16_
        val i_21_ = i_18_ shl 1
        val i_22_ = i_17_ shl 1
        val i_23_ = i_20_ shl 1
        val i_24_ = i_19_ shl 1
        val i_25_ = i_8_ shl 1
        val i_26_ = i_16_ shl 1
        var i_27_ = i_21_ + i_17_ * (-i_25_ + 1)
        var i_28_ = i_18_ + -(i_22_ * (-1 + i_25_))
        var i_29_ = (1 - i_26_) * i_19_ + i_23_
        var i_30_ = i_20_ + -(i_24_ * (-1 + i_26_))
        val i_31_ = i_17_ shl 2
        val i_32_ = i_18_ shl 2
        val i_33_ = i_19_ shl 2
        val i_34_ = i_20_ shl 2
        var i_35_ = 3 * i_21_
        var i_36_ = i_22_ * (-3 + i_25_)
        var i_37_ = 3 * i_23_
        var i_38_ = (i_26_ - 3) * i_24_
        var i_39_ = i_32_
        var i_40_ = i_31_ * (i_7_ + i_8_)
        var i_41_ = i_34_
        var i_42_ = i_33_ * (-1 + i_16_)
        if (i_6_ >= anInt1910 && anInt513 >= i_6_) {
            val `is` = row(i_6_)
            val i_43_ = method831(anInt1745, i_5_ + -i, anInt4960)
            val i_44_ = method831(anInt1745, i + i_5_, anInt4960)
            val i_45_ = method831(anInt1745, i_5_ + -i_15_, anInt4960)
            val i_46_ = method831(anInt1745, i_5_ + i_15_, anInt4960)
            method1156(i_45_, `is`, i_43_, i_10_)
            method1156(i_46_, `is`, i_45_, i_9_)
            method1156(i_44_, `is`, i_46_, i_10_)
        }
        while (i_13_ > 0) {
            val bool = i_16_ >= i_13_
            if (bool) {
                if (i_29_ < 0) {
                    while (i_29_ < 0) {
                        i_29_ += i_37_
                        i_30_ += i_41_
                        i_41_ += i_34_
                        i_14_++
                        i_37_ += i_34_
                    }
                }
                if (i_30_ < 0) {
                    i_29_ += i_37_
                    i_30_ += i_41_
                    i_41_ += i_34_
                    i_37_ += i_34_
                    i_14_++
                }
                i_29_ += -i_42_
                i_30_ += -i_38_
                i_42_ -= i_33_
                i_38_ -= i_33_
            }
            if (i_27_ < 0) {
                while (i_27_ < 0) {
                    i_27_ += i_35_
                    i_28_ += i_39_
                    i_12_++
                    i_39_ += i_32_
                    i_35_ += i_32_
                }
            }
            if (i_28_ < 0) {
                i_28_ += i_39_
                i_27_ += i_35_
                i_35_ += i_32_
                i_12_++
                i_39_ += i_32_
            }
            i_27_ += -i_40_
            i_28_ += -i_36_
            i_13_--
            i_36_ -= i_31_
            i_40_ -= i_31_
            val i_47_ = -i_13_ + i_6_
            val i_48_ = i_13_ + i_6_
            if (i_48_ >= anInt1910 && anInt513 >= i_47_) {
                val i_49_ = method831(anInt1745, i_12_ + i_5_, anInt4960)
                val i_50_ = method831(anInt1745, -i_12_ + i_5_, anInt4960)
                if (bool) {
                    val i_51_ = method831(anInt1745, i_5_ - -i_14_, anInt4960)
                    val i_52_ = method831(anInt1745, -i_14_ + i_5_, anInt4960)
                    if (anInt1910 <= i_47_) {
                        val `is` = row(i_47_)
                        method1156(i_52_, `is`, i_50_, i_10_)
                        method1156(i_51_, `is`, i_52_, i_9_)
                        method1156(i_49_, `is`, i_51_, i_10_)
                    }
                    if (anInt513 >= i_48_) {
                        val `is` = row(i_48_)
                        method1156(i_52_, `is`, i_50_, i_10_)
                        method1156(i_51_, `is`, i_52_, i_9_)
                        method1156(i_49_, `is`, i_51_, i_10_)
                    }
                } else {
                    if (i_47_ >= anInt1910) method1156(i_49_, row(i_47_), i_50_, i_10_)
                    if (i_48_ <= anInt513) method1156(i_49_, row(i_48_), i_50_, i_10_)
                }
            }
        }
    }

    /** Class348_Sub31.method3012 */
    fun method3012(i: Int, i_44_: Int, i_45_: Int, i_46_: Int, i_47_: Int, i_49_: Int, i_50_: Int) {
        var i_51_ = 0
        var i_52_ = i
        var i_53_ = 0
        val i_54_ = -i_50_ + i_45_
        val i_55_ = -i_50_ + i
        val i_56_ = i_45_ * i_45_
        val i_57_ = i * i
        val i_58_ = i_54_ * i_54_
        val i_59_ = i_55_ * i_55_
        val i_60_ = i_57_ shl 1
        val i_61_ = i_56_ shl 1
        val i_62_ = i_59_ shl 1
        val i_63_ = i_58_ shl 1
        val i_64_ = i shl 1
        val i_65_ = i_55_ shl 1
        var i_66_ = i_60_ + (-i_64_ + 1) * i_56_
        var i_67_ = i_57_ + -(i_61_ * (i_64_ - 1))
        var i_68_ = i_62_ + (-i_65_ + 1) * i_58_
        var i_69_ = -((i_65_ + -1) * i_63_) + i_59_
        val i_70_ = i_56_ shl 2
        val i_71_ = i_57_ shl 2
        val i_72_ = i_58_ shl 2
        val i_73_ = i_59_ shl 2
        var i_74_ = 3 * i_60_
        var i_75_ = (-3 + i_64_) * i_61_
        var i_76_ = 3 * i_62_
        var i_77_ = i_63_ * (-3 + i_65_)
        var i_78_ = i_71_
        var i_79_ = i_70_ * (-1 + i)
        var i_81_ = i_73_
        var i_82_ = (-1 + i_55_) * i_72_
        val `is` = row(i_49_)
        method1156(i_47_ - i_54_, `is`, -i_45_ + i_47_, i_46_)
        method1156(i_47_ + i_54_, `is`, -i_54_ + i_47_, i_44_)
        method1156(i_45_ + i_47_, `is`, i_47_ + i_54_, i_46_)
        while (i_52_ > 0) {
            val bool = i_55_ >= i_52_
            if (i_66_ < 0) {
                while (i_66_ < 0) {
                    i_67_ += i_78_
                    i_66_ += i_74_
                    i_78_ += i_71_
                    i_51_++
                    i_74_ += i_71_
                }
            }
            if (bool) {
                if (i_68_ < 0) {
                    while (i_68_ < 0) {
                        i_69_ += i_81_
                        i_68_ += i_76_
                        i_53_++
                        i_81_ += i_73_
                        i_76_ += i_73_
                    }
                }
                if (i_69_ < 0) {
                    i_69_ += i_81_
                    i_68_ += i_76_
                    i_76_ += i_73_
                    i_53_++
                    i_81_ += i_73_
                }
                i_69_ += -i_77_
                i_68_ += -i_82_
                i_77_ -= i_72_
                i_82_ -= i_72_
            }
            if (i_67_ < 0) {
                i_67_ += i_78_
                i_66_ += i_74_
                i_78_ += i_71_
                i_51_++
                i_74_ += i_71_
            }
            i_67_ += -i_75_
            i_66_ += -i_79_
            i_75_ -= i_70_
            i_52_--
            i_79_ -= i_70_
            val i_83_ = -i_52_ + i_49_
            val i_84_ = i_49_ - -i_52_
            val i_85_ = i_51_ + i_47_
            val i_86_ = -i_51_ + i_47_
            if (bool) {
                val i_87_ = i_47_ + i_53_
                val i_88_ = -i_53_ + i_47_
                method1156(i_88_, row(i_83_), i_86_, i_46_)
                method1156(i_87_, row(i_83_), i_88_, i_44_)
                method1156(i_85_, row(i_83_), i_87_, i_46_)
                method1156(i_88_, row(i_84_), i_86_, i_46_)
                method1156(i_87_, row(i_84_), i_88_, i_44_)
                method1156(i_85_, row(i_84_), i_87_, i_46_)
            } else {
                method1156(i_85_, row(i_83_), i_86_, i_46_)
                method1156(i_85_, row(i_84_), i_86_, i_46_)
            }
        }
    }

    /** ha.method3641 */
    fun method3641(i: Int, i_35_: Int, i_37_: Int, i_38_: Int, i_39_: Int) {
        if (i_35_ == i_38_) method1116(i, i_37_, i_39_, i_38_)
        else if (anInt4960 <= i_39_ + -i_38_ && anInt1745 >= i_39_ - -i_38_ && -i_35_ + i >= anInt1910 && anInt513 >= i + i_35_) method515(i_37_, i_39_, i, i_35_, i_38_)
        else method1839(i_39_, i_35_, i, i_37_, i_38_)
    }

    /** Class127.method1116 */
    fun method1116(i: Int, i_8_: Int, i_9_: Int, i_10_: Int) {
        if (anInt4960 > i_9_ + -i_10_ || anInt1745 < i_10_ + i_9_ || anInt1910 > i - i_10_ || i - -i_10_ > anInt513) method203(i_9_, i_8_, i, i_10_)
        else method253(i, i_9_, i_8_, i_10_)
    }

    /** Class6.method203 */
    fun method203(i_0_: Int, i_1_: Int, i_2_: Int, i_3_: Int) {
        var i_4_ = 0
        var i_5_ = i_3_
        var i_6_ = -i_3_
        var i_7_ = -1
        val i_8_ = method831(anInt1745, i_3_ + i_0_, anInt4960)
        val i_9_ = method831(anInt1745, -i_3_ + i_0_, anInt4960)
        method1156(i_8_, row(i_2_), i_9_, i_1_)
        while (i_5_ > i_4_) {
            i_7_ += 2
            i_6_ += i_7_
            if (i_6_ > 0) {
                i_5_--
                i_6_ -= i_5_ shl 1
                val i_10_ = -i_5_ + i_2_
                val i_11_ = i_5_ + i_2_
                if (i_11_ >= anInt1910 && anInt513 >= i_10_) {
                    val i_12_ = method831(anInt1745, i_4_ + i_0_, anInt4960)
                    val i_13_ = method831(anInt1745, i_0_ + -i_4_, anInt4960)
                    if (i_11_ <= anInt513) method1156(i_12_, row(i_11_), i_13_, i_1_)
                    if (anInt1910 <= i_10_) method1156(i_12_, row(i_10_), i_13_, i_1_)
                }
            }
            val i_14_ = -++i_4_ + i_2_
            val i_15_ = i_2_ - -i_4_
            if (anInt1910 <= i_15_ && anInt513 >= i_14_) {
                val i_16_ = method831(anInt1745, i_0_ + i_5_, anInt4960)
                val i_17_ = method831(anInt1745, i_0_ + -i_5_, anInt4960)
                if (i_15_ <= anInt513) method1156(i_16_, row(i_15_), i_17_, i_1_)
                if (i_14_ >= anInt1910) method1156(i_16_, row(i_14_), i_17_, i_1_)
            }
        }
    }

    /** Class14_Sub4.method253 */
    fun method253(i: Int, i_12_: Int, i_14_: Int, i_15_: Int) {
        var i_16_ = 0
        var i_17_ = i_15_
        var i_18_ = -i_15_
        var i_19_ = -1
        method1156(i_12_ + i_15_, row(i), i_12_ + -i_15_, i_14_)
        while (i_16_ < i_17_) {
            i_19_ += 2
            i_18_ += i_19_
            i_16_++
            if (i_18_ >= 0) {
                i_17_--
                i_18_ -= i_17_ shl 1
                val `is` = row(i - -i_17_)
                val is_20_ = row(i + -i_17_)
                val i_21_ = i_16_ + i_12_
                val i_22_ = i_12_ + -i_16_
                method1156(i_21_, `is`, i_22_, i_14_)
                method1156(i_21_, is_20_, i_22_, i_14_)
            }
            val i_23_ = i_17_ + i_12_
            val i_24_ = i_12_ - i_17_
            val `is` = row(i_16_ + i)
            val is_25_ = row(-i_16_ + i)
            method1156(i_23_, `is`, i_24_, i_14_)
            method1156(i_23_, is_25_, i_24_, i_14_)
        }
    }

    /**
     * Class55.method515
     * Note: the Java source has `i_23_ = /*---*/i_8_ + i_3_;` making both rows below the centre (the
     * top half of the ellipse is never filled). Kept as-is to match the Java client.
     */
    fun method515(i: Int, i_2_: Int, i_3_: Int, i_4_: Int, i_5_: Int) {
        var i_7_ = 0
        var i_8_ = i_4_
        val i_9_ = i_5_ * i_5_
        val i_10_ = i_4_ * i_4_
        val i_11_ = i_10_ shl 1
        val i_12_ = i_9_ shl 1
        val i_14_ = i_4_ shl 1
        var i_15_ = i_11_ + i_9_ * (1 + -i_14_)
        var i_16_ = -(i_12_ * (i_14_ - 1)) + i_10_
        val i_17_ = i_9_ shl 2
        val i_18_ = i_10_ shl 2
        var i_19_ = (3 + (i_7_ shl 1)) * i_11_
        var i_20_ = i_12_ * ((i_8_ shl 1) - 3)
        var i_21_ = i_18_ * (i_7_ + 1)
        method1156(i_2_ + i_5_, row(i_3_), -i_5_ + i_2_, i)
        var i_22_ = i_17_ * (-1 + i_8_)
        while (i_8_ > 0) {
            if (i_15_ < 0) {
                while (i_15_ < 0) {
                    i_16_ += i_21_
                    i_15_ += i_19_
                    i_7_++
                    i_21_ += i_18_
                    i_19_ += i_18_
                }
            }
            if (i_16_ < 0) {
                i_16_ += i_21_
                i_15_ += i_19_
                i_7_++
                i_21_ += i_18_
                i_19_ += i_18_
            }
            i_16_ += -i_20_
            i_15_ += -i_22_
            i_22_ -= i_17_
            i_20_ -= i_17_
            val i_23_ = i_8_ + i_3_
            val i_24_ = i_3_ - -i_8_
            val i_25_ = i_2_ - -i_7_
            val i_26_ = i_2_ + -i_7_
            method1156(i_25_, row(i_23_), i_26_, i)
            method1156(i_25_, row(i_24_), i_26_, i)
        }
    }

    /** Class239_Sub27.method1839 (dummy i_2_ == -1 inlined) */
    fun method1839(i: Int, i_0_: Int, i_1_: Int, i_3_: Int, i_4_: Int) {
        val i_2_ = -1
        var i_5_ = 0
        var i_6_ = i_0_
        val i_7_ = i_4_ * i_4_
        val i_8_ = i_0_ * i_0_
        val i_9_ = i_8_ shl 1
        val i_10_ = i_7_ shl 1
        val i_11_ = i_0_ shl 1
        var i_12_ = i_9_ + i_7_ * (-i_11_ + 1)
        var i_13_ = -((i_11_ + i_2_) * i_10_) + i_8_
        val i_14_ = i_7_ shl 2
        val i_15_ = i_8_ shl 2
        var i_16_ = i_9_ * (3 + (i_5_ shl 1))
        var i_17_ = i_10_ * (-3 + (i_6_ shl 1))
        var i_18_ = i_15_ * (i_5_ - -1)
        if (anInt1910 <= i_1_ && anInt513 >= i_1_) {
            val i_19_ = method831(anInt1745, i + i_4_, anInt4960)
            val i_20_ = method831(anInt1745, -i_4_ + i, anInt4960)
            method1156(i_19_, row(i_1_), i_20_, i_3_)
        }
        var i_21_ = i_14_ * (i_6_ - 1)
        while (i_6_ > 0) {
            if (i_12_ < 0) {
                while (i_12_ < 0) {
                    i_13_ += i_18_
                    i_12_ += i_16_
                    i_5_++
                    i_16_ += i_15_
                    i_18_ += i_15_
                }
            }
            if (i_13_ < 0) {
                i_12_ += i_16_
                i_13_ += i_18_
                i_16_ += i_15_
                i_18_ += i_15_
                i_5_++
            }
            i_13_ += -i_17_
            i_12_ += -i_21_
            i_21_ -= i_14_
            i_17_ -= i_14_
            i_6_--
            val i_22_ = i_1_ + -i_6_
            val i_23_ = i_6_ + i_1_
            if (i_23_ >= anInt1910 && i_22_ <= anInt513) {
                val i_24_ = method831(anInt1745, i + i_5_, anInt4960)
                val i_25_ = method831(anInt1745, -i_5_ + i, anInt4960)
                if (anInt1910 <= i_22_) method1156(i_24_, row(i_22_), i_25_, i_3_)
                if (anInt513 >= i_23_) method1156(i_24_, row(i_23_), i_25_, i_3_)
            }
        }
    }

    /* ---------------- Rectangle (Class50_Sub2) ---------------- */

    /** Class170.method1308 */
    fun method1308(i: Int, i_14_: Int, i_15_: Int, i_16_: Int, i_18_: Int, i_19_: Int, i_20_: Int) {
        if (anInt4960 <= i_15_ && i_19_ <= anInt1745 && i_14_ >= anInt1910 && anInt513 >= i_20_) method1730(i_16_, i_20_, i_14_, i_19_, i, i_18_, i_15_)
        else method4004(i_20_, i_19_, i_14_, i, i_18_, i_15_, i_16_)
    }

    /** Class239_Sub3.method1730 */
    fun method1730(i: Int, i_5_: Int, i_6_: Int, i_7_: Int, i_8_: Int, i_9_: Int, i_11_: Int) {
        val i_12_ = -i_9_ + i_5_
        val i_13_ = i_9_ + i_6_
        for (i_14_ in i_6_..<i_13_) method1156(i_7_, row(i_14_), i_11_, i)
        val i_15_ = i_7_ + -i_9_
        val i_16_ = i_9_ + i_11_
        var i_17_ = i_5_
        while (i_12_ < i_17_) {
            method1156(i_7_, row(i_17_), i_11_, i)
            i_17_--
        }
        for (i_18_ in i_13_..i_12_) {
            val `is` = row(i_18_)
            method1156(i_16_, `is`, i_11_, i)
            method1156(i_15_, `is`, i_16_, i_8_)
            method1156(i_7_, `is`, i_15_, i)
        }
    }

    /** s_Sub3.method4004 (dummy i_7_ == -80 so the body always runs) */
    fun method4004(i: Int, i_3_: Int, i_4_: Int, i_5_: Int, i_6_: Int, i_8_: Int, i_9_: Int) {
        val i_10_ = method831(anInt513, i_4_, anInt1910)
        val i_11_ = method831(anInt513, i, anInt1910)
        val i_12_ = method831(anInt1745, i_8_, anInt4960)
        val i_13_ = method831(anInt1745, i_3_, anInt4960)
        val i_14_ = method831(anInt513, i_4_ + i_6_, anInt1910)
        val i_15_ = method831(anInt513, i + -i_6_, anInt1910)
        for (i_16_ in i_10_..<i_14_) method1156(i_13_, row(i_16_), i_12_, i_9_)
        var i_17_ = i_11_
        while (i_17_ > i_15_) {
            method1156(i_13_, row(i_17_), i_12_, i_9_)
            i_17_--
        }
        val i_18_ = method831(anInt1745, i_8_ + i_6_, anInt4960)
        val i_19_ = method831(anInt1745, -i_6_ + i_3_, anInt4960)
        for (i_20_ in i_14_..i_15_) {
            val `is` = row(i_20_)
            method1156(i_18_, `is`, i_12_, i_9_)
            method1156(i_19_, `is`, i_18_, i_5_)
            method1156(i_13_, `is`, i_19_, i_9_)
        }
    }

    /** Class97.method872 (dummy i_0_ == 1 dropped) */
    fun method872(i: Int, i_1_: Int, i_2_: Int, i_3_: Int, i_4_: Int, i_5_: Int) {
        if (i_5_ >= anInt4960 && anInt1745 >= i_4_ && anInt1910 <= i_2_ && anInt513 >= i_3_) {
            if (i_1_ == 1) method1388(i_2_, i, i_4_, i_5_, i_3_)
            else method849(i_1_, i_2_, i_5_, i_4_, i_3_, i)
        } else if (i_1_ != 1) method535(i_5_, i_3_, i_1_, i_4_, i, i_2_)
        else method3575(i_4_, i, i_3_, i_2_, i_5_)
    }

    /** Class184.method1388 */
    fun method1388(i: Int, i_4_: Int, i_6_: Int, i_7_: Int, i_8_: Int) {
        var y = i
        var i_8 = i_8_
        method1156(i_6_, row(y++), i_7_, i_4_)
        method1156(i_6_, row(i_8--), i_7_, i_4_)
        for (i_10_ in y..i_8) {
            val `is` = row(i_10_)
            `is`[i_6_] = i_4_
            `is`[i_7_] = i_4_
        }
    }

    /** Class89.method849 */
    fun method849(i: Int, i_0_: Int, i_1_: Int, i_2_: Int, i_3_: Int, i_5_: Int) {
        val i_6_ = i_0_ + i
        val i_7_ = -i + i_3_
        for (i_8_ in i_0_..<i_6_) method1156(i_2_, row(i_8_), i_1_, i_5_)
        val i_9_ = -i + i_2_
        var i_10_ = i_3_
        while (i_10_ > i_7_) {
            method1156(i_2_, row(i_10_), i_1_, i_5_)
            i_10_--
        }
        val i_11_ = i + i_1_
        for (i_12_ in i_6_..i_7_) {
            val `is` = row(i_12_)
            method1156(i_11_, `is`, i_1_, i_5_)
            method1156(i_2_, `is`, i_9_, i_5_)
        }
    }

    /** Class58.method535 */
    fun method535(i: Int, i_7_: Int, i_8_: Int, i_9_: Int, i_10_: Int, i_11_: Int) {
        val i_13_ = method831(anInt513, i_11_, anInt1910)
        val i_14_ = method831(anInt513, i_7_, anInt1910)
        val i_15_ = method831(anInt1745, i, anInt4960)
        val i_16_ = method831(anInt1745, i_9_, anInt4960)
        val i_17_ = method831(anInt513, i_11_ + i_8_, anInt1910)
        val i_18_ = method831(anInt513, -i_8_ + i_7_, anInt1910)
        for (i_19_ in i_13_..<i_17_) method1156(i_16_, row(i_19_), i_15_, i_10_)
        var i_20_ = i_14_
        while (i_18_ < i_20_) {
            method1156(i_16_, row(i_20_), i_15_, i_10_)
            i_20_--
        }
        val i_21_ = method831(anInt1745, i_8_ + i, anInt4960)
        val i_22_ = method831(anInt1745, -i_8_ + i_9_, anInt4960)
        for (i_23_ in i_17_..i_18_) {
            val `is` = row(i_23_)
            method1156(i_21_, `is`, i_15_, i_10_)
            method1156(i_16_, `is`, i_22_, i_10_)
        }
    }

    /** Class369_Sub3_Sub1.method3575 */
    fun method3575(i_0_: Int, i_1_: Int, i_2_: Int, i_3_: Int, i_4_: Int) {
        var i_0 = i_0_
        var i_2 = i_2_
        var i_3 = i_3_
        var i_4 = i_4_
        if (i_3 <= anInt513 && i_2 >= anInt1910) {
            val bool: Boolean
            if (anInt4960 <= i_4) {
                if (i_4 <= anInt1745) bool = true
                else {
                    bool = false
                    i_4 = anInt1745
                }
            } else {
                i_4 = anInt4960
                bool = false
            }
            val bool_5_: Boolean
            if (anInt4960 > i_0) {
                i_0 = anInt4960
                bool_5_ = false
            } else if (anInt1745 < i_0) {
                i_0 = anInt1745
                bool_5_ = false
            } else bool_5_ = true
            if (i_3 < anInt1910) i_3 = anInt1910
            else method1156(i_0, row(i_3++), i_4, i_1_)
            if (anInt513 >= i_2) method1156(i_0, row(i_2--), i_4, i_1_)
            else i_2 = anInt513
            if (!bool || !bool_5_) {
                if (bool) {
                    for (i_7_ in i_3..i_2) row(i_7_)[i_4] = i_1_
                } else if (bool_5_) {
                    for (i_6_ in i_3..i_2) row(i_6_)[i_0] = i_1_
                }
            } else {
                for (i_8_ in i_3..i_2) {
                    val `is` = row(i_8_)
                    `is`[i_4] = i_1_
                    `is`[i_0] = i_1_
                }
            }
        }
    }

    /** Class318_Sub1_Sub5.method2486 */
    fun method2486(i_5_: Int, i_6_: Int, i_7_: Int, i_8_: Int, i_9_: Int) {
        if (i_8_ >= anInt4960 && i_5_ <= anInt1745 && anInt1910 <= i_9_ && anInt513 >= i_7_) method1111(i_7_, i_5_, i_8_, i_9_, i_6_)
        else method1792(i_9_, i_8_, i_7_, i_5_, i_6_)
    }

    /** Class125.method1111 */
    fun method1111(i: Int, i_7_: Int, i_8_: Int, i_9_: Int, i_10_: Int) {
        for (i_11_ in i_9_..i) method1156(i_7_, row(i_11_), i_8_, i_10_)
    }

    /** Class239_Sub16.method1792 */
    fun method1792(i: Int, i_12_: Int, i_13_: Int, i_14_: Int, i_16_: Int) {
        val i_17_ = method831(anInt513, i, anInt1910)
        val i_18_ = method831(anInt513, i_13_, anInt1910)
        val i_19_ = method831(anInt1745, i_12_, anInt4960)
        val i_20_ = method831(anInt1745, i_14_, anInt4960)
        for (i_22_ in i_17_..i_18_) method1156(i_20_, row(i_22_), i_19_, i_16_)
    }

    /* ---------------- Curve (Class50_Sub3) ---------------- */

    /** Class367_Sub3.method3540 */
    fun method3540(i: Int, i_2_: Int, i_3_: Int, i_4_: Int, i_5_: Int, i_6_: Int, i_7_: Int, i_8_: Int, i_9_: Int) {
        if (i_6_ >= anInt4960 && anInt1745 >= i_6_ && anInt4960 <= i_5_ && anInt1745 >= i_5_ && i_7_ >= anInt4960 && i_7_ <= anInt1745 && anInt4960 <= i_9_ && anInt1745 >= i_9_ && i >= anInt1910 && anInt513 >= i && i_2_ >= anInt1910 && anInt513 >= i_2_ && anInt1910 <= i_8_ && i_8_ <= anInt513 && anInt1910 <= i_4_ && i_4_ <= anInt513) {
            method3009(i_2_, i_8_, i_5_, i_9_, i_4_, i_7_, i, i_3_, i_6_)
        } else method2364(i_3_, i_7_, i_6_, i_2_, i_5_, i_8_, i_9_, i, i_4_)
    }

    /** Class348_Sub31.method3009 */
    fun method3009(i: Int, i_3_: Int, i_4_: Int, i_5_: Int, i_6_: Int, i_8_: Int, i_9_: Int, i_10_: Int, i_11_: Int) {
        if (i_11_ != i_4_ || i != i_9_ || i_8_ != i_5_ || i_3_ != i_6_) {
            var i_12_ = i_11_
            var i_13_ = i_9_
            val i_14_ = 3 * i_11_
            val i_15_ = 3 * i_9_
            val i_16_ = 3 * i_4_
            val i_17_ = 3 * i
            val i_18_ = 3 * i_8_
            val i_19_ = i_3_ * 3
            val i_20_ = -i_11_ + (i_16_ + -i_18_) + i_5_
            val i_21_ = -i_9_ + i_17_ + -i_19_ + i_6_
            val i_22_ = i_14_ + (-i_16_ + (i_18_ + -i_16_))
            val i_23_ = -i_17_ + (-i_17_ + (i_19_ - -i_15_))
            val i_24_ = -i_14_ + i_16_
            val i_25_ = i_17_ - i_15_
            var i_26_ = 128
            while (i_26_ <= 4096) {
                val i_27_ = i_26_ * i_26_ shr 12
                val i_28_ = i_27_ * i_26_ shr 12
                val i_29_ = i_20_ * i_28_
                val i_30_ = i_28_ * i_21_
                val i_31_ = i_22_ * i_27_
                val i_32_ = i_27_ * i_23_
                val i_33_ = i_24_ * i_26_
                val i_34_ = i_25_ * i_26_
                val i_35_ = i_11_ + (i_29_ + (i_31_ + i_33_) shr 12)
                val i_36_ = i_9_ + (i_30_ - -i_32_ + i_34_ shr 12)
                method1783(i_12_, i_13_, i_36_, i_35_, i_10_)
                i_13_ = i_36_
                i_12_ = i_35_
                i_26_ += 128
            }
        } else method1783(i_11_, i_9_, i_6_, i_5_, i_10_)
    }

    /** Class316.method2364 (dummy i_7_ == 3 dropped) */
    fun method2364(i: Int, i_0_: Int, i_1_: Int, i_2_: Int, i_3_: Int, i_4_: Int, i_5_: Int, i_6_: Int, i_8_: Int) {
        if (i_3_ != i_1_ || i_2_ != i_6_ || i_5_ != i_0_ || i_8_ != i_4_) {
            var i_9_ = i_1_
            var i_10_ = i_6_
            val i_11_ = i_1_ * 3
            val i_12_ = 3 * i_6_
            val i_13_ = i_3_ * 3
            val i_14_ = 3 * i_2_
            val i_15_ = 3 * i_0_
            val i_16_ = i_4_ * 3
            val i_17_ = -i_1_ + (i_13_ + i_5_) + -i_15_
            val i_18_ = -i_6_ + (i_8_ + (-i_16_ - -i_14_))
            val i_19_ = -i_13_ + -i_13_ + (i_15_ + i_11_)
            val i_20_ = i_12_ + -i_14_ + (i_16_ - i_14_)
            val i_21_ = -i_11_ + i_13_
            val i_22_ = -i_12_ + i_14_
            var i_23_ = 128
            while (i_23_ <= 4096) {
                val i_24_ = i_23_ * i_23_ shr 12
                val i_25_ = i_24_ * i_23_ shr 12
                val i_26_ = i_17_ * i_25_
                val i_27_ = i_18_ * i_25_
                val i_28_ = i_19_ * i_24_
                val i_29_ = i_24_ * i_20_
                val i_30_ = i_23_ * i_21_
                val i_31_ = i_22_ * i_23_
                val i_32_ = i_1_ - -(i_26_ - (-i_28_ + -i_30_) shr 12)
                val i_33_ = i_6_ - -(i_29_ + (i_27_ - -i_31_) shr 12)
                method2665(i_9_, i, i_10_, i_33_, i_32_)
                i_9_ = i_32_
                i_10_ = i_33_
                i_23_ += 128
            }
        } else method2665(i_1_, i, i_6_, i_8_, i_5_)
    }

    /* ---------------- Line (Class50_Sub4) ---------------- */

    /** Class339.method2665 (clipped line) */
    fun method2665(i: Int, i_1_: Int, i_2_: Int, i_3_: Int, i_4_: Int) {
        val i_5_ = i_4_ - i
        val i_6_ = -i_2_ + i_3_
        if (i_5_ == 0) {
            if (i_6_ != 0) method2656(i_2_, i, i_1_, i_3_)
            return
        } else if (i_6_ == 0) {
            method196(i_4_, i_1_, i, i_2_)
            return
        }
        val i_7_ = (i_6_ shl 12) / i_5_
        val i_8_ = -(i_7_ * i shr 12) + i_2_
        var i_9_: Int
        var i_10_: Int
        if (i < anInt4960) {
            i_9_ = (i_7_ * anInt4960 shr 12) + i_8_
            i_10_ = anInt4960
        } else if (i > anInt1745) {
            i_9_ = i_8_ - -(anInt1745 * i_7_ shr 12)
            i_10_ = anInt1745
        } else {
            i_10_ = i
            i_9_ = i_2_
        }
        var i_11_: Int
        var i_12_: Int
        if (anInt4960 <= i_4_) {
            if (anInt1745 >= i_4_) {
                i_11_ = i_4_
                i_12_ = i_3_
            } else {
                i_11_ = anInt1745
                i_12_ = i_8_ + (anInt1745 * i_7_ shr 12)
            }
        } else {
            i_11_ = anInt4960
            i_12_ = (i_7_ * anInt4960 shr 12) + i_8_
        }
        if (i_12_ < anInt1910) {
            i_11_ = (anInt1910 + -i_8_ shl 12) / i_7_
            i_12_ = anInt1910
        } else if (anInt513 < i_12_) {
            i_11_ = (anInt513 - i_8_ shl 12) / i_7_
            i_12_ = anInt513
        }
        if (anInt1910 <= i_9_) {
            if (anInt513 < i_9_) {
                i_10_ = (anInt513 - i_8_ shl 12) / i_7_
                i_9_ = anInt513
            }
        } else {
            i_10_ = (-i_8_ + anInt1910 shl 12) / i_7_
            i_9_ = anInt1910
        }
        method1783(i_10_, i_9_, i_12_, i_11_, i_1_)
    }

    /** Class336.method2656 */
    fun method2656(i: Int, i_0_: Int, i_2_: Int, i_3_: Int) {
        if (i_0_ >= anInt4960 && anInt1745 >= i_0_) {
            val y0 = method831(anInt513, i, anInt1910)
            val y1 = method831(anInt513, i_3_, anInt1910)
            method2641(y1, y0, i_0_, i_2_)
        }
    }

    /** Class5_Sub2.method196 */
    fun method196(i: Int, i_13_: Int, i_14_: Int, i_15_: Int) {
        if (anInt1910 <= i_15_ && anInt513 >= i_15_) {
            val x0 = method831(anInt1745, i_14_, anInt4960)
            val x1 = method831(anInt1745, i, anInt4960)
            method223(x1, i_13_, i_15_, x0)
        }
    }

    /** Class239_Sub15.method1783 (unclipped bresenham line, dummy i_2_ == -1 dropped) */
    fun method1783(i: Int, i_3_: Int, i_4_: Int, i_5_: Int, i_6_: Int) {
        var i = i
        var i_3 = i_3_
        var i_4 = i_4_
        var i_5 = i_5_
        var i_7_ = -i_3 + i_4
        var i_8_ = i_5 + -i
        if (i_8_ == 0) {
            if (i_7_ != 0) method2641(i_4, i_3, i, i_6_)
            return
        } else if (i_7_ == 0) {
            method223(i_5, i_6_, i_3, i)
            return
        }
        if (i_7_ < 0) i_7_ = -i_7_
        if (i_8_ < 0) i_8_ = -i_8_
        val bool = i_8_ < i_7_
        if (bool) {
            val i_9_ = i
            val i_10_ = i_5
            i = i_3
            i_5 = i_4
            i_3 = i_9_
            i_4 = i_10_
        }
        if (i_5 < i) {
            val i_11_ = i
            i = i_5
            val i_12_ = i_3
            i_5 = i_11_
            i_3 = i_4
            i_4 = i_12_
        }
        var i_13_ = i_3
        val i_14_ = -i + i_5
        var i_15_ = -i_3 + i_4
        var i_16_ = -(i_14_ shr 1)
        if (i_15_ < 0) i_15_ = -i_15_
        val i_17_ = if (i_4 <= i_3) -1 else 1
        if (bool) {
            for (i_19_ in i..i_5) {
                row(i_19_)[i_13_] = i_6_
                i_16_ += i_15_
                if (i_16_ > 0) {
                    i_13_ += i_17_
                    i_16_ -= i_14_
                }
            }
        } else {
            for (i_18_ in i..i_5) {
                i_16_ += i_15_
                row(i_13_)[i_18_] = i_6_
                if (i_16_ > 0) {
                    i_13_ += i_17_
                    i_16_ -= i_14_
                }
            }
        }
    }
}
