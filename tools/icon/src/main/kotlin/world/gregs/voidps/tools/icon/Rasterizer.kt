package world.gregs.voidps.tools.icon

/* Class109 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Rasterizer(private val aHa_Sub1_1666: JavaToolkit, private val aJavaThreadResource_1670: JavaThreadResource) {
    var aBoolean1667: Boolean = false
    var anInt1665: Int = 0
    var anInt1668: Int = 0
    var aBoolean1669: Boolean = true
    var clamp: Boolean = false
    var height: Int = 0
    private val anIntArray1673: IntArray
    var anInt1674: Int = 0
    private var aBoolean1675 = false
    var lineOffsets: IntArray = IntArray(4096)
    private val aFloatArray1677: FloatArray
    private val anInt1678: Int
    var width: Int = 0
    private val aBoolean1680 = false
    private val aFloat1681 = 0.0f
    private val aFloat1682 = 0.0f
    private var anInt1683 = 0
    private val aFloat1684 = 0.0f
    private val anIntArray1685: IntArray? = null
    private val anInt1686 = 0
    private val anInt1687: Int
    private val anInt1688 = 0
    private val anInt1689: Int
    private var anInt1690 = 0
    private val anInt1691 = 0
    private val anIntArray1692: IntArray? = null
    private var anInt1693 = 0
    private var aBoolean1694 = true
    private val anInt1695 = 0
    private var anInt1696 = 0
    private val anInt1697: Int
    private var anIntArray1698: IntArray? = null

    private fun method1016(`is`: IntArray, is_0_: IntArray?, i: Int, i_1_: Int, i_2_: Int, f: Float, f_3_: Float, f_4_: Float, f_5_: Float, f_6_: Float, f_7_: Float, f_8_: Float, f_9_: Float, f_10_: Float, f_11_: Float, f_12_: Float, f_13_: Float, f_14_: Float, f_15_: Float, f_16_: Float, f_17_: Float) {
        var i = i
        var i_1_ = i_1_
        var i_2_ = i_2_
        var f = f
        var f_4_ = f_4_
        var f_6_ = f_6_
        var f_8_ = f_8_
        var f_10_ = f_10_
        var f_12_ = f_12_
        var f_14_ = f_14_
        var f_16_ = f_16_
        var i_18_ = i_2_ - i_1_
        val f_19_ = 1.0f / i_18_.toFloat()
        val f_20_ = (f_3_ - f) * f_19_
        val f_21_ = (f_5_ - f_4_) * f_19_
        val f_22_ = (f_7_ - f_6_) * f_19_
        val f_23_ = (f_9_ - f_8_) * f_19_
        val f_24_ = (f_11_ - f_10_) * f_19_
        val f_25_ = (f_13_ - f_12_) * f_19_
        val f_26_ = (f_15_ - f_14_) * f_19_
        val f_27_ = (f_17_ - f_16_) * f_19_
        if (this.clamp) {
            if (i_2_ > this.width) i_2_ = this.width
            if (i_1_ < 0) {
                f -= f_20_ * i_1_.toFloat()
                f_4_ -= f_21_ * i_1_.toFloat()
                f_6_ -= f_22_ * i_1_.toFloat()
                f_8_ -= f_23_ * i_1_.toFloat()
                f_10_ -= f_24_ * i_1_.toFloat()
                f_12_ -= f_25_ * i_1_.toFloat()
                f_14_ -= f_26_ * i_1_.toFloat()
                f_16_ -= f_27_ * i_1_.toFloat()
                i_1_ = 0
            }
        }
        if (i_1_ < i_2_) {
            i_18_ = i_2_ - i_1_
            i += i_1_
            while (i_18_-- > 0) {
                val f_28_ = 1.0f / f
                if (f_28_ < aFloatArray1677[i]) {
                    var i_29_ = (f_4_ * f_28_ * anInt1693.toFloat()).toInt()
                    if (aBoolean1694) i_29_ = i_29_ and anInt1690
                    else if (i_29_ < 0) i_29_ = 0
                    else if (i_29_ > anInt1690) i_29_ = anInt1690
                    var i_30_ = (f_6_ * f_28_ * anInt1693.toFloat()).toInt()
                    if (aBoolean1694) i_30_ = i_30_ and anInt1690
                    else if (i_30_ < 0) i_30_ = 0
                    else if (i_30_ > anInt1690) i_30_ = anInt1690
                    val i_31_ = anIntArray1698!![i_30_ * anInt1693 + i_29_]
                    var i_32_ = 255
                    if (anInt1683 == 2) i_32_ = i_31_ shr 24 and 0xff
                    else if (anInt1683 == 1) i_32_ = if (i_31_ == 0) 0 else 255
                    else i_32_ = f_10_.toInt()
                    if (i_32_ != 0) {
                        if (i_32_ == 255) {
                            var i_38_ = (0xffffff.inv() or ((f_12_ * (i_31_ shr 16 and 0xff).toFloat()).toInt() shl 8 and 0xff0000) or ((f_14_ * (i_31_ shr 8 and 0xff).toFloat()).toInt() and 0xff00) or ((f_16_ * (i_31_ and 0xff).toFloat()).toInt() shr 8))
                            if (f_8_ != 0.0f) {
                                val i_39_ = (255.0f - f_8_).toInt()
                                val i_40_ = ((((anInt1696 and 0xff00ff) * f_8_.toInt() and 0xff00ff.inv()) or ((anInt1696 and 0xff00) * f_8_.toInt() and 0xff0000)) ushr 8)
                                i_38_ = (((i_38_ and 0xff00ff) * i_39_ and 0xff00ff.inv() or ((i_38_ and 0xff00) * i_39_ and 0xff0000)) ushr 8) + i_40_
                            }
                            `is`[i] = i_38_
                            aFloatArray1677[i] = f_28_
                        } else {
                            var i_33_ = (0xffffff.inv() or ((f_12_ * (i_31_ shr 16 and 0xff).toFloat()).toInt() shl 8 and 0xff0000) or ((f_14_ * (i_31_ shr 8 and 0xff).toFloat()).toInt() and 0xff00) or ((f_16_ * (i_31_ and 0xff).toFloat()).toInt() shr 8))
                            if (f_8_ != 0.0f) {
                                val i_34_ = (255.0f - f_8_).toInt()
                                val i_35_ = ((((anInt1696 and 0xff00ff) * f_8_.toInt() and 0xff00ff.inv()) or ((anInt1696 and 0xff00) * f_8_.toInt() and 0xff0000)) ushr 8)
                                i_33_ = (((i_33_ and 0xff00ff) * i_34_ and 0xff00ff.inv() or ((i_33_ and 0xff00) * i_34_ and 0xff0000)) ushr 8) + i_35_
                            }
                            val i_36_ = `is`[i]
                            val i_37_ = 255 - i_32_
                            i_33_ = ((((i_36_ and 0xff00ff) * i_37_ + (i_33_ and 0xff00ff) * i_32_) and 0xff00ff.inv()) + (((i_36_ and 0xff00) * i_37_ + (i_33_ and 0xff00) * i_32_) and 0xff0000)) shr 8
                            `is`[i] = i_33_
                            aFloatArray1677[i] = f_28_
                        }
                    }
                }
                i++
                f += f_20_
                f_4_ += f_21_
                f_6_ += f_22_
                f_8_ += f_23_
                f_10_ += f_24_
                f_12_ += f_25_
                f_14_ += f_26_
                f_16_ += f_27_
            }
        }
    }

    fun method1017(): Int {
        return this.lineOffsets[0] / anInt1678
    }

    fun method1018(f: Float, f_41_: Float, f_42_: Float, f_43_: Float, f_44_: Float, f_45_: Float, f_46_: Float, f_47_: Float, f_48_: Float, i: Int) {
        var f = f
        var f_41_ = f_41_
        var f_42_ = f_42_
        var f_43_ = f_43_
        var f_44_ = f_44_
        var f_45_ = f_45_
        var f_46_ = f_46_
        var f_47_ = f_47_
        var f_48_ = f_48_
        if (aBoolean1675) {
            aHa_Sub1_1666.line(f.toInt(), f_43_.toInt(), f_44_.toInt(), -8003, i, f_41_.toInt())
            aHa_Sub1_1666.line(f_41_.toInt(), f_44_.toInt(), f_45_.toInt(), -8003, i, f_42_.toInt())
            aHa_Sub1_1666.line(f_42_.toInt(), f_45_.toInt(), f_43_.toInt(), -8003, i, f.toInt())
        } else {
            val f_49_ = f_44_ - f_43_
            val f_50_ = f_41_ - f
            val f_51_ = f_45_ - f_43_
            val f_52_ = f_42_ - f
            val f_53_ = f_47_ - f_46_
            val f_54_ = f_48_ - f_46_
            var f_55_ = 0.0f
            if (f_41_ != f) f_55_ = (f_44_ - f_43_) / (f_41_ - f)
            var f_56_ = 0.0f
            if (f_42_ != f_41_) f_56_ = (f_45_ - f_44_) / (f_42_ - f_41_)
            var f_57_ = 0.0f
            if (f_42_ != f) f_57_ = (f_43_ - f_45_) / (f - f_42_)
            val f_58_ = f_49_ * f_52_ - f_51_ * f_50_
            if (f_58_ != 0.0f) {
                val f_59_ = (f_53_ * f_52_ - f_54_ * f_50_) / f_58_
                val f_60_ = (f_54_ * f_49_ - f_53_ * f_51_) / f_58_
                if (f <= f_41_ && f <= f_42_) {
                    if (!(f >= this.height.toFloat())) {
                        if (f_41_ > this.height.toFloat()) f_41_ = this.height.toFloat()
                        if (f_42_ > this.height.toFloat()) f_42_ = this.height.toFloat()
                        f_46_ = f_46_ - f_59_ * f_43_ + f_59_
                        if (f_41_ < f_42_) {
                            f_45_ = f_43_
                            if (f < 0.0f) {
                                f_45_ -= f_57_ * f
                                f_43_ -= f_55_ * f
                                f_46_ -= f_60_ * f
                                f = 0.0f
                            }
                            if (f_41_ < 0.0f) {
                                f_44_ -= f_56_ * f_41_
                                f_41_ = 0.0f
                            }
                            if (f != f_41_ && f_57_ < f_55_ || f == f_41_ && f_57_ > f_56_) {
                                f_42_ -= f_41_
                                f_41_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_41_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_45_.toInt(), f_43_.toInt(), f_46_, f_59_)
                                    f_45_ += f_57_
                                    f_43_ += f_55_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_45_.toInt(), f_44_.toInt(), f_46_, f_59_)
                                    f_45_ += f_57_
                                    f_44_ += f_56_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                            } else {
                                f_42_ -= f_41_
                                f_41_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_41_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_43_.toInt(), f_45_.toInt(), f_46_, f_59_)
                                    f_45_ += f_57_
                                    f_43_ += f_55_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_44_.toInt(), f_45_.toInt(), f_46_, f_59_)
                                    f_45_ += f_57_
                                    f_44_ += f_56_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                            }
                        } else {
                            f_44_ = f_43_
                            if (f < 0.0f) {
                                f_44_ -= f_57_ * f
                                f_43_ -= f_55_ * f
                                f_46_ -= f_60_ * f
                                f = 0.0f
                            }
                            if (f_42_ < 0.0f) {
                                f_45_ -= f_56_ * f_42_
                                f_42_ = 0.0f
                            }
                            if (f != f_42_ && f_57_ < f_55_ || f == f_42_ && f_56_ > f_55_) {
                                f_41_ -= f_42_
                                f_42_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_44_.toInt(), f_43_.toInt(), f_46_, f_59_)
                                    f_44_ += f_57_
                                    f_43_ += f_55_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_41_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_45_.toInt(), f_43_.toInt(), f_46_, f_59_)
                                    f_45_ += f_56_
                                    f_43_ += f_55_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                            } else {
                                f_41_ -= f_42_
                                f_42_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_43_.toInt(), f_44_.toInt(), f_46_, f_59_)
                                    f_44_ += f_57_
                                    f_43_ += f_55_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_41_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f.toInt(), i, 0, f_43_.toInt(), f_45_.toInt(), f_46_, f_59_)
                                    f_45_ += f_56_
                                    f_43_ += f_55_
                                    f_46_ += f_60_
                                    f += anInt1678.toFloat()
                                }
                            }
                        }
                    }
                } else if (f_41_ <= f_42_) {
                    if (!(f_41_ >= this.height.toFloat())) {
                        if (f_42_ > this.height.toFloat()) f_42_ = this.height.toFloat()
                        if (f > this.height.toFloat()) f = this.height.toFloat()
                        f_47_ = f_47_ - f_59_ * f_44_ + f_59_
                        if (f_42_ < f) {
                            f_43_ = f_44_
                            if (f_41_ < 0.0f) {
                                f_43_ -= f_55_ * f_41_
                                f_44_ -= f_56_ * f_41_
                                f_47_ -= f_60_ * f_41_
                                f_41_ = 0.0f
                            }
                            if (f_42_ < 0.0f) {
                                f_45_ -= f_57_ * f_42_
                                f_42_ = 0.0f
                            }
                            if (f_41_ != f_42_ && f_55_ < f_56_ || f_41_ == f_42_ && f_55_ > f_57_) {
                                f -= f_42_
                                f_42_ -= f_41_
                                f_41_ = (this.lineOffsets[f_41_.toInt()]).toFloat()
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_43_.toInt(), f_44_.toInt(), f_47_, f_59_)
                                    f_43_ += f_55_
                                    f_44_ += f_56_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                                while (--f >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_43_.toInt(), f_45_.toInt(), f_47_, f_59_)
                                    f_43_ += f_55_
                                    f_45_ += f_57_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                            } else {
                                f -= f_42_
                                f_42_ -= f_41_
                                f_41_ = (this.lineOffsets[f_41_.toInt()]).toFloat()
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_44_.toInt(), f_43_.toInt(), f_47_, f_59_)
                                    f_43_ += f_55_
                                    f_44_ += f_56_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                                while (--f >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_45_.toInt(), f_43_.toInt(), f_47_, f_59_)
                                    f_43_ += f_55_
                                    f_45_ += f_57_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                            }
                        } else {
                            f_45_ = f_44_
                            if (f_41_ < 0.0f) {
                                f_45_ -= f_55_ * f_41_
                                f_44_ -= f_56_ * f_41_
                                f_47_ -= f_60_ * f_41_
                                f_41_ = 0.0f
                            }
                            if (f < 0.0f) {
                                f_43_ -= f_57_ * f
                                f = 0.0f
                            }
                            if (f_55_ < f_56_) {
                                f_42_ -= f
                                f -= f_41_
                                f_41_ = (this.lineOffsets[f_41_.toInt()]).toFloat()
                                while (--f >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_45_.toInt(), f_44_.toInt(), f_47_, f_59_)
                                    f_45_ += f_55_
                                    f_44_ += f_56_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_43_.toInt(), f_44_.toInt(), f_47_, f_59_)
                                    f_43_ += f_57_
                                    f_44_ += f_56_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                            } else {
                                f_42_ -= f
                                f -= f_41_
                                f_41_ = (this.lineOffsets[f_41_.toInt()]).toFloat()
                                while (--f >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_44_.toInt(), f_45_.toInt(), f_47_, f_59_)
                                    f_45_ += f_55_
                                    f_44_ += f_56_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                                while (--f_42_ >= 0.0f) {
                                    method1026(anIntArray1673, aFloatArray1677, f_41_.toInt(), i, 0, f_44_.toInt(), f_43_.toInt(), f_47_, f_59_)
                                    f_43_ += f_57_
                                    f_44_ += f_56_
                                    f_47_ += f_60_
                                    f_41_ += anInt1678.toFloat()
                                }
                            }
                        }
                    }
                } else if (!(f_42_ >= this.height.toFloat())) {
                    if (f > this.height.toFloat()) f = this.height.toFloat()
                    if (f_41_ > this.height.toFloat()) f_41_ = this.height.toFloat()
                    f_48_ = f_48_ - f_59_ * f_45_ + f_59_
                    if (f < f_41_) {
                        f_44_ = f_45_
                        if (f_42_ < 0.0f) {
                            f_44_ -= f_56_ * f_42_
                            f_45_ -= f_57_ * f_42_
                            f_48_ -= f_60_ * f_42_
                            f_42_ = 0.0f
                        }
                        if (f < 0.0f) {
                            f_43_ -= f_55_ * f
                            f = 0.0f
                        }
                        if (f_56_ < f_57_) {
                            f_41_ -= f
                            f -= f_42_
                            f_42_ = (this.lineOffsets[f_42_.toInt()]).toFloat()
                            while (--f >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_44_.toInt(), f_45_.toInt(), f_48_, f_59_)
                                f_44_ += f_56_
                                f_45_ += f_57_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                            while (--f_41_ >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_44_.toInt(), f_43_.toInt(), f_48_, f_59_)
                                f_44_ += f_56_
                                f_43_ += f_55_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                        } else {
                            f_41_ -= f
                            f -= f_42_
                            f_42_ = (this.lineOffsets[f_42_.toInt()]).toFloat()
                            while (--f >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_45_.toInt(), f_44_.toInt(), f_48_, f_59_)
                                f_44_ += f_56_
                                f_45_ += f_57_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                            while (--f_41_ >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_43_.toInt(), f_44_.toInt(), f_48_, f_59_)
                                f_44_ += f_56_
                                f_43_ += f_55_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                        }
                    } else {
                        f_43_ = f_45_
                        if (f_42_ < 0.0f) {
                            f_43_ -= f_56_ * f_42_
                            f_45_ -= f_57_ * f_42_
                            f_48_ -= f_60_ * f_42_
                            f_42_ = 0.0f
                        }
                        if (f_41_ < 0.0f) {
                            f_44_ -= f_55_ * f_41_
                            f_41_ = 0.0f
                        }
                        if (f_56_ < f_57_) {
                            f -= f_41_
                            f_41_ -= f_42_
                            f_42_ = (this.lineOffsets[f_42_.toInt()]).toFloat()
                            while (--f_41_ >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_43_.toInt(), f_45_.toInt(), f_48_, f_59_)
                                f_43_ += f_56_
                                f_45_ += f_57_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                            while (--f >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_44_.toInt(), f_45_.toInt(), f_48_, f_59_)
                                f_44_ += f_55_
                                f_45_ += f_57_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                        } else {
                            f -= f_41_
                            f_41_ -= f_42_
                            f_42_ = (this.lineOffsets[f_42_.toInt()]).toFloat()
                            while (--f_41_ >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_45_.toInt(), f_43_.toInt(), f_48_, f_59_)
                                f_43_ += f_56_
                                f_45_ += f_57_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                            while (--f >= 0.0f) {
                                method1026(anIntArray1673, aFloatArray1677, f_42_.toInt(), i, 0, f_45_.toInt(), f_44_.toInt(), f_48_, f_59_)
                                f_44_ += f_55_
                                f_45_ += f_57_
                                f_48_ += f_60_
                                f_42_ += anInt1678.toFloat()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun method1019(`is`: IntArray, fs: FloatArray, i: Int, i_61_: Int, i_62_: Int, i_63_: Int, i_64_: Int, f: Float, f_65_: Float, f_66_: Float, f_67_: Float) {
        var i = i
        var i_61_ = i_61_
        var i_62_ = i_62_
        var i_63_ = i_63_
        var i_64_ = i_64_
        var f = f
        var f_65_ = f_65_
        var f_66_ = f_66_
        if (this.clamp) {
            if (i_64_ > this.width) i_64_ = this.width
            if (i_63_ < 0) i_63_ = 0
        }
        if (i_63_ < i_64_) {
            i += i_63_ - 1
            f += f_65_ * i_63_.toFloat()
            f_66_ += f_67_ * i_63_.toFloat()
            if (aJavaThreadResource_1670.aBoolean2202) {
                do {
                    if (this.aBoolean1669) {
                        i_62_ = i_64_ - i_63_ shr 2
                        f_65_ *= 4.0f
                        if (this.anInt1674 == 0) {
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                    f += f_65_
                                    if (f_66_ < fs[++i]) {
                                        `is`[i] = i_61_
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        `is`[i] = i_61_
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        `is`[i] = i_61_
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        `is`[i] = i_61_
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                } while (--i_62_ > 0)
                            }
                            i_62_ = i_64_ - i_63_ and 0x3
                            if (i_62_ > 0) {
                                i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                do {
                                    if (f_66_ < fs[++i]) {
                                        `is`[i] = i_61_
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                } while (--i_62_ > 0)
                            }
                        } else {
                            val i_68_ = this.anInt1674
                            val i_69_ = 256 - this.anInt1674
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                    f += f_65_
                                    i_61_ = (((i_61_ and 0xff00ff) * i_69_ shr 8 and 0xff00ff) + ((i_61_ and 0xff00) * i_69_ shr 8 and 0xff00))
                                    if (f_66_ < fs[++i]) {
                                        val i_70_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_70_ and 0xff00ff) * i_68_ shr 8) and 0xff00ff) + ((i_70_ and 0xff00) * i_68_ shr 8 and 0xff00))
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        val i_71_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_71_ and 0xff00ff) * i_68_ shr 8) and 0xff00ff) + ((i_71_ and 0xff00) * i_68_ shr 8 and 0xff00))
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        val i_72_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_72_ and 0xff00ff) * i_68_ shr 8) and 0xff00ff) + ((i_72_ and 0xff00) * i_68_ shr 8 and 0xff00))
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        val i_73_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_73_ and 0xff00ff) * i_68_ shr 8) and 0xff00ff) + ((i_73_ and 0xff00) * i_68_ shr 8 and 0xff00))
                                        fs[i] = f_66_
                                    }
                                    f_66_ += f_67_
                                } while (--i_62_ > 0)
                            }
                            i_62_ = i_64_ - i_63_ and 0x3
                            if (i_62_ <= 0) break
                            i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                            i_61_ = (((i_61_ and 0xff00ff) * i_69_ shr 8 and 0xff00ff) + ((i_61_ and 0xff00) * i_69_ shr 8 and 0xff00))
                            do {
                                if (f_66_ < fs[++i]) {
                                    val i_74_ = `is`[i]
                                    `is`[i] = (i_61_ + ((i_74_ and 0xff00ff) * i_68_ shr 8 and 0xff00ff) + ((i_74_ and 0xff00) * i_68_ shr 8 and 0xff00))
                                    fs[i] = f_66_
                                }
                                f_66_ += f_67_
                            } while (--i_62_ > 0)
                        }
                        break
                    }
                    i_62_ = i_64_ - i_63_
                    if (this.anInt1674 == 0) {
                        do {
                            if (f_66_ < fs[++i]) {
                                `is`[i] = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                fs[i] = f_66_
                            }
                            f_66_ += f_67_
                            f += f_65_
                        } while (--i_62_ > 0)
                        break
                    }
                    val i_75_ = this.anInt1674
                    val i_76_ = 256 - this.anInt1674
                    do {
                        if (f_66_ < fs[++i]) {
                            i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                            i_61_ = (((i_61_ and 0xff00ff) * i_76_ shr 8 and 0xff00ff) + ((i_61_ and 0xff00) * i_76_ shr 8 and 0xff00))
                            val i_77_ = `is`[i]
                            `is`[i] = (i_61_ + ((i_77_ and 0xff00ff) * i_75_ shr 8 and 0xff00ff) + ((i_77_ and 0xff00) * i_75_ shr 8 and 0xff00))
                            fs[i] = f_66_
                        }
                        f += f_65_
                        f_66_ += f_67_
                    } while (--i_62_ > 0)
                } while (false)
            } else {
                do {
                    if (this.aBoolean1669) {
                        i_62_ = i_64_ - i_63_ shr 2
                        f_65_ *= 4.0f
                        if (this.anInt1674 == 0) {
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                    f += f_65_
                                    if (f_66_ < fs[++i]) `is`[i] = i_61_
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) `is`[i] = i_61_
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) `is`[i] = i_61_
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) `is`[i] = i_61_
                                    f_66_ += f_67_
                                } while (--i_62_ > 0)
                            }
                            i_62_ = i_64_ - i_63_ and 0x3
                            if (i_62_ > 0) {
                                i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                do {
                                    if (f_66_ < fs[++i]) `is`[i] = i_61_
                                    f_66_ += f_67_
                                } while (--i_62_ > 0)
                            }
                        } else {
                            val i_78_ = this.anInt1674
                            val i_79_ = 256 - this.anInt1674
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                                    f += f_65_
                                    i_61_ = (((i_61_ and 0xff00ff) * i_79_ shr 8 and 0xff00ff) + ((i_61_ and 0xff00) * i_79_ shr 8 and 0xff00))
                                    if (f_66_ < fs[++i]) {
                                        val i_80_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_80_ and 0xff00ff) * i_78_ shr 8) and 0xff00ff) + ((i_80_ and 0xff00) * i_78_ shr 8 and 0xff00))
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        val i_81_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_81_ and 0xff00ff) * i_78_ shr 8) and 0xff00ff) + ((i_81_ and 0xff00) * i_78_ shr 8 and 0xff00))
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        val i_82_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_82_ and 0xff00ff) * i_78_ shr 8) and 0xff00ff) + ((i_82_ and 0xff00) * i_78_ shr 8 and 0xff00))
                                    }
                                    f_66_ += f_67_
                                    if (f_66_ < fs[++i]) {
                                        val i_83_ = `is`[i]
                                        `is`[i] = (i_61_ + (((i_83_ and 0xff00ff) * i_78_ shr 8) and 0xff00ff) + ((i_83_ and 0xff00) * i_78_ shr 8 and 0xff00))
                                    }
                                    f_66_ += f_67_
                                } while (--i_62_ > 0)
                            }
                            i_62_ = i_64_ - i_63_ and 0x3
                            if (i_62_ <= 0) break
                            i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                            i_61_ = (((i_61_ and 0xff00ff) * i_79_ shr 8 and 0xff00ff) + ((i_61_ and 0xff00) * i_79_ shr 8 and 0xff00))
                            do {
                                if (f_66_ < fs[++i]) {
                                    val i_84_ = `is`[i]
                                    `is`[i] = (i_61_ + ((i_84_ and 0xff00ff) * i_78_ shr 8 and 0xff00ff) + ((i_84_ and 0xff00) * i_78_ shr 8 and 0xff00))
                                }
                                f_66_ += f_67_
                            } while (--i_62_ > 0)
                        }
                        break
                    }
                    i_62_ = i_64_ - i_63_
                    if (this.anInt1674 == 0) {
                        do {
                            if (f_66_ < fs[++i]) `is`[i] = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                            f_66_ += f_67_
                            f += f_65_
                        } while (--i_62_ > 0)
                        break
                    }
                    val i_85_ = this.anInt1674
                    val i_86_ = 256 - this.anInt1674
                    do {
                        if (f_66_ < fs[++i]) {
                            i_61_ = ItemSpriteCacheKey.Companion.HSV_TO_RGB[f.toInt()]
                            i_61_ = (((i_61_ and 0xff00ff) * i_86_ shr 8 and 0xff00ff) + ((i_61_ and 0xff00) * i_86_ shr 8 and 0xff00))
                            val i_87_ = `is`[i]
                            `is`[i] = (i_61_ + ((i_87_ and 0xff00ff) * i_85_ shr 8 and 0xff00ff) + ((i_87_ and 0xff00) * i_85_ shr 8 and 0xff00))
                        }
                        f += f_65_
                        f_66_ += f_67_
                    } while (--i_62_ > 0)
                } while (false)
            }
        }
    }

    private fun method1021(`is`: IntArray, fs: FloatArray, i: Int, i_168_: Int, i_169_: Int, i_170_: Int, i_171_: Int, f: Float, f_172_: Float, f_173_: Float, f_174_: Float, f_175_: Float, f_176_: Float, f_177_: Float, f_178_: Float) {
        var i = i
        var i_168_ = i_168_
        var i_169_ = i_169_
        var i_170_ = i_170_
        var i_171_ = i_171_
        var f = f
        var f_173_ = f_173_
        var f_174_ = f_174_
        var f_175_ = f_175_
        var f_176_ = f_176_
        var f_177_ = f_177_
        var f_178_ = f_178_
        if (this.clamp) {
            if (i_171_ > this.width) i_171_ = this.width
            if (i_170_ < 0) i_170_ = 0
        }
        if (i_170_ < i_171_) {
            if (aBoolean1680) {
                i += i_170_
                f_173_ += f_174_ * i_170_.toFloat()
                f_175_ += f_176_ * i_170_.toFloat()
                f_177_ += f_178_ * i_170_.toFloat()
                if (this.aBoolean1669) {
                    i_169_ = i_171_ - i_170_ shr 2
                    f_174_ *= 4.0f
                    f_176_ *= 4.0f
                    f_178_ *= 4.0f
                    if (this.anInt1674 == 0) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                                `is`[i++] = i_168_
                                `is`[i++] = i_168_
                                `is`[i++] = i_168_
                                `is`[i++] = i_168_
                            } while (--i_169_ > 0)
                        }
                        i_169_ = i_171_ - i_170_ and 0x3
                        if (i_169_ > 0) {
                            i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            do `is`[i++] = i_168_ while (--i_169_ > 0)
                        }
                    } else if (this.aBoolean1667) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                                val is_183_ = `is`
                                val i_184_ = i++
                                val i_185_ = i_168_
                                var i_186_ = is_183_[i_184_]
                                val i_187_ = i_185_ + i_186_
                                val i_188_ = ((i_185_ and 0xff00ff) + (i_186_ and 0xff00ff))
                                i_186_ = (i_188_ and 0x1000100) + (i_187_ - i_188_ and 0x10000)
                                is_183_[i_184_] = (0xffffff.inv() or i_187_ - i_186_ or i_186_ - (i_186_ ushr 8))
                                val is_189_ = `is`
                                val i_190_ = i++
                                val i_191_ = i_168_
                                var i_192_ = is_189_[i_190_]
                                val i_193_ = i_191_ + i_192_
                                val i_194_ = ((i_191_ and 0xff00ff) + (i_192_ and 0xff00ff))
                                i_192_ = (i_194_ and 0x1000100) + (i_193_ - i_194_ and 0x10000)
                                is_189_[i_190_] = (0xffffff.inv() or i_193_ - i_192_ or i_192_ - (i_192_ ushr 8))
                                val is_195_ = `is`
                                val i_196_ = i++
                                val i_197_ = i_168_
                                var i_198_ = is_195_[i_196_]
                                val i_199_ = i_197_ + i_198_
                                val i_200_ = ((i_197_ and 0xff00ff) + (i_198_ and 0xff00ff))
                                i_198_ = (i_200_ and 0x1000100) + (i_199_ - i_200_ and 0x10000)
                                is_195_[i_196_] = (0xffffff.inv() or i_199_ - i_198_ or i_198_ - (i_198_ ushr 8))
                                val is_201_ = `is`
                                val i_202_ = i++
                                val i_203_ = i_168_
                                var i_204_ = is_201_[i_202_]
                                val i_205_ = i_203_ + i_204_
                                val i_206_ = ((i_203_ and 0xff00ff) + (i_204_ and 0xff00ff))
                                i_204_ = (i_206_ and 0x1000100) + (i_205_ - i_206_ and 0x10000)
                                is_201_[i_202_] = (0xffffff.inv() or i_205_ - i_204_ or i_204_ - (i_204_ ushr 8))
                            } while (--i_169_ > 0)
                        }
                        i_169_ = i_171_ - i_170_ and 0x3
                        if (i_169_ > 0) {
                            i_168_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            do {
                                val is_207_ = `is`
                                val i_208_ = i++
                                val i_209_ = i_168_
                                var i_210_ = is_207_[i_208_]
                                val i_211_ = i_209_ + i_210_
                                val i_212_ = ((i_209_ and 0xff00ff) + (i_210_ and 0xff00ff))
                                i_210_ = (i_212_ and 0x1000100) + (i_211_ - i_212_ and 0x10000)
                                is_207_[i_208_] = (0xffffff.inv() or i_211_ - i_210_ or i_210_ - (i_210_ ushr 8))
                            } while (--i_169_ > 0)
                        }
                    } else {
                        val i_179_ = this.anInt1674
                        val i_180_ = 256 - this.anInt1674
                        if (i_169_ > 0) {
                            do {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                                i_168_ = (((i_168_ and 0xff00ff) * i_180_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_180_ shr 8 and 0xff00))
                                var i_181_ = `is`[i]
                                `is`[i++] = (i_168_ + ((i_181_ and 0xff00ff) * i_179_ shr 8 and 0xff00ff) + ((i_181_ and 0xff00) * i_179_ shr 8 and 0xff00))
                                i_181_ = `is`[i]
                                `is`[i++] = (i_168_ + ((i_181_ and 0xff00ff) * i_179_ shr 8 and 0xff00ff) + ((i_181_ and 0xff00) * i_179_ shr 8 and 0xff00))
                                i_181_ = `is`[i]
                                `is`[i++] = (i_168_ + ((i_181_ and 0xff00ff) * i_179_ shr 8 and 0xff00ff) + ((i_181_ and 0xff00) * i_179_ shr 8 and 0xff00))
                                i_181_ = `is`[i]
                                `is`[i++] = (i_168_ + ((i_181_ and 0xff00ff) * i_179_ shr 8 and 0xff00ff) + ((i_181_ and 0xff00) * i_179_ shr 8 and 0xff00))
                            } while (--i_169_ > 0)
                        }
                        i_169_ = i_171_ - i_170_ and 0x3
                        if (i_169_ > 0) {
                            i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            i_168_ = (((i_168_ and 0xff00ff) * i_180_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_180_ shr 8 and 0xff00))
                            do {
                                val i_182_ = `is`[i]
                                `is`[i++] = (i_168_ + ((i_182_ and 0xff00ff) * i_179_ shr 8 and 0xff00ff) + ((i_182_ and 0xff00) * i_179_ shr 8 and 0xff00))
                            } while (--i_169_ > 0)
                        }
                    }
                } else {
                    i_169_ = i_171_ - i_170_
                    if (this.anInt1674 == 0) {
                        do {
                            `is`[i++] = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            f_173_ += f_174_
                            f_175_ += f_176_
                            f_177_ += f_178_
                        } while (--i_169_ > 0)
                    } else if (this.aBoolean1667) {
                        do {
                            val is_216_ = `is`
                            val i_217_ = i++
                            val i_218_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            var i_219_ = is_216_[i_217_]
                            val i_220_ = i_218_ + i_219_
                            val i_221_ = (i_218_ and 0xff00ff) + (i_219_ and 0xff00ff)
                            i_219_ = (i_221_ and 0x1000100) + (i_220_ - i_221_ and 0x10000)
                            is_216_[i_217_] = (0xffffff.inv() or i_220_ - i_219_ or i_219_ - (i_219_ ushr 8))
                            f_173_ += f_174_
                            f_175_ += f_176_
                            f_177_ += f_178_
                        } while (--i_169_ > 0)
                    } else {
                        val i_213_ = this.anInt1674
                        val i_214_ = 256 - this.anInt1674
                        do {
                            i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            f_173_ += f_174_
                            f_175_ += f_176_
                            f_177_ += f_178_
                            i_168_ = (((i_168_ and 0xff00ff) * i_214_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_214_ shr 8 and 0xff00))
                            val i_215_ = `is`[i]
                            `is`[i++] = (i_168_ + ((i_215_ and 0xff00ff) * i_213_ shr 8 and 0xff00ff) + ((i_215_ and 0xff00) * i_213_ shr 8 and 0xff00))
                        } while (--i_169_ > 0)
                    }
                }
            } else {
                i += i_170_ - 1
                f += f_172_ * i_170_.toFloat()
                f_173_ += f_174_ * i_170_.toFloat()
                f_175_ += f_176_ * i_170_.toFloat()
                f_177_ += f_178_ * i_170_.toFloat()
                if (aJavaThreadResource_1670.aBoolean2202) {
                    if (this.aBoolean1669) {
                        i_169_ = i_171_ - i_170_ shr 2
                        f_174_ *= 4.0f
                        f_176_ *= 4.0f
                        f_178_ *= 4.0f
                        if (this.anInt1674 == 0) {
                            if (i_169_ > 0) {
                                do {
                                    i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                    f_173_ += f_174_
                                    f_175_ += f_176_
                                    f_177_ += f_178_
                                    if (f < fs[++i]) {
                                        `is`[i] = i_168_
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        `is`[i] = i_168_
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        `is`[i] = i_168_
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        `is`[i] = i_168_
                                        fs[i] = f
                                    }
                                    f += f_172_
                                } while (--i_169_ > 0)
                            }
                            i_169_ = i_171_ - i_170_ and 0x3
                            if (i_169_ > 0) {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                do {
                                    if (f < fs[++i]) {
                                        `is`[i] = i_168_
                                        fs[i] = f
                                    }
                                    f += f_172_
                                } while (--i_169_ > 0)
                            }
                        } else if (this.aBoolean1667) {
                            if (i_169_ > 0) {
                                do {
                                    i_168_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                    f_173_ += f_174_
                                    f_175_ += f_176_
                                    f_177_ += f_178_
                                    if (f < fs[++i]) {
                                        val is_229_ = `is`
                                        val i_230_ = i
                                        val i_231_ = i_168_
                                        var i_232_ = is_229_[i_230_]
                                        val i_233_ = i_231_ + i_232_
                                        val i_234_ = ((i_231_ and 0xff00ff) + (i_232_ and 0xff00ff))
                                        i_232_ = ((i_234_ and 0x1000100) + (i_233_ - i_234_ and 0x10000))
                                        is_229_[i_230_] = (0xffffff.inv() or i_233_ - i_232_ or i_232_ - (i_232_ ushr 8))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        val is_235_ = `is`
                                        val i_236_ = i
                                        val i_237_ = i_168_
                                        var i_238_ = is_235_[i_236_]
                                        val i_239_ = i_237_ + i_238_
                                        val i_240_ = ((i_237_ and 0xff00ff) + (i_238_ and 0xff00ff))
                                        i_238_ = ((i_240_ and 0x1000100) + (i_239_ - i_240_ and 0x10000))
                                        is_235_[i_236_] = (0xffffff.inv() or i_239_ - i_238_ or i_238_ - (i_238_ ushr 8))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        val is_241_ = `is`
                                        val i_242_ = i
                                        val i_243_ = i_168_
                                        var i_244_ = is_241_[i_242_]
                                        val i_245_ = i_243_ + i_244_
                                        val i_246_ = ((i_243_ and 0xff00ff) + (i_244_ and 0xff00ff))
                                        i_244_ = ((i_246_ and 0x1000100) + (i_245_ - i_246_ and 0x10000))
                                        is_241_[i_242_] = (0xffffff.inv() or i_245_ - i_244_ or i_244_ - (i_244_ ushr 8))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        val is_247_ = `is`
                                        val i_248_ = i
                                        val i_249_ = i_168_
                                        var i_250_ = is_247_[i_248_]
                                        val i_251_ = i_249_ + i_250_
                                        val i_252_ = ((i_249_ and 0xff00ff) + (i_250_ and 0xff00ff))
                                        i_250_ = ((i_252_ and 0x1000100) + (i_251_ - i_252_ and 0x10000))
                                        is_247_[i_248_] = (0xffffff.inv() or i_251_ - i_250_ or i_250_ - (i_250_ ushr 8))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                } while (--i_169_ > 0)
                            }
                            i_169_ = i_171_ - i_170_ and 0x3
                            if (i_169_ > 0) {
                                i_168_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                do {
                                    if (f < fs[++i]) {
                                        val is_253_ = `is`
                                        val i_254_ = i
                                        val i_255_ = i_168_
                                        var i_256_ = is_253_[i_254_]
                                        val i_257_ = i_255_ + i_256_
                                        val i_258_ = ((i_255_ and 0xff00ff) + (i_256_ and 0xff00ff))
                                        i_256_ = ((i_258_ and 0x1000100) + (i_257_ - i_258_ and 0x10000))
                                        is_253_[i_254_] = (0xffffff.inv() or i_257_ - i_256_ or i_256_ - (i_256_ ushr 8))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                } while (--i_169_ > 0)
                            }
                        } else {
                            val i_222_ = this.anInt1674
                            val i_223_ = 256 - this.anInt1674
                            if (i_169_ > 0) {
                                do {
                                    i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                    f_173_ += f_174_
                                    f_175_ += f_176_
                                    f_177_ += f_178_
                                    i_168_ = (((i_168_ and 0xff00ff) * i_223_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_223_ shr 8 and 0xff00))
                                    if (f < fs[++i]) {
                                        val i_224_ = `is`[i]
                                        `is`[i] = (i_168_ + (((i_224_ and 0xff00ff) * i_222_ shr 8) and 0xff00ff) + (((i_224_ and 0xff00) * i_222_ shr 8) and 0xff00))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        val i_225_ = `is`[i]
                                        `is`[i] = (i_168_ + (((i_225_ and 0xff00ff) * i_222_ shr 8) and 0xff00ff) + (((i_225_ and 0xff00) * i_222_ shr 8) and 0xff00))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        val i_226_ = `is`[i]
                                        `is`[i] = (i_168_ + (((i_226_ and 0xff00ff) * i_222_ shr 8) and 0xff00ff) + (((i_226_ and 0xff00) * i_222_ shr 8) and 0xff00))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                    if (f < fs[++i]) {
                                        val i_227_ = `is`[i]
                                        `is`[i] = (i_168_ + (((i_227_ and 0xff00ff) * i_222_ shr 8) and 0xff00ff) + (((i_227_ and 0xff00) * i_222_ shr 8) and 0xff00))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                } while (--i_169_ > 0)
                            }
                            i_169_ = i_171_ - i_170_ and 0x3
                            if (i_169_ > 0) {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                i_168_ = (((i_168_ and 0xff00ff) * i_223_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_223_ shr 8 and 0xff00))
                                do {
                                    if (f < fs[++i]) {
                                        val i_228_ = `is`[i]
                                        `is`[i] = (i_168_ + (((i_228_ and 0xff00ff) * i_222_ shr 8) and 0xff00ff) + (((i_228_ and 0xff00) * i_222_ shr 8) and 0xff00))
                                        fs[i] = f
                                    }
                                    f += f_172_
                                } while (--i_169_ > 0)
                            }
                        }
                    } else {
                        i_169_ = i_171_ - i_170_
                        if (this.anInt1674 == 0) {
                            do {
                                if (f < fs[++i]) {
                                    `is`[i] = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                    fs[i] = f
                                }
                                f += f_172_
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                            } while (--i_169_ > 0)
                        } else if (this.aBoolean1667) {
                            do {
                                if (f < fs[++i]) {
                                    val is_262_ = `is`
                                    val i_263_ = i
                                    val i_264_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                    var i_265_ = is_262_[i_263_]
                                    val i_266_ = i_264_ + i_265_
                                    val i_267_ = ((i_264_ and 0xff00ff) + (i_265_ and 0xff00ff))
                                    i_265_ = ((i_267_ and 0x1000100) + (i_266_ - i_267_ and 0x10000))
                                    is_262_[i_263_] = (0xffffff.inv() or i_266_ - i_265_ or i_265_ - (i_265_ ushr 8))
                                    fs[i] = f
                                }
                                f += f_172_
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                            } while (--i_169_ > 0)
                        } else {
                            val i_259_ = this.anInt1674
                            val i_260_ = 256 - this.anInt1674
                            do {
                                if (f < fs[++i]) {
                                    i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                    i_168_ = (((i_168_ and 0xff00ff) * i_260_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_260_ shr 8 and 0xff00))
                                    val i_261_ = `is`[i]
                                    `is`[i] = (i_168_ + ((i_261_ and 0xff00ff) * i_259_ shr 8 and 0xff00ff) + ((i_261_ and 0xff00) * i_259_ shr 8 and 0xff00))
                                    fs[i] = f
                                }
                                f += f_172_
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                            } while (--i_169_ > 0)
                        }
                    }
                } else if (this.aBoolean1669) {
                    i_169_ = i_171_ - i_170_ shr 2
                    f_174_ *= 4.0f
                    f_176_ *= 4.0f
                    f_178_ *= 4.0f
                    if (this.anInt1674 == 0) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                                if (f < fs[++i]) `is`[i] = i_168_
                                f += f_172_
                                if (f < fs[++i]) `is`[i] = i_168_
                                f += f_172_
                                if (f < fs[++i]) `is`[i] = i_168_
                                f += f_172_
                                if (f < fs[++i]) `is`[i] = i_168_
                                f += f_172_
                            } while (--i_169_ > 0)
                        }
                        i_169_ = i_171_ - i_170_ and 0x3
                        if (i_169_ > 0) {
                            i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            do {
                                if (f < fs[++i]) `is`[i] = i_168_
                                f += f_172_
                            } while (--i_169_ > 0)
                        }
                    } else if (this.aBoolean1667) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                                if (f < fs[++i]) {
                                    val is_275_ = `is`
                                    val i_276_ = i
                                    val i_277_ = i_168_
                                    var i_278_ = is_275_[i_276_]
                                    val i_279_ = i_277_ + i_278_
                                    val i_280_ = ((i_277_ and 0xff00ff) + (i_278_ and 0xff00ff))
                                    i_278_ = ((i_280_ and 0x1000100) + (i_279_ - i_280_ and 0x10000))
                                    is_275_[i_276_] = (0xffffff.inv() or i_279_ - i_278_ or i_278_ - (i_278_ ushr 8))
                                }
                                f += f_172_
                                if (f < fs[++i]) {
                                    val is_281_ = `is`
                                    val i_282_ = i
                                    val i_283_ = i_168_
                                    var i_284_ = is_281_[i_282_]
                                    val i_285_ = i_283_ + i_284_
                                    val i_286_ = ((i_283_ and 0xff00ff) + (i_284_ and 0xff00ff))
                                    i_284_ = ((i_286_ and 0x1000100) + (i_285_ - i_286_ and 0x10000))
                                    is_281_[i_282_] = (0xffffff.inv() or i_285_ - i_284_ or i_284_ - (i_284_ ushr 8))
                                }
                                f += f_172_
                                if (f < fs[++i]) {
                                    val is_287_ = `is`
                                    val i_288_ = i
                                    val i_289_ = i_168_
                                    var i_290_ = is_287_[i_288_]
                                    val i_291_ = i_289_ + i_290_
                                    val i_292_ = ((i_289_ and 0xff00ff) + (i_290_ and 0xff00ff))
                                    i_290_ = ((i_292_ and 0x1000100) + (i_291_ - i_292_ and 0x10000))
                                    is_287_[i_288_] = (0xffffff.inv() or i_291_ - i_290_ or i_290_ - (i_290_ ushr 8))
                                }
                                f += f_172_
                                if (f < fs[++i]) {
                                    val is_293_ = `is`
                                    val i_294_ = i
                                    val i_295_ = i_168_
                                    var i_296_ = is_293_[i_294_]
                                    val i_297_ = i_295_ + i_296_
                                    val i_298_ = ((i_295_ and 0xff00ff) + (i_296_ and 0xff00ff))
                                    i_296_ = ((i_298_ and 0x1000100) + (i_297_ - i_298_ and 0x10000))
                                    is_293_[i_294_] = (0xffffff.inv() or i_297_ - i_296_ or i_296_ - (i_296_ ushr 8))
                                }
                                f += f_172_
                            } while (--i_169_ > 0)
                        }
                        i_169_ = i_171_ - i_170_ and 0x3
                        if (i_169_ > 0) {
                            i_168_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            do {
                                if (f < fs[++i]) {
                                    val is_299_ = `is`
                                    val i_300_ = i
                                    val i_301_ = i_168_
                                    var i_302_ = is_299_[i_300_]
                                    val i_303_ = i_301_ + i_302_
                                    val i_304_ = ((i_301_ and 0xff00ff) + (i_302_ and 0xff00ff))
                                    i_302_ = ((i_304_ and 0x1000100) + (i_303_ - i_304_ and 0x10000))
                                    is_299_[i_300_] = (0xffffff.inv() or i_303_ - i_302_ or i_302_ - (i_302_ ushr 8))
                                }
                                f += f_172_
                            } while (--i_169_ > 0)
                        }
                    } else {
                        val i_268_ = this.anInt1674
                        val i_269_ = 256 - this.anInt1674
                        if (i_169_ > 0) {
                            do {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                f_173_ += f_174_
                                f_175_ += f_176_
                                f_177_ += f_178_
                                i_168_ = (((i_168_ and 0xff00ff) * i_269_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_269_ shr 8 and 0xff00))
                                if (f < fs[++i]) {
                                    val i_270_ = `is`[i]
                                    `is`[i] = (i_168_ + ((i_270_ and 0xff00ff) * i_268_ shr 8 and 0xff00ff) + ((i_270_ and 0xff00) * i_268_ shr 8 and 0xff00))
                                }
                                f += f_172_
                                if (f < fs[++i]) {
                                    val i_271_ = `is`[i]
                                    `is`[i] = (i_168_ + ((i_271_ and 0xff00ff) * i_268_ shr 8 and 0xff00ff) + ((i_271_ and 0xff00) * i_268_ shr 8 and 0xff00))
                                }
                                f += f_172_
                                if (f < fs[++i]) {
                                    val i_272_ = `is`[i]
                                    `is`[i] = (i_168_ + ((i_272_ and 0xff00ff) * i_268_ shr 8 and 0xff00ff) + ((i_272_ and 0xff00) * i_268_ shr 8 and 0xff00))
                                }
                                f += f_172_
                                if (f < fs[++i]) {
                                    val i_273_ = `is`[i]
                                    `is`[i] = (i_168_ + ((i_273_ and 0xff00ff) * i_268_ shr 8 and 0xff00ff) + ((i_273_ and 0xff00) * i_268_ shr 8 and 0xff00))
                                }
                                f += f_172_
                            } while (--i_169_ > 0)
                        }
                        i_169_ = i_171_ - i_170_ and 0x3
                        if (i_169_ > 0) {
                            i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            i_168_ = (((i_168_ and 0xff00ff) * i_269_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_269_ shr 8 and 0xff00))
                            do {
                                if (f < fs[++i]) {
                                    val i_274_ = `is`[i]
                                    `is`[i] = (i_168_ + ((i_274_ and 0xff00ff) * i_268_ shr 8 and 0xff00ff) + ((i_274_ and 0xff00) * i_268_ shr 8 and 0xff00))
                                }
                                f += f_172_
                            } while (--i_169_ > 0)
                        }
                    }
                } else {
                    i_169_ = i_171_ - i_170_
                    if (this.anInt1674 == 0) {
                        do {
                            if (f < fs[++i]) `is`[i] = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                            f += f_172_
                            f_173_ += f_174_
                            f_175_ += f_176_
                            f_177_ += f_178_
                        } while (--i_169_ > 0)
                    } else if (this.aBoolean1667) {
                        do {
                            if (f < fs[++i]) {
                                val is_308_ = `is`
                                val i_309_ = i
                                val i_310_ = (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                var i_311_ = is_308_[i_309_]
                                val i_312_ = i_310_ + i_311_
                                val i_313_ = ((i_310_ and 0xff00ff) + (i_311_ and 0xff00ff))
                                i_311_ = (i_313_ and 0x1000100) + (i_312_ - i_313_ and 0x10000)
                                is_308_[i_309_] = (0xffffff.inv() or i_312_ - i_311_ or i_311_ - (i_311_ ushr 8))
                            }
                            f += f_172_
                            f_173_ += f_174_
                            f_175_ += f_176_
                            f_177_ += f_178_
                        } while (--i_169_ > 0)
                    } else {
                        val i_305_ = this.anInt1674
                        val i_306_ = 256 - this.anInt1674
                        do {
                            if (f < fs[++i]) {
                                i_168_ = 0xffffff.inv() or (f_173_.toInt() and 0xff0000 or (f_175_.toInt() and 0xff00) or (f_177_.toInt() and 0xff))
                                i_168_ = (((i_168_ and 0xff00ff) * i_306_ shr 8 and 0xff00ff) + ((i_168_ and 0xff00) * i_306_ shr 8 and 0xff00))
                                val i_307_ = `is`[i]
                                `is`[i] = (i_168_ + ((i_307_ and 0xff00ff) * i_305_ shr 8 and 0xff00ff) + ((i_307_ and 0xff00) * i_305_ shr 8 and 0xff00))
                            }
                            f += f_172_
                            f_173_ += f_174_
                            f_175_ += f_176_
                            f_177_ += f_178_
                        } while (--i_169_ > 0)
                    }
                }
            }
        }
    }

    fun method1022(f: Float, f_314_: Float, f_315_: Float, f_316_: Float, f_317_: Float, f_318_: Float, f_319_: Float, f_320_: Float, f_321_: Float, f_322_: Float, f_323_: Float, f_324_: Float) {
        var f = f
        var f_314_ = f_314_
        var f_315_ = f_315_
        var f_316_ = f_316_
        var f_317_ = f_317_
        var f_318_ = f_318_
        var f_319_ = f_319_
        var f_320_ = f_320_
        var f_321_ = f_321_
        var f_322_ = f_322_
        var f_323_ = f_323_
        var f_324_ = f_324_
        if (aBoolean1675) {
            aHa_Sub1_1666.line(f.toInt(), f_316_.toInt(), f_317_.toInt(), -8003, ItemSpriteCacheKey.Companion.HSV_TO_RGB[f_322_.toInt()], f_314_.toInt())
            aHa_Sub1_1666.line(f_314_.toInt(), f_317_.toInt(), f_318_.toInt(), -8003, ItemSpriteCacheKey.Companion.HSV_TO_RGB[f_322_.toInt()], f_315_.toInt())
            aHa_Sub1_1666.line(f_315_.toInt(), f_318_.toInt(), f_316_.toInt(), -8003, ItemSpriteCacheKey.Companion.HSV_TO_RGB[f_322_.toInt()], f.toInt())
        } else {
            val f_325_ = f_317_ - f_316_
            val f_326_ = f_314_ - f
            val f_327_ = f_318_ - f_316_
            val f_328_ = f_315_ - f
            val f_329_ = f_323_ - f_322_
            val f_330_ = f_324_ - f_322_
            val f_331_ = f_320_ - f_319_
            val f_332_ = f_321_ - f_319_
            val f_333_: Float
            if (f_315_ != f_314_) f_333_ = (f_318_ - f_317_) / (f_315_ - f_314_)
            else f_333_ = 0.0f
            val f_334_: Float
            if (f_314_ != f) f_334_ = f_325_ / f_326_
            else f_334_ = 0.0f
            val f_335_: Float
            if (f_315_ != f) f_335_ = f_327_ / f_328_
            else f_335_ = 0.0f
            val f_336_ = f_325_ * f_328_ - f_327_ * f_326_
            if (f_336_ != 0.0f) {
                val f_337_ = (f_329_ * f_328_ - f_330_ * f_326_) / f_336_
                val f_338_ = (f_330_ * f_325_ - f_329_ * f_327_) / f_336_
                val f_339_ = (f_331_ * f_328_ - f_332_ * f_326_) / f_336_
                val f_340_ = (f_332_ * f_325_ - f_331_ * f_327_) / f_336_
                if (f <= f_314_ && f <= f_315_) {
                    if (!(f >= this.height.toFloat())) {
                        if (f_314_ > this.height.toFloat()) f_314_ = this.height.toFloat()
                        if (f_315_ > this.height.toFloat()) f_315_ = this.height.toFloat()
                        f_322_ = f_322_ - f_337_ * f_316_ + f_337_
                        f_319_ = f_319_ - f_339_ * f_316_ + f_339_
                        if (f_314_ < f_315_) {
                            f_318_ = f_316_
                            if (f < 0.0f) {
                                f_318_ -= f_335_ * f
                                f_316_ -= f_334_ * f
                                f_322_ -= f_338_ * f
                                f_319_ -= f_340_ * f
                                f = 0.0f
                            }
                            if (f_314_ < 0.0f) {
                                f_317_ -= f_333_ * f_314_
                                f_314_ = 0.0f
                            }
                            if (f != f_314_ && f_335_ < f_334_ || f == f_314_ && f_335_ > f_333_) {
                                f_315_ -= f_314_
                                f_314_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_314_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_318_.toInt(), f_316_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_318_ += f_335_
                                    f_316_ += f_334_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_318_.toInt(), f_317_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_318_ += f_335_
                                    f_317_ += f_333_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                            } else {
                                f_315_ -= f_314_
                                f_314_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_314_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_316_.toInt(), f_318_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_318_ += f_335_
                                    f_316_ += f_334_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_317_.toInt(), f_318_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_318_ += f_335_
                                    f_317_ += f_333_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                            }
                        } else {
                            f_317_ = f_316_
                            if (f < 0.0f) {
                                f_317_ -= f_335_ * f
                                f_316_ -= f_334_ * f
                                f_322_ -= f_338_ * f
                                f_319_ -= f_340_ * f
                                f = 0.0f
                            }
                            if (f_315_ < 0.0f) {
                                f_318_ -= f_333_ * f_315_
                                f_315_ = 0.0f
                            }
                            if (f != f_315_ && f_335_ < f_334_ || f == f_315_ && f_333_ > f_334_) {
                                f_314_ -= f_315_
                                f_315_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_317_.toInt(), f_316_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_317_ += f_335_
                                    f_316_ += f_334_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_314_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_318_.toInt(), f_316_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_318_ += f_333_
                                    f_316_ += f_334_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                            } else {
                                f_314_ -= f_315_
                                f_315_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_316_.toInt(), f_317_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_317_ += f_335_
                                    f_316_ += f_334_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_314_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_316_.toInt(), f_318_.toInt(), f_322_, f_337_, f_319_, f_339_)
                                    f_318_ += f_333_
                                    f_316_ += f_334_
                                    f_322_ += f_338_
                                    f_319_ += f_340_
                                    f += anInt1678.toFloat()
                                }
                            }
                        }
                    }
                } else if (f_314_ <= f_315_) {
                    if (!(f_314_ >= this.height.toFloat())) {
                        if (f_315_ > this.height.toFloat()) f_315_ = this.height.toFloat()
                        if (f > this.height.toFloat()) f = this.height.toFloat()
                        f_323_ = f_323_ - f_337_ * f_317_ + f_337_
                        f_320_ = f_320_ - f_339_ * f_317_ + f_339_
                        if (f_315_ < f) {
                            f_316_ = f_317_
                            if (f_314_ < 0.0f) {
                                f_316_ -= f_334_ * f_314_
                                f_317_ -= f_333_ * f_314_
                                f_323_ -= f_338_ * f_314_
                                f_320_ -= f_340_ * f_314_
                                f_314_ = 0.0f
                            }
                            if (f_315_ < 0.0f) {
                                f_318_ -= f_335_ * f_315_
                                f_315_ = 0.0f
                            }
                            if (f_314_ != f_315_ && f_334_ < f_333_ || f_314_ == f_315_ && f_334_ > f_335_) {
                                f -= f_315_
                                f_315_ -= f_314_
                                f_314_ = (this.lineOffsets[f_314_.toInt()]).toFloat()
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_316_.toInt(), f_317_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_316_ += f_334_
                                    f_317_ += f_333_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                                while (--f >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_316_.toInt(), f_318_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_316_ += f_334_
                                    f_318_ += f_335_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                            } else {
                                f -= f_315_
                                f_315_ -= f_314_
                                f_314_ = (this.lineOffsets[f_314_.toInt()]).toFloat()
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_317_.toInt(), f_316_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_316_ += f_334_
                                    f_317_ += f_333_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                                while (--f >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_318_.toInt(), f_316_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_316_ += f_334_
                                    f_318_ += f_335_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                            }
                        } else {
                            f_318_ = f_317_
                            if (f_314_ < 0.0f) {
                                f_318_ -= f_334_ * f_314_
                                f_317_ -= f_333_ * f_314_
                                f_323_ -= f_338_ * f_314_
                                f_320_ -= f_340_ * f_314_
                                f_314_ = 0.0f
                            }
                            if (f < 0.0f) {
                                f_316_ -= f_335_ * f
                                f = 0.0f
                            }
                            if (f_334_ < f_333_) {
                                f_315_ -= f
                                f -= f_314_
                                f_314_ = (this.lineOffsets[f_314_.toInt()]).toFloat()
                                while (--f >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_318_.toInt(), f_317_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_318_ += f_334_
                                    f_317_ += f_333_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_316_.toInt(), f_317_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_316_ += f_335_
                                    f_317_ += f_333_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                            } else {
                                f_315_ -= f
                                f -= f_314_
                                f_314_ = (this.lineOffsets[f_314_.toInt()]).toFloat()
                                while (--f >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_317_.toInt(), f_318_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_318_ += f_334_
                                    f_317_ += f_333_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                                while (--f_315_ >= 0.0f) {
                                    method1019(anIntArray1673, aFloatArray1677, f_314_.toInt(), 0, 0, f_317_.toInt(), f_316_.toInt(), f_323_, f_337_, f_320_, f_339_)
                                    f_316_ += f_335_
                                    f_317_ += f_333_
                                    f_323_ += f_338_
                                    f_320_ += f_340_
                                    f_314_ += anInt1678.toFloat()
                                }
                            }
                        }
                    }
                } else if (!(f_315_ >= this.height.toFloat())) {
                    if (f > this.height.toFloat()) f = this.height.toFloat()
                    if (f_314_ > this.height.toFloat()) f_314_ = this.height.toFloat()
                    f_324_ = f_324_ - f_337_ * f_318_ + f_337_
                    f_321_ = f_321_ - f_339_ * f_318_ + f_339_
                    if (f < f_314_) {
                        f_317_ = f_318_
                        if (f_315_ < 0.0f) {
                            f_317_ -= f_333_ * f_315_
                            f_318_ -= f_335_ * f_315_
                            f_324_ -= f_338_ * f_315_
                            f_321_ -= f_340_ * f_315_
                            f_315_ = 0.0f
                        }
                        if (f < 0.0f) {
                            f_316_ -= f_334_ * f
                            f = 0.0f
                        }
                        if (f_333_ < f_335_) {
                            f_314_ -= f
                            f -= f_315_
                            f_315_ = (this.lineOffsets[f_315_.toInt()]).toFloat()
                            while (--f >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_317_.toInt(), f_318_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_317_ += f_333_
                                f_318_ += f_335_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                            while (--f_314_ >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_317_.toInt(), f_316_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_317_ += f_333_
                                f_316_ += f_334_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                        } else {
                            f_314_ -= f
                            f -= f_315_
                            f_315_ = (this.lineOffsets[f_315_.toInt()]).toFloat()
                            while (--f >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_318_.toInt(), f_317_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_317_ += f_333_
                                f_318_ += f_335_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                            while (--f_314_ >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_316_.toInt(), f_317_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_317_ += f_333_
                                f_316_ += f_334_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                        }
                    } else {
                        f_316_ = f_318_
                        if (f_315_ < 0.0f) {
                            f_316_ -= f_333_ * f_315_
                            f_318_ -= f_335_ * f_315_
                            f_324_ -= f_338_ * f_315_
                            f_321_ -= f_340_ * f_315_
                            f_315_ = 0.0f
                        }
                        if (f_314_ < 0.0f) {
                            f_317_ -= f_334_ * f_314_
                            f_314_ = 0.0f
                        }
                        if (f_333_ < f_335_) {
                            f -= f_314_
                            f_314_ -= f_315_
                            f_315_ = (this.lineOffsets[f_315_.toInt()]).toFloat()
                            while (--f_314_ >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_316_.toInt(), f_318_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_316_ += f_333_
                                f_318_ += f_335_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                            while (--f >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_317_.toInt(), f_318_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_317_ += f_334_
                                f_318_ += f_335_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                        } else {
                            f -= f_314_
                            f_314_ -= f_315_
                            f_315_ = (this.lineOffsets[f_315_.toInt()]).toFloat()
                            while (--f_314_ >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_318_.toInt(), f_316_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_316_ += f_333_
                                f_318_ += f_335_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                            while (--f >= 0.0f) {
                                method1019(anIntArray1673, aFloatArray1677, f_315_.toInt(), 0, 0, f_318_.toInt(), f_317_.toInt(), f_324_, f_337_, f_321_, f_339_)
                                f_317_ += f_334_
                                f_318_ += f_335_
                                f_324_ += f_338_
                                f_321_ += f_340_
                                f_315_ += anInt1678.toFloat()
                            }
                        }
                    }
                }
            }
        }
    }

    fun method1023(bool: Boolean) {
        aBoolean1675 = bool
    }

    /* NOTE: originally calls method1027(...) behind "if (anIntArray1698 ==
     * null)" (a texture-lookup-miss branch). method1027 is not in the
     * genuine-methods list (never observed covered), so it is treated as
     * dead code and omitted. Flagged for a follow-up compile-fix pass. */
    fun method1024(f: Float, f_341_: Float, f_342_: Float, f_343_: Float, f_344_: Float, f_345_: Float, f_346_: Float, f_347_: Float, f_348_: Float, f_349_: Float, f_350_: Float, f_351_: Float, f_352_: Float, f_353_: Float, f_354_: Float, i: Int, i_355_: Int, i_356_: Int, i_357_: Int, i_358_: Int, i_359_: Int, i_360_: Int, i_361_: Int) {
        var f = f
        var f_341_ = f_341_
        var f_342_ = f_342_
        var f_343_ = f_343_
        var f_344_ = f_344_
        var f_345_ = f_345_
        var f_346_ = f_346_
        var f_347_ = f_347_
        var f_348_ = f_348_
        var f_349_ = f_349_
        var f_350_ = f_350_
        var f_351_ = f_351_
        var f_352_ = f_352_
        var f_353_ = f_353_
        var f_354_ = f_354_
        var i_358_ = i_358_
        var i_359_ = i_359_
        var i_360_ = i_360_
        if (i_361_ != anInt1697) {
            anIntArray1698 = aHa_Sub1_1666.method3719(i_361_)
            if (anIntArray1698 == null) {
                method1027(
                    f.toInt().toFloat(),
                    f_341_.toInt().toFloat(),
                    f_342_.toInt().toFloat(),
                    f_343_.toInt().toFloat(),
                    f_344_.toInt().toFloat(),
                    f_345_.toInt().toFloat(),
                    f_346_.toInt().toFloat(),
                    f_347_.toInt().toFloat(),
                    f_348_.toInt().toFloat(),
                    JavaBillboardFace.Companion.method206(i, i_357_ or (i_358_ shl 24), 255),
                    JavaBillboardFace.Companion.method206(i_355_, i_357_ or (i_359_ shl 24), 255),
                    JavaBillboardFace.Companion.method206(i_356_, i_357_ or (i_360_ shl 24), 255)
                )
                return
            }
            anInt1693 = (if (aHa_Sub1_1666.method3727(i_361_)) 64 else aHa_Sub1_1666.anInt7501)
            anInt1690 = anInt1693 - 1
            anInt1683 = aHa_Sub1_1666.method3726(i_361_)
            aBoolean1694 = aHa_Sub1_1666.method3714(i_361_)
        }
        anInt1696 = i_357_
        var f_362_ = (i shr 24 and 0xff).toFloat()
        var f_363_ = (i_355_ shr 24 and 0xff).toFloat()
        var f_364_ = (i_356_ shr 24 and 0xff).toFloat()
        var f_365_ = (i shr 16 and 0xff).toFloat()
        var f_366_ = (i_355_ shr 16 and 0xff).toFloat()
        var f_367_ = (i_356_ shr 16 and 0xff).toFloat()
        var f_368_ = (i shr 8 and 0xff).toFloat()
        var f_369_ = (i_355_ shr 8 and 0xff).toFloat()
        var f_370_ = (i_356_ shr 8 and 0xff).toFloat()
        var f_371_ = (i and 0xff).toFloat()
        var f_372_ = (i_355_ and 0xff).toFloat()
        var f_373_ = (i_356_ and 0xff).toFloat()
        f_349_ /= f_346_
        f_350_ /= f_347_
        f_351_ /= f_348_
        f_352_ /= f_346_
        f_353_ /= f_347_
        f_354_ /= f_348_
        f_346_ = 1.0f / f_346_
        f_347_ = 1.0f / f_347_
        f_348_ = 1.0f / f_348_
        var f_374_ = 0.0f
        var f_375_ = 0.0f
        var f_376_ = 0.0f
        var f_377_ = 0.0f
        var f_378_ = 0.0f
        var f_379_ = 0.0f
        var f_380_ = 0.0f
        var f_381_ = 0.0f
        var f_382_ = 0.0f
        if (f_341_ != f) {
            val f_383_ = f_341_ - f
            f_374_ = (f_344_ - f_343_) / f_383_
            f_375_ = (f_347_ - f_346_) / f_383_
            f_376_ = (f_350_ - f_349_) / f_383_
            f_377_ = (f_353_ - f_352_) / f_383_
            f_378_ = (i_359_ - i_358_).toFloat() / f_383_
            f_379_ = (f_363_ - f_362_) / f_383_
            f_380_ = (f_366_ - f_365_) / f_383_
            f_381_ = (f_369_ - f_368_) / f_383_
            f_382_ = (f_372_ - f_371_) / f_383_
        }
        var f_384_ = 0.0f
        var f_385_ = 0.0f
        var f_386_ = 0.0f
        var f_387_ = 0.0f
        var f_388_ = 0.0f
        var f_389_ = 0.0f
        var f_390_ = 0.0f
        var f_391_ = 0.0f
        var f_392_ = 0.0f
        if (f_342_ != f_341_) {
            val f_393_ = f_342_ - f_341_
            f_384_ = (f_345_ - f_344_) / f_393_
            f_385_ = (f_348_ - f_347_) / f_393_
            f_386_ = (f_351_ - f_350_) / f_393_
            f_387_ = (f_354_ - f_353_) / f_393_
            f_388_ = (i_360_ - i_359_).toFloat() / f_393_
            f_389_ = (f_364_ - f_363_) / f_393_
            f_390_ = (f_367_ - f_366_) / f_393_
            f_391_ = (f_370_ - f_369_) / f_393_
            f_392_ = (f_373_ - f_372_) / f_393_
        }
        var f_394_ = 0.0f
        var f_395_ = 0.0f
        var f_396_ = 0.0f
        var f_397_ = 0.0f
        var f_398_ = 0.0f
        var f_399_ = 0.0f
        var f_400_ = 0.0f
        var f_401_ = 0.0f
        var f_402_ = 0.0f
        if (f != f_342_) {
            val f_403_ = f - f_342_
            f_394_ = (f_343_ - f_345_) / f_403_
            f_395_ = (f_346_ - f_348_) / f_403_
            f_396_ = (f_349_ - f_351_) / f_403_
            f_397_ = (f_352_ - f_354_) / f_403_
            f_398_ = (i_358_ - i_360_).toFloat() / f_403_
            f_399_ = (f_362_ - f_364_) / f_403_
            f_400_ = (f_365_ - f_367_) / f_403_
            f_401_ = (f_368_ - f_370_) / f_403_
            f_402_ = (f_371_ - f_373_) / f_403_
        }
        if (f <= f_341_ && f <= f_342_) {
            if (!(f >= this.height.toFloat())) {
                if (f_341_ > this.height.toFloat()) f_341_ = this.height.toFloat()
                if (f_342_ > this.height.toFloat()) f_342_ = this.height.toFloat()
                if (f_341_ < f_342_) {
                    f_345_ = f_343_
                    f_348_ = f_346_
                    f_351_ = f_349_
                    f_354_ = f_352_
                    i_360_ = i_358_
                    f_364_ = f_362_
                    f_367_ = f_365_
                    f_370_ = f_368_
                    f_373_ = f_371_
                    if (f < 0.0f) {
                        f_343_ -= f_374_ * f
                        f_345_ -= f_394_ * f
                        f_346_ -= f_375_ * f
                        f_348_ -= f_395_ * f
                        f_349_ -= f_376_ * f
                        f_351_ -= f_396_ * f
                        f_352_ -= f_377_ * f
                        f_354_ -= f_397_ * f
                        i_358_ = (i_358_ - f_378_ * f).toInt()
                        i_360_ = (i_360_ - f_398_ * f).toInt()
                        f_362_ -= f_379_ * f
                        f_364_ -= f_399_ * f
                        f_365_ -= f_379_ * f
                        f_367_ -= f_399_ * f
                        f_368_ -= f_379_ * f
                        f_370_ -= f_399_ * f
                        f_371_ -= f_379_ * f
                        f_373_ -= f_399_ * f
                        f = 0.0f
                    }
                    if (f_341_ < 0.0f) {
                        f_344_ -= f_384_ * f_341_
                        f_347_ -= f_385_ * f_341_
                        f_350_ -= f_386_ * f_341_
                        f_353_ -= f_387_ * f_341_
                        i_359_ = (i_359_ - f_388_ * f_341_).toInt()
                        f_363_ -= f_389_ * f_341_
                        f_366_ -= f_390_ * f_341_
                        f_369_ -= f_391_ * f_341_
                        f_372_ -= f_392_ * f_341_
                        f_341_ = 0.0f
                    }
                    if (f != f_341_ && f_394_ < f_374_ || f == f_341_ && f_394_ > f_384_) {
                        f_342_ -= f_341_
                        f_341_ -= f
                        f = this.lineOffsets[f.toInt()].toFloat()
                        while (--f_341_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_345_.toInt(), f_343_.toInt(), f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, i_360_.toFloat(), i_358_.toFloat(), f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_)
                            f_343_ += f_374_
                            f_345_ += f_394_
                            f_346_ += f_375_
                            f_348_ += f_395_
                            f_349_ += f_376_
                            f_351_ += f_396_
                            f_352_ += f_377_
                            f_354_ += f_397_
                            i_358_ = (i_358_ + f_378_).toInt()
                            i_360_ = (i_360_ + f_398_).toInt()
                            f_362_ += f_379_
                            f_364_ += f_399_
                            f_365_ += f_380_
                            f_367_ += f_400_
                            f_368_ += f_381_
                            f_370_ += f_401_
                            f_371_ += f_382_
                            f_373_ += f_402_
                            f += anInt1678.toFloat()
                        }
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_345_.toInt(), f_344_.toInt(), f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, i_360_.toFloat(), i_359_.toFloat(), f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_)
                            f_344_ += f_384_
                            f_345_ += f_394_
                            f_347_ += f_385_
                            f_348_ += f_395_
                            f_350_ += f_386_
                            f_351_ += f_396_
                            f_353_ += f_387_
                            f_354_ += f_397_
                            i_359_ = (i_359_ + f_388_).toInt()
                            i_360_ = (i_360_ + f_398_).toInt()
                            f_363_ += f_389_
                            f_364_ += f_399_
                            f_366_ += f_390_
                            f_367_ += f_400_
                            f_369_ += f_391_
                            f_370_ += f_401_
                            f_372_ += f_392_
                            f_373_ += f_402_
                            f += anInt1678.toFloat()
                        }
                    } else {
                        f_342_ -= f_341_
                        f_341_ -= f
                        f = this.lineOffsets[f.toInt()].toFloat()
                        while (--f_341_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_343_.toInt(), f_345_.toInt(), f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, i_358_.toFloat(), i_360_.toFloat(), f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_)
                            f_343_ += f_374_
                            f_345_ += f_394_
                            f_346_ += f_375_
                            f_348_ += f_395_
                            f_349_ += f_376_
                            f_351_ += f_396_
                            f_352_ += f_377_
                            f_354_ += f_397_
                            i_358_ = (i_358_ + f_378_).toInt()
                            i_360_ = (i_360_ + f_398_).toInt()
                            f_362_ += f_379_
                            f_364_ += f_399_
                            f_365_ += f_380_
                            f_367_ += f_400_
                            f_368_ += f_381_
                            f_370_ += f_401_
                            f_371_ += f_382_
                            f_373_ += f_402_
                            f += anInt1678.toFloat()
                        }
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_344_.toInt(), f_345_.toInt(), f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, i_359_.toFloat(), i_360_.toFloat(), f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_)
                            f_344_ += f_384_
                            f_345_ += f_394_
                            f_347_ += f_385_
                            f_348_ += f_395_
                            f_350_ += f_386_
                            f_351_ += f_396_
                            f_353_ += f_387_
                            f_354_ += f_397_
                            i_359_ = (i_359_ + f_388_).toInt()
                            i_360_ = (i_360_ + f_398_).toInt()
                            f_363_ += f_389_
                            f_364_ += f_399_
                            f_366_ += f_390_
                            f_367_ += f_400_
                            f_369_ += f_391_
                            f_370_ += f_401_
                            f_372_ += f_392_
                            f_373_ += f_402_
                            f += anInt1678.toFloat()
                        }
                    }
                } else {
                    f_344_ = f_343_
                    f_347_ = f_346_
                    f_350_ = f_349_
                    f_353_ = f_352_
                    i_359_ = i_358_
                    f_363_ = f_362_
                    f_366_ = f_365_
                    f_369_ = f_368_
                    f_372_ = f_371_
                    if (f < 0.0f) {
                        f_343_ -= f_374_ * f
                        f_344_ -= f_394_ * f
                        f_346_ -= f_375_ * f
                        f_347_ -= f_395_ * f
                        f_349_ -= f_376_ * f
                        f_350_ -= f_396_ * f
                        f_352_ -= f_377_ * f
                        f_353_ -= f_397_ * f
                        i_358_ = (i_358_ - f_378_ * f).toInt()
                        i_359_ = (i_359_ - f_398_ * f).toInt()
                        f_362_ -= f_379_ * f
                        f_363_ -= f_399_ * f
                        f_365_ -= f_379_ * f
                        f_366_ -= f_399_ * f
                        f_368_ -= f_379_ * f
                        f_369_ -= f_399_ * f
                        f_371_ -= f_379_ * f
                        f_372_ -= f_399_ * f
                        f = 0.0f
                    }
                    if (f_342_ < 0.0f) {
                        f_345_ -= f_384_ * f_342_
                        f_348_ -= f_385_ * f_342_
                        f_351_ -= f_386_ * f_342_
                        f_354_ -= f_387_ * f_342_
                        i_360_ = (i_360_ - f_388_ * f_342_).toInt()
                        f_364_ -= f_389_ * f_342_
                        f_367_ -= f_390_ * f_342_
                        f_370_ -= f_391_ * f_342_
                        f_373_ -= f_392_ * f_342_
                        f_342_ = 0.0f
                    }
                    if (f != f_342_ && f_394_ < f_374_ || f == f_342_ && f_384_ > f_374_) {
                        f_341_ -= f_342_
                        f_342_ -= f
                        f = this.lineOffsets[f.toInt()].toFloat()
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_344_.toInt(), f_343_.toInt(), f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, i_359_.toFloat(), i_358_.toFloat(), f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_)
                            f_343_ += f_374_
                            f_344_ += f_394_
                            f_346_ += f_375_
                            f_347_ += f_395_
                            f_349_ += f_376_
                            f_350_ += f_396_
                            f_352_ += f_377_
                            f_353_ += f_397_
                            i_358_ = (i_358_ + f_378_).toInt()
                            i_359_ = (i_359_ + f_398_).toInt()
                            f_362_ += f_379_
                            f_363_ += f_399_
                            f_365_ += f_380_
                            f_366_ += f_400_
                            f_368_ += f_381_
                            f_369_ += f_401_
                            f_371_ += f_382_
                            f_372_ += f_402_
                            f += anInt1678.toFloat()
                        }
                        while (--f_341_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_345_.toInt(), f_343_.toInt(), f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, i_360_.toFloat(), i_358_.toFloat(), f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_)
                            f_345_ += f_384_
                            f_343_ += f_374_
                            f_348_ += f_385_
                            f_346_ += f_375_
                            f_351_ += f_386_
                            f_349_ += f_376_
                            f_354_ += f_387_
                            f_352_ += f_377_
                            i_360_ = (i_360_ + f_388_).toInt()
                            i_358_ = (i_358_ + f_378_).toInt()
                            f_364_ += f_389_
                            f_362_ += f_379_
                            f_367_ += f_390_
                            f_365_ += f_380_
                            f_370_ += f_391_
                            f_368_ += f_381_
                            f_373_ += f_392_
                            f_371_ += f_382_
                            f += anInt1678.toFloat()
                        }
                    } else {
                        f_341_ -= f_342_
                        f_342_ -= f
                        f = this.lineOffsets[f.toInt()].toFloat()
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_343_.toInt(), f_344_.toInt(), f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, i_358_.toFloat(), i_359_.toFloat(), f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_)
                            f_344_ += f_394_
                            f_343_ += f_374_
                            f_347_ += f_395_
                            f_346_ += f_375_
                            f_350_ += f_396_
                            f_349_ += f_376_
                            f_353_ += f_397_
                            f_352_ += f_377_
                            i_359_ = (i_359_ + f_398_).toInt()
                            i_358_ = (i_358_ + f_378_).toInt()
                            f_363_ += f_399_
                            f_362_ += f_379_
                            f_366_ += f_400_
                            f_365_ += f_380_
                            f_369_ += f_401_
                            f_368_ += f_381_
                            f_372_ += f_402_
                            f_371_ += f_382_
                            f += anInt1678.toFloat()
                        }
                        while (--f_341_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f.toInt(), f_343_.toInt(), f_345_.toInt(), f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, i_358_.toFloat(), i_360_.toFloat(), f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_)
                            f_343_ += f_374_
                            f_345_ += f_384_
                            f_346_ += f_375_
                            f_348_ += f_385_
                            f_349_ += f_376_
                            f_351_ += f_386_
                            f_352_ += f_377_
                            f_354_ += f_397_
                            i_358_ = (i_358_ + f_378_).toInt()
                            i_360_ = (i_360_ + f_388_).toInt()
                            f_362_ += f_379_
                            f_364_ += f_389_
                            f_365_ += f_380_
                            f_367_ += f_390_
                            f_368_ += f_381_
                            f_370_ += f_391_
                            f_371_ += f_382_
                            f_373_ += f_392_
                            f += anInt1678.toFloat()
                        }
                    }
                }
            }
        } else if (f_341_ <= f_342_) {
            if (!(f_341_ >= this.height.toFloat())) {
                if (f_342_ > this.height.toFloat()) f_342_ = this.height.toFloat()
                if (f > this.height.toFloat()) f = this.height.toFloat()
                if (f_342_ < f) {
                    f_343_ = f_344_
                    f_346_ = f_347_
                    f_349_ = f_350_
                    f_352_ = f_353_
                    i_358_ = i_359_
                    f_362_ = f_363_
                    f_365_ = f_366_
                    f_368_ = f_369_
                    f_371_ = f_372_
                    if (f_341_ < 0.0f) {
                        f_343_ -= f_374_ * f_341_
                        f_344_ -= f_384_ * f_341_
                        f_346_ -= f_375_ * f_341_
                        f_347_ -= f_385_ * f_341_
                        f_349_ -= f_376_ * f_341_
                        f_350_ -= f_386_ * f_341_
                        f_352_ -= f_377_ * f_341_
                        f_353_ -= f_387_ * f_341_
                        i_358_ = (i_358_ - f_378_ * f_341_).toInt()
                        i_359_ = (i_359_ - f_388_ * f_341_).toInt()
                        f_362_ -= f_379_ * f_341_
                        f_363_ -= f_389_ * f_341_
                        f_365_ -= f_380_ * f_341_
                        f_366_ -= f_390_ * f_341_
                        f_368_ -= f_381_ * f_341_
                        f_369_ -= f_391_ * f_341_
                        f_371_ -= f_382_ * f_341_
                        f_372_ -= f_392_ * f_341_
                        f_341_ = 0.0f
                    }
                    if (f_342_ < 0.0f) {
                        f_345_ -= f_394_ * f_342_
                        f_348_ -= f_395_ * f_342_
                        f_351_ -= f_396_ * f_342_
                        f_354_ -= f_397_ * f_342_
                        i_360_ = (i_360_ - f_398_ * f_342_).toInt()
                        f_364_ -= f_399_ * f_342_
                        f_367_ -= f_400_ * f_342_
                        f_370_ -= f_401_ * f_342_
                        f_373_ -= f_402_ * f_342_
                        f_342_ = 0.0f
                    }
                    if (f_341_ != f_342_ && f_374_ < f_384_ || f_341_ == f_342_ && f_374_ > f_394_) {
                        f -= f_342_
                        f_342_ -= f_341_
                        f_341_ = (this.lineOffsets[f_341_.toInt()]).toFloat()
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_343_.toInt(), f_344_.toInt(), f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, i_358_.toFloat(), i_359_.toFloat(), f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_)
                            f_343_ += f_374_
                            f_344_ += f_384_
                            f_346_ += f_375_
                            f_347_ += f_385_
                            f_349_ += f_376_
                            f_350_ += f_386_
                            f_352_ += f_377_
                            f_353_ += f_387_
                            i_358_ = (i_358_ + f_378_).toInt()
                            i_359_ = (i_359_ + f_388_).toInt()
                            f_362_ += f_379_
                            f_363_ += f_389_
                            f_365_ += f_380_
                            f_366_ += f_390_
                            f_368_ += f_381_
                            f_369_ += f_391_
                            f_371_ += f_382_
                            f_372_ += f_392_
                            f_341_ += anInt1678.toFloat()
                        }
                        while (--f >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_343_.toInt(), f_345_.toInt(), f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, i_358_.toFloat(), i_360_.toFloat(), f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_)
                            f_343_ += f_374_
                            f_345_ += f_394_
                            f_346_ += f_375_
                            f_348_ += f_395_
                            f_349_ += f_376_
                            f_351_ += f_396_
                            f_352_ += f_377_
                            f_354_ += f_397_
                            i_358_ = (i_358_ + f_378_).toInt()
                            i_360_ = (i_360_ + f_398_).toInt()
                            f_362_ += f_379_
                            f_364_ += f_399_
                            f_365_ += f_380_
                            f_367_ += f_400_
                            f_368_ += f_381_
                            f_370_ += f_401_
                            f_371_ += f_382_
                            f_373_ += f_402_
                            f_341_ += anInt1678.toFloat()
                        }
                    } else {
                        f -= f_342_
                        f_342_ -= f_341_
                        f_341_ = (this.lineOffsets[f_341_.toInt()]).toFloat()
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_344_.toInt(), f_343_.toInt(), f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, i_359_.toFloat(), i_358_.toFloat(), f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_)
                            f_344_ += f_384_
                            f_343_ += f_374_
                            f_347_ += f_385_
                            f_346_ += f_375_
                            f_350_ += f_386_
                            f_349_ += f_376_
                            f_353_ += f_387_
                            f_352_ += f_377_
                            i_359_ = (i_359_ + f_388_).toInt()
                            i_358_ = (i_358_ + f_378_).toInt()
                            f_363_ += f_389_
                            f_362_ += f_379_
                            f_366_ += f_390_
                            f_365_ += f_380_
                            f_369_ += f_391_
                            f_368_ += f_381_
                            f_372_ += f_392_
                            f_371_ += f_382_
                            f_341_ += anInt1678.toFloat()
                        }
                        while (--f >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_345_.toInt(), f_343_.toInt(), f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, i_360_.toFloat(), i_358_.toFloat(), f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_)
                            f_345_ += f_394_
                            f_343_ += f_374_
                            f_348_ += f_395_
                            f_346_ += f_375_
                            f_351_ += f_396_
                            f_349_ += f_376_
                            f_354_ += f_397_
                            f_352_ += f_377_
                            i_360_ = (i_360_ + f_398_).toInt()
                            i_358_ = (i_358_ + f_378_).toInt()
                            f_364_ += f_399_
                            f_362_ += f_379_
                            f_367_ += f_400_
                            f_365_ += f_380_
                            f_370_ += f_401_
                            f_368_ += f_381_
                            f_373_ += f_402_
                            f_371_ += f_382_
                            f_341_ += anInt1678.toFloat()
                        }
                    }
                } else {
                    f_345_ = f_344_
                    f_348_ = f_347_
                    f_351_ = f_350_
                    f_354_ = f_353_
                    i_360_ = i_359_
                    f_364_ = f_363_
                    f_367_ = f_366_
                    f_370_ = f_369_
                    f_373_ = f_372_
                    if (f_341_ < 0.0f) {
                        f_345_ -= f_374_ * f_341_
                        f_344_ -= f_384_ * f_341_
                        f_348_ -= f_375_ * f_341_
                        f_347_ -= f_385_ * f_341_
                        f_351_ -= f_376_ * f_341_
                        f_350_ -= f_386_ * f_341_
                        f_354_ -= f_377_ * f_341_
                        f_353_ -= f_387_ * f_341_
                        i_360_ = (i_360_ - f_378_ * f_341_).toInt()
                        i_359_ = (i_359_ - f_388_ * f_341_).toInt()
                        f_364_ -= f_379_ * f_341_
                        f_363_ -= f_389_ * f_341_
                        f_367_ -= f_380_ * f_341_
                        f_366_ -= f_390_ * f_341_
                        f_370_ -= f_381_ * f_341_
                        f_369_ -= f_391_ * f_341_
                        f_373_ -= f_382_ * f_341_
                        f_372_ -= f_392_ * f_341_
                        f_341_ = 0.0f
                    }
                    if (f < 0.0f) {
                        f_343_ -= f_394_ * f
                        f_346_ -= f_395_ * f
                        f_349_ -= f_396_ * f
                        f_352_ -= f_397_ * f
                        i_358_ = (i_358_ - f_398_ * f).toInt()
                        f_362_ -= f_399_ * f
                        f_365_ -= f_400_ * f
                        f_368_ -= f_401_ * f
                        f_371_ -= f_402_ * f
                        f = 0.0f
                    }
                    f_342_ -= f
                    f -= f_341_
                    f_341_ = (this.lineOffsets[f_341_.toInt()]).toFloat()
                    if (f_374_ < f_384_) {
                        while (--f >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_345_.toInt(), f_344_.toInt(), f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, i_360_.toFloat(), i_359_.toFloat(), f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_)
                            f_345_ += f_374_
                            f_344_ += f_384_
                            f_348_ += f_375_
                            f_347_ += f_385_
                            f_351_ += f_376_
                            f_350_ += f_386_
                            f_354_ += f_377_
                            f_353_ += f_387_
                            i_360_ = (i_360_ + f_378_).toInt()
                            i_359_ = (i_359_ + f_388_).toInt()
                            f_364_ += f_379_
                            f_363_ += f_389_
                            f_367_ += f_380_
                            f_366_ += f_390_
                            f_370_ += f_381_
                            f_369_ += f_391_
                            f_373_ += f_382_
                            f_372_ += f_392_
                            f_341_ += anInt1678.toFloat()
                        }
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_343_.toInt(), f_344_.toInt(), f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, i_358_.toFloat(), i_359_.toFloat(), f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_)
                            f_343_ += f_394_
                            f_344_ += f_384_
                            f_346_ += f_395_
                            f_347_ += f_385_
                            f_349_ += f_396_
                            f_350_ += f_386_
                            f_352_ += f_397_
                            f_353_ += f_387_
                            i_358_ = (i_358_ + f_398_).toInt()
                            i_359_ = (i_359_ + f_388_).toInt()
                            f_362_ += f_399_
                            f_363_ += f_389_
                            f_365_ += f_400_
                            f_366_ += f_390_
                            f_368_ += f_401_
                            f_369_ += f_391_
                            f_371_ += f_402_
                            f_372_ += f_392_
                            f_341_ += anInt1678.toFloat()
                        }
                    } else {
                        while (--f >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_344_.toInt(), f_345_.toInt(), f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, i_359_.toFloat(), i_360_.toFloat(), f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_)
                            f_344_ += f_384_
                            f_345_ += f_374_
                            f_347_ += f_385_
                            f_348_ += f_375_
                            f_350_ += f_386_
                            f_351_ += f_376_
                            f_353_ += f_387_
                            f_354_ += f_377_
                            i_359_ = (i_359_ + f_388_).toInt()
                            i_360_ = (i_360_ + f_378_).toInt()
                            f_363_ += f_389_
                            f_364_ += f_379_
                            f_366_ += f_390_
                            f_367_ += f_380_
                            f_369_ += f_391_
                            f_370_ += f_381_
                            f_372_ += f_392_
                            f_373_ += f_382_
                            f_341_ += anInt1678.toFloat()
                        }
                        while (--f_342_ >= 0.0f) {
                            method1016(anIntArray1673, anIntArray1698, f_341_.toInt(), f_344_.toInt(), f_343_.toInt(), f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, i_359_.toFloat(), i_358_.toFloat(), f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_)
                            f_344_ += f_384_
                            f_343_ += f_394_
                            f_347_ += f_385_
                            f_346_ += f_395_
                            f_350_ += f_386_
                            f_349_ += f_396_
                            f_353_ += f_387_
                            f_352_ += f_397_
                            i_359_ = (i_359_ + f_388_).toInt()
                            i_358_ = (i_358_ + f_398_).toInt()
                            f_363_ += f_389_
                            f_362_ += f_399_
                            f_366_ += f_390_
                            f_365_ += f_400_
                            f_369_ += f_391_
                            f_368_ += f_401_
                            f_372_ += f_392_
                            f_371_ += f_402_
                            f_341_ += anInt1678.toFloat()
                        }
                    }
                }
            }
        } else if (!(f_342_ >= this.height.toFloat())) {
            if (f > this.height.toFloat()) f = this.height.toFloat()
            if (f_341_ > this.height.toFloat()) f_341_ = this.height.toFloat()
            if (f < f_341_) {
                f_344_ = f_345_
                f_347_ = f_348_
                f_350_ = f_351_
                f_353_ = f_354_
                i_359_ = i_360_
                f_363_ = f_364_
                f_366_ = f_367_
                f_369_ = f_370_
                f_372_ = f_373_
                if (f_342_ < 0.0f) {
                    f_345_ -= f_394_ * f_342_
                    f_344_ -= f_384_ * f_342_
                    f_348_ -= f_395_ * f_342_
                    f_347_ -= f_385_ * f_342_
                    f_351_ -= f_396_ * f_342_
                    f_350_ -= f_386_ * f_342_
                    f_354_ -= f_397_ * f_342_
                    f_353_ -= f_387_ * f_342_
                    i_360_ = (i_360_ - f_398_ * 3.0f).toInt()
                    i_359_ = (i_359_ - f_388_ * f_342_).toInt()
                    f_364_ -= f_399_ * f_342_
                    f_363_ -= f_389_ * f_342_
                    f_367_ -= f_400_ * f_342_
                    f_366_ -= f_390_ * f_342_
                    f_370_ -= f_401_ * f_342_
                    f_369_ -= f_391_ * f_342_
                    f_373_ -= f_402_ * f_342_
                    f_372_ -= f_392_ * f_342_
                    f_342_ = 0.0f
                }
                if (f < 0.0f) {
                    f_343_ -= f_374_ * f
                    f_346_ -= f_375_ * f
                    f_349_ -= f_376_ * f
                    f_352_ -= f_377_ * f
                    i_358_ = (i_358_ - f_378_ * f).toInt()
                    f_362_ -= f_379_ * f
                    f_365_ -= f_380_ * f
                    f_368_ -= f_381_ * f
                    f_371_ -= f_382_ * f
                    f = 0.0f
                }
                if (f_384_ < f_394_) {
                    f_341_ -= f
                    f -= f_342_
                    f_342_ = (this.lineOffsets[f_342_.toInt()]).toFloat()
                    while (--f >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_344_.toInt(), f_345_.toInt(), f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, i_359_.toFloat(), i_360_.toFloat(), f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_)
                        f_344_ += f_384_
                        f_345_ += f_394_
                        f_347_ += f_385_
                        f_348_ += f_395_
                        f_350_ += f_386_
                        f_351_ += f_396_
                        f_353_ += f_387_
                        f_354_ += f_397_
                        i_359_ = (i_359_ + f_388_).toInt()
                        i_360_ = (i_360_ + f_398_).toInt()
                        f_363_ += f_389_
                        f_364_ += f_399_
                        f_366_ += f_390_
                        f_367_ += f_400_
                        f_369_ += f_391_
                        f_370_ += f_401_
                        f_372_ += f_392_
                        f_373_ += f_402_
                        f_342_ += anInt1678.toFloat()
                    }
                    while (--f_341_ >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_344_.toInt(), f_343_.toInt(), f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, i_359_.toFloat(), i_358_.toFloat(), f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_)
                        f_344_ += f_384_
                        f_343_ += f_374_
                        f_347_ += f_385_
                        f_346_ += f_375_
                        f_350_ += f_386_
                        f_349_ += f_376_
                        f_353_ += f_387_
                        f_352_ += f_377_
                        i_359_ = (i_359_ + f_388_).toInt()
                        i_358_ = (i_358_ + f_378_).toInt()
                        f_363_ += f_389_
                        f_362_ += f_379_
                        f_366_ += f_390_
                        f_365_ += f_380_
                        f_369_ += f_391_
                        f_368_ += f_381_
                        f_372_ += f_392_
                        f_371_ += f_382_
                        f_342_ += anInt1678.toFloat()
                    }
                } else {
                    f_341_ -= f
                    f -= f_342_
                    f_342_ = (this.lineOffsets[f_342_.toInt()]).toFloat()
                    while (--f >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_345_.toInt(), f_344_.toInt(), f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, i_360_.toFloat(), i_359_.toFloat(), f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_)
                        f_345_ += f_394_
                        f_344_ += f_384_
                        f_348_ += f_395_
                        f_347_ += f_385_
                        f_351_ += f_396_
                        f_350_ += f_386_
                        f_354_ += f_397_
                        f_353_ += f_387_
                        i_360_ = (i_360_ + f_398_).toInt()
                        i_359_ = (i_359_ + f_388_).toInt()
                        f_364_ += f_399_
                        f_363_ += f_389_
                        f_367_ += f_400_
                        f_366_ += f_390_
                        f_370_ += f_401_
                        f_369_ += f_391_
                        f_373_ += f_402_
                        f_372_ += f_392_
                        f_342_ += anInt1678.toFloat()
                    }
                    while (--f_341_ >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_343_.toInt(), f_344_.toInt(), f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, i_358_.toFloat(), i_359_.toFloat(), f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_)
                        f_343_ += f_374_
                        f_344_ += f_384_
                        f_346_ += f_375_
                        f_347_ += f_385_
                        f_349_ += f_376_
                        f_350_ += f_386_
                        f_352_ += f_377_
                        f_353_ += f_387_
                        i_358_ = (i_358_ + f_378_).toInt()
                        i_359_ = (i_359_ + f_388_).toInt()
                        f_362_ += f_379_
                        f_363_ += f_389_
                        f_365_ += f_380_
                        f_366_ += f_390_
                        f_368_ += f_381_
                        f_369_ += f_391_
                        f_371_ += f_382_
                        f_372_ += f_392_
                        f_342_ += anInt1678.toFloat()
                    }
                }
            } else {
                f_343_ = f_345_
                f_346_ = f_348_
                f_349_ = f_351_
                f_352_ = f_354_
                i_358_ = i_360_
                f_362_ = f_364_
                f_365_ = f_367_
                f_368_ = f_370_
                f_371_ = f_373_
                if (f_342_ < 0.0f) {
                    f_345_ -= f_394_ * f_342_
                    f_343_ -= f_384_ * f_342_
                    f_348_ -= f_395_ * f_342_
                    f_346_ -= f_385_ * f_342_
                    f_351_ -= f_396_ * f_342_
                    f_349_ -= f_386_ * f_342_
                    f_354_ -= f_397_ * f_342_
                    f_352_ -= f_387_ * f_342_
                    i_360_ = (i_360_ - f_398_ * 3.0f).toInt()
                    i_358_ = (i_358_ - f_388_ * f_342_).toInt()
                    f_364_ -= f_399_ * f_342_
                    f_362_ -= f_389_ * f_342_
                    f_367_ -= f_400_ * f_342_
                    f_365_ -= f_390_ * f_342_
                    f_370_ -= f_401_ * f_342_
                    f_368_ -= f_391_ * f_342_
                    f_373_ -= f_402_ * f_342_
                    f_371_ -= f_392_ * f_342_
                    f_342_ = 0.0f
                }
                if (f_341_ < 0.0f) {
                    f_344_ -= f_374_ * f_341_
                    f_347_ -= f_375_ * f_341_
                    f_350_ -= f_376_ * f_341_
                    f_353_ -= f_377_ * f_341_
                    i_359_ = (i_359_ - f_378_ * f_341_).toInt()
                    f_363_ -= f_379_ * f_341_
                    f_366_ -= f_380_ * f_341_
                    f_369_ -= f_381_ * f_341_
                    f_372_ -= f_382_ * f_341_
                    f_341_ = 0.0f
                }
                if (f_384_ < f_394_) {
                    f -= f_341_
                    f_341_ -= f_342_
                    f_342_ = (this.lineOffsets[f_342_.toInt()]).toFloat()
                    while (--f_341_ >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_343_.toInt(), f_345_.toInt(), f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, i_358_.toFloat(), i_360_.toFloat(), f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_)
                        f_343_ += f_384_
                        f_345_ += f_394_
                        f_346_ += f_385_
                        f_348_ += f_395_
                        f_349_ += f_386_
                        f_351_ += f_396_
                        f_352_ += f_387_
                        f_354_ += f_397_
                        i_358_ = (i_358_ + f_388_).toInt()
                        i_360_ = (i_360_ + f_398_).toInt()
                        f_362_ += f_389_
                        f_364_ += f_399_
                        f_365_ += f_390_
                        f_367_ += f_400_
                        f_368_ += f_391_
                        f_370_ += f_401_
                        f_371_ += f_392_
                        f_373_ += f_402_
                        f_342_ += anInt1678.toFloat()
                    }
                    while (--f >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_344_.toInt(), f_345_.toInt(), f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, i_359_.toFloat(), i_360_.toFloat(), f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_)
                        f_344_ += f_374_
                        f_345_ += f_394_
                        f_347_ += f_375_
                        f_348_ += f_395_
                        f_350_ += f_376_
                        f_351_ += f_396_
                        f_353_ += f_377_
                        f_354_ += f_397_
                        i_359_ = (i_359_ + f_378_).toInt()
                        i_360_ = (i_360_ + f_398_).toInt()
                        f_363_ += f_379_
                        f_364_ += f_399_
                        f_366_ += f_380_
                        f_367_ += f_400_
                        f_369_ += f_381_
                        f_370_ += f_401_
                        f_372_ += f_382_
                        f_373_ += f_402_
                        f_342_ += anInt1678.toFloat()
                    }
                } else {
                    f -= f_341_
                    f_341_ -= f_342_
                    f_342_ = (this.lineOffsets[f_342_.toInt()]).toFloat()
                    while (--f_341_ >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_345_.toInt(), f_343_.toInt(), f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, i_360_.toFloat(), i_358_.toFloat(), f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_)
                        f_345_ += f_394_
                        f_343_ += f_384_
                        f_348_ += f_395_
                        f_346_ += f_385_
                        f_351_ += f_396_
                        f_349_ += f_386_
                        f_354_ += f_397_
                        f_352_ += f_387_
                        i_360_ = (i_360_ + f_398_).toInt()
                        i_358_ = (i_358_ + f_388_).toInt()
                        f_364_ += f_399_
                        f_362_ += f_389_
                        f_367_ += f_400_
                        f_365_ += f_390_
                        f_370_ += f_401_
                        f_368_ += f_391_
                        f_373_ += f_402_
                        f_371_ += f_392_
                        f_342_ += anInt1678.toFloat()
                    }
                    while (--f >= 0.0f) {
                        method1016(anIntArray1673, anIntArray1698, f_342_.toInt(), f_345_.toInt(), f_344_.toInt(), f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, i_360_.toFloat(), i_359_.toFloat(), f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_)
                        f_345_ += f_394_
                        f_344_ += f_374_
                        f_348_ += f_395_
                        f_347_ += f_375_
                        f_351_ += f_396_
                        f_350_ += f_376_
                        f_354_ += f_397_
                        f_353_ += f_377_
                        i_360_ = (i_360_ + f_398_).toInt()
                        i_359_ = (i_359_ + f_378_).toInt()
                        f_364_ += f_399_
                        f_363_ += f_379_
                        f_367_ += f_400_
                        f_366_ += f_380_
                        f_370_ += f_401_
                        f_369_ += f_391_
                        f_373_ += f_402_
                        f_372_ += f_392_
                        f_342_ += anInt1678.toFloat()
                    }
                }
            }
        }
    }

    private fun method1026(`is`: IntArray, fs: FloatArray, i: Int, i_450_: Int, i_451_: Int, i_452_: Int, i_453_: Int, f: Float, f_454_: Float) {
        var i = i
        var i_450_ = i_450_
        var i_451_ = i_451_
        var i_452_ = i_452_
        var i_453_ = i_453_
        var f = f
        if (this.clamp) {
            if (i_453_ > this.width) i_453_ = this.width
            if (i_452_ < 0) i_452_ = 0
        }
        if (i_452_ < i_453_) {
            i += i_452_ - 1
            i_451_ = i_453_ - i_452_ shr 2
            f += f_454_ * i_452_.toFloat()
            if (aJavaThreadResource_1670.aBoolean2202) {
                if (this.anInt1674 == 0) {
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            `is`[i] = i_450_
                            fs[i] = f
                        }
                        f += f_454_
                        if (f < fs[++i]) {
                            `is`[i] = i_450_
                            fs[i] = f
                        }
                        f += f_454_
                        if (f < fs[++i]) {
                            `is`[i] = i_450_
                            fs[i] = f
                        }
                        f += f_454_
                        if (f < fs[++i]) {
                            `is`[i] = i_450_
                            fs[i] = f
                        }
                        f += f_454_
                    }
                    i_451_ = i_453_ - i_452_ and 0x3
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            `is`[i] = i_450_
                            fs[i] = f
                        }
                        f += f_454_
                    }
                } else if (this.anInt1674 == 254) {
                    if (i_452_ != 0 && i_453_ <= this.width - 1) {
                        while (--i_451_ >= 0) {
                            if (f < fs[++i]) `is`[i - 1] = `is`[i]
                            f += f_454_
                            if (f < fs[++i]) `is`[i - 1] = `is`[i]
                            f += f_454_
                            if (f < fs[++i]) `is`[i - 1] = `is`[i]
                            f += f_454_
                            if (f < fs[++i]) `is`[i - 1] = `is`[i]
                            f += f_454_
                        }
                        i_451_ = i_453_ - i_452_ and 0x3
                        while (--i_451_ >= 0) {
                            if (f < fs[++i]) `is`[i - 1] = `is`[i]
                            f += f_454_
                        }
                    }
                } else {
                    val i_455_ = this.anInt1674
                    val i_456_ = 256 - this.anInt1674
                    i_450_ = (((i_450_ and 0xff00ff) * i_456_ shr 8 and 0xff00ff) + ((i_450_ and 0xff00) * i_456_ shr 8 and 0xff00))
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            val i_457_ = `is`[i]
                            `is`[i] = (i_450_ + ((i_457_ and 0xff00ff) * i_455_ shr 8 and 0xff00ff) + ((i_457_ and 0xff00) * i_455_ shr 8 and 0xff00))
                            fs[i] = f
                        }
                        f += f_454_
                        if (f < fs[++i]) {
                            val i_458_ = `is`[i]
                            `is`[i] = (i_450_ + ((i_458_ and 0xff00ff) * i_455_ shr 8 and 0xff00ff) + ((i_458_ and 0xff00) * i_455_ shr 8 and 0xff00))
                            fs[i] = f
                        }
                        f += f_454_
                        if (f < fs[++i]) {
                            val i_459_ = `is`[i]
                            `is`[i] = (i_450_ + ((i_459_ and 0xff00ff) * i_455_ shr 8 and 0xff00ff) + ((i_459_ and 0xff00) * i_455_ shr 8 and 0xff00))
                            fs[i] = f
                        }
                        f += f_454_
                        if (f < fs[++i]) {
                            val i_460_ = `is`[i]
                            `is`[i] = (i_450_ + ((i_460_ and 0xff00ff) * i_455_ shr 8 and 0xff00ff) + ((i_460_ and 0xff00) * i_455_ shr 8 and 0xff00))
                            fs[i] = f
                        }
                        f += f_454_
                    }
                    i_451_ = i_453_ - i_452_ and 0x3
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            val i_461_ = `is`[i]
                            `is`[i] = (i_450_ + ((i_461_ and 0xff00ff) * i_455_ shr 8 and 0xff00ff) + ((i_461_ and 0xff00) * i_455_ shr 8 and 0xff00))
                            fs[i] = f
                        }
                        f += f_454_
                    }
                }
            } else if (this.anInt1674 == 0) {
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) `is`[i] = i_450_
                    f += f_454_
                    if (f < fs[++i]) `is`[i] = i_450_
                    f += f_454_
                    if (f < fs[++i]) `is`[i] = i_450_
                    f += f_454_
                    if (f < fs[++i]) `is`[i] = i_450_
                    f += f_454_
                }
                i_451_ = i_453_ - i_452_ and 0x3
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) `is`[i] = i_450_
                    f += f_454_
                }
            } else if (this.anInt1674 == 254) {
                if (i_452_ != 0 && i_453_ <= this.width - 1) {
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) `is`[i - 1] = `is`[i]
                        f += f_454_
                        if (f < fs[++i]) `is`[i - 1] = `is`[i]
                        f += f_454_
                        if (f < fs[++i]) `is`[i - 1] = `is`[i]
                        f += f_454_
                        if (f < fs[++i]) `is`[i - 1] = `is`[i]
                        f += f_454_
                    }
                    i_451_ = i_453_ - i_452_ and 0x3
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) `is`[i - 1] = `is`[i]
                        f += f_454_
                    }
                }
            } else {
                val i_462_ = this.anInt1674
                val i_463_ = 256 - this.anInt1674
                i_450_ = (((i_450_ and 0xff00ff) * i_463_ shr 8 and 0xff00ff) + ((i_450_ and 0xff00) * i_463_ shr 8 and 0xff00))
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) {
                        val i_464_ = `is`[i]
                        `is`[i] = (i_450_ + ((i_464_ and 0xff00ff) * i_462_ shr 8 and 0xff00ff) + ((i_464_ and 0xff00) * i_462_ shr 8 and 0xff00))
                    }
                    f += f_454_
                    if (f < fs[++i]) {
                        val i_465_ = `is`[i]
                        `is`[i] = (i_450_ + ((i_465_ and 0xff00ff) * i_462_ shr 8 and 0xff00ff) + ((i_465_ and 0xff00) * i_462_ shr 8 and 0xff00))
                    }
                    f += f_454_
                    if (f < fs[++i]) {
                        val i_466_ = `is`[i]
                        `is`[i] = (i_450_ + ((i_466_ and 0xff00ff) * i_462_ shr 8 and 0xff00ff) + ((i_466_ and 0xff00) * i_462_ shr 8 and 0xff00))
                    }
                    f += f_454_
                    if (f < fs[++i]) {
                        val i_467_ = `is`[i]
                        `is`[i] = (i_450_ + ((i_467_ and 0xff00ff) * i_462_ shr 8 and 0xff00ff) + ((i_467_ and 0xff00) * i_462_ shr 8 and 0xff00))
                    }
                    f += f_454_
                }
                i_451_ = i_453_ - i_452_ and 0x3
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) {
                        val i_468_ = `is`[i]
                        `is`[i] = (i_450_ + ((i_468_ and 0xff00ff) * i_462_ shr 8 and 0xff00ff) + ((i_468_ and 0xff00) * i_462_ shr 8 and 0xff00))
                    }
                    f += f_454_
                }
            }
        }
    }

    fun method1027(f: Float, f_469_: Float, f_470_: Float, f_471_: Float, f_472_: Float, f_473_: Float, f_474_: Float, f_475_: Float, f_476_: Float, i: Int, i_477_: Int, i_478_: Int) {
        var f = f
        var f_469_ = f_469_
        var f_470_ = f_470_
        var f_471_ = f_471_
        var f_472_ = f_472_
        var f_473_ = f_473_
        var f_474_ = f_474_
        var f_475_ = f_475_
        var f_476_ = f_476_
        if (aBoolean1675) {
            aHa_Sub1_1666.line(f.toInt(), f_471_.toInt(), f_472_.toInt(), -8003, 0xffffff.inv() or i, f_469_.toInt())
            aHa_Sub1_1666.line(f_469_.toInt(), f_472_.toInt(), f_473_.toInt(), -8003, 0xffffff.inv() or i, f_470_.toInt())
            aHa_Sub1_1666.line(f_470_.toInt(), f_473_.toInt(), f_471_.toInt(), -8003, 0xffffff.inv() or i, f.toInt())
        } else {
            val f_479_ = f_472_ - f_471_
            val f_480_ = f_469_ - f
            val f_481_ = f_473_ - f_471_
            val f_482_ = f_470_ - f
            val f_483_ = f_475_ - f_474_
            val f_484_ = f_476_ - f_474_
            val f_485_ = ((i_477_ and 0xff0000) - (i and 0xff0000)).toFloat()
            val f_486_ = ((i_478_ and 0xff0000) - (i and 0xff0000)).toFloat()
            val f_487_ = ((i_477_ and 0xff00) - (i and 0xff00)).toFloat()
            val f_488_ = ((i_478_ and 0xff00) - (i and 0xff00)).toFloat()
            val f_489_ = ((i_477_ and 0xff) - (i and 0xff)).toFloat()
            val f_490_ = ((i_478_ and 0xff) - (i and 0xff)).toFloat()
            val f_491_: Float
            if (f_470_ != f_469_) f_491_ = (f_473_ - f_472_) / (f_470_ - f_469_)
            else f_491_ = 0.0f
            val f_492_: Float
            if (f_469_ != f) f_492_ = f_479_ / f_480_
            else f_492_ = 0.0f
            val f_493_: Float
            if (f_470_ != f) f_493_ = f_481_ / f_482_
            else f_493_ = 0.0f
            val f_494_ = f_479_ * f_482_ - f_481_ * f_480_
            if (f_494_ != 0.0f) {
                val f_495_ = (f_483_ * f_482_ - f_484_ * f_480_) / f_494_
                val f_496_ = (f_484_ * f_479_ - f_483_ * f_481_) / f_494_
                val f_497_ = (f_485_ * f_482_ - f_486_ * f_480_) / f_494_
                val f_498_ = (f_486_ * f_479_ - f_485_ * f_481_) / f_494_
                val f_499_ = (f_487_ * f_482_ - f_488_ * f_480_) / f_494_
                val f_500_ = (f_488_ * f_479_ - f_487_ * f_481_) / f_494_
                val f_501_ = (f_489_ * f_482_ - f_490_ * f_480_) / f_494_
                val f_502_ = (f_490_ * f_479_ - f_489_ * f_481_) / f_494_
                if (f <= f_469_ && f <= f_470_) {
                    if (!(f >= this.height.toFloat())) {
                        if (f_469_ > this.height.toFloat()) f_469_ = this.height.toFloat()
                        if (f_470_ > this.height.toFloat()) f_470_ = this.height.toFloat()
                        f_474_ = f_474_ - f_495_ * f_471_ + f_495_
                        var f_503_ = ((i and 0xff0000).toFloat() - f_497_ * f_471_ + f_497_)
                        var f_504_ = (i and 0xff00).toFloat() - f_499_ * f_471_ + f_499_
                        var f_505_ = (i and 0xff).toFloat() - f_501_ * f_471_ + f_501_
                        if (f_469_ < f_470_) {
                            f_473_ = f_471_
                            if (f < 0.0f) {
                                f_473_ -= f_493_ * f
                                f_471_ -= f_492_ * f
                                f_474_ -= f_496_ * f
                                f_503_ -= f_498_ * f
                                f_504_ -= f_500_ * f
                                f_505_ -= f_502_ * f
                                f = 0.0f
                            }
                            if (f_469_ < 0.0f) {
                                f_472_ -= f_491_ * f_469_
                                f_469_ = 0.0f
                            }
                            if (f != f_469_ && f_493_ < f_492_ || f == f_469_ && f_493_ > f_491_) {
                                f_470_ -= f_469_
                                f_469_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_469_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_473_.toInt(), f_471_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_473_ += f_493_
                                    f_471_ += f_492_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_473_.toInt(), f_472_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_473_ += f_493_
                                    f_472_ += f_491_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                            } else {
                                f_470_ -= f_469_
                                f_469_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_469_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_471_.toInt(), f_473_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_473_ += f_493_
                                    f_471_ += f_492_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_472_.toInt(), f_473_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_473_ += f_493_
                                    f_472_ += f_491_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                            }
                        } else {
                            f_472_ = f_471_
                            if (f < 0.0f) {
                                f_472_ -= f_493_ * f
                                f_471_ -= f_492_ * f
                                f_474_ -= f_496_ * f
                                f_503_ -= f_498_ * f
                                f_504_ -= f_500_ * f
                                f_505_ -= f_502_ * f
                                f = 0.0f
                            }
                            if (f_470_ < 0.0f) {
                                f_473_ -= f_491_ * f_470_
                                f_470_ = 0.0f
                            }
                            if (f != f_470_ && f_493_ < f_492_ || f == f_470_ && f_491_ > f_492_) {
                                f_469_ -= f_470_
                                f_470_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_472_.toInt(), f_471_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_472_ += f_493_
                                    f_471_ += f_492_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_469_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_473_.toInt(), f_471_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_473_ += f_491_
                                    f_471_ += f_492_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                            } else {
                                f_469_ -= f_470_
                                f_470_ -= f
                                f = (this.lineOffsets[f.toInt()]).toFloat()
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_471_.toInt(), f_472_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_472_ += f_493_
                                    f_471_ += f_492_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                                while (--f_469_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f.toInt(), 0, 0, f_471_.toInt(), f_473_.toInt(), f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_)
                                    f_473_ += f_491_
                                    f_471_ += f_492_
                                    f_474_ += f_496_
                                    f_503_ += f_498_
                                    f_504_ += f_500_
                                    f_505_ += f_502_
                                    f += anInt1678.toFloat()
                                }
                            }
                        }
                    }
                } else if (f_469_ <= f_470_) {
                    if (!(f_469_ >= this.height.toFloat())) {
                        if (f_470_ > this.height.toFloat()) f_470_ = this.height.toFloat()
                        if (f > this.height.toFloat()) f = this.height.toFloat()
                        f_475_ = f_475_ - f_495_ * f_472_ + f_495_
                        var f_506_ = ((i_477_ and 0xff0000).toFloat() - f_497_ * f_472_ + f_497_)
                        var f_507_ = ((i_477_ and 0xff00).toFloat() - f_499_ * f_472_ + f_499_)
                        var f_508_ = ((i_477_ and 0xff).toFloat() - f_501_ * f_472_ + f_501_)
                        if (f_470_ < f) {
                            f_471_ = f_472_
                            if (f_469_ < 0.0f) {
                                f_471_ -= f_492_ * f_469_
                                f_472_ -= f_491_ * f_469_
                                f_475_ -= f_496_ * f_469_
                                f_506_ -= f_498_ * f_469_
                                f_507_ -= f_500_ * f_469_
                                f_508_ -= f_502_ * f_469_
                                f_469_ = 0.0f
                            }
                            if (f_470_ < 0.0f) {
                                f_473_ -= f_493_ * f_470_
                                f_470_ = 0.0f
                            }
                            if (f_469_ != f_470_ && f_492_ < f_491_ || f_469_ == f_470_ && f_492_ > f_493_) {
                                f -= f_470_
                                f_470_ -= f_469_
                                f_469_ = (this.lineOffsets[f_469_.toInt()]).toFloat()
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_471_.toInt(), f_472_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_471_ += f_492_
                                    f_472_ += f_491_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                                while (--f >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_471_.toInt(), f_473_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_471_ += f_492_
                                    f_473_ += f_493_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                            } else {
                                f -= f_470_
                                f_470_ -= f_469_
                                f_469_ = (this.lineOffsets[f_469_.toInt()]).toFloat()
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_472_.toInt(), f_471_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_471_ += f_492_
                                    f_472_ += f_491_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                                while (--f >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_473_.toInt(), f_471_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_471_ += f_492_
                                    f_473_ += f_493_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                            }
                        } else {
                            f_473_ = f_472_
                            if (f_469_ < 0.0f) {
                                f_473_ -= f_492_ * f_469_
                                f_472_ -= f_491_ * f_469_
                                f_475_ -= f_496_ * f_469_
                                f_506_ -= f_498_ * f_469_
                                f_507_ -= f_500_ * f_469_
                                f_508_ -= f_502_ * f_469_
                                f_469_ = 0.0f
                            }
                            if (f < 0.0f) {
                                f_471_ -= f_493_ * f
                                f = 0.0f
                            }
                            if (f_492_ < f_491_) {
                                f_470_ -= f
                                f -= f_469_
                                f_469_ = (this.lineOffsets[f_469_.toInt()]).toFloat()
                                while (--f >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_473_.toInt(), f_472_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_473_ += f_492_
                                    f_472_ += f_491_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_471_.toInt(), f_472_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_471_ += f_493_
                                    f_472_ += f_491_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                            } else {
                                f_470_ -= f
                                f -= f_469_
                                f_469_ = (this.lineOffsets[f_469_.toInt()]).toFloat()
                                while (--f >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_472_.toInt(), f_473_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_473_ += f_492_
                                    f_472_ += f_491_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                                while (--f_470_ >= 0.0f) {
                                    method1021(anIntArray1673, aFloatArray1677, f_469_.toInt(), 0, 0, f_472_.toInt(), f_471_.toInt(), f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_)
                                    f_471_ += f_493_
                                    f_472_ += f_491_
                                    f_475_ += f_496_
                                    f_506_ += f_498_
                                    f_507_ += f_500_
                                    f_508_ += f_502_
                                    f_469_ += anInt1678.toFloat()
                                }
                            }
                        }
                    }
                } else if (!(f_470_ >= this.height.toFloat())) {
                    if (f > this.height.toFloat()) f = this.height.toFloat()
                    if (f_469_ > this.height.toFloat()) f_469_ = this.height.toFloat()
                    f_476_ = f_476_ - f_495_ * f_473_ + f_495_
                    var f_509_ = ((i_478_ and 0xff0000).toFloat() - f_497_ * f_473_ + f_497_)
                    var f_510_ = (i_478_ and 0xff00).toFloat() - f_499_ * f_473_ + f_499_
                    var f_511_ = (i_478_ and 0xff).toFloat() - f_501_ * f_473_ + f_501_
                    if (f < f_469_) {
                        f_472_ = f_473_
                        if (f_470_ < 0.0f) {
                            f_472_ -= f_491_ * f_470_
                            f_473_ -= f_493_ * f_470_
                            f_476_ -= f_496_ * f_470_
                            f_509_ -= f_498_ * f_470_
                            f_510_ -= f_500_ * f_470_
                            f_511_ -= f_502_ * f_470_
                            f_470_ = 0.0f
                        }
                        if (f < 0.0f) {
                            f_471_ -= f_492_ * f
                            f = 0.0f
                        }
                        if (f_491_ < f_493_) {
                            f_469_ -= f
                            f -= f_470_
                            f_470_ = (this.lineOffsets[f_470_.toInt()]).toFloat()
                            while (--f >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_472_.toInt(), f_473_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_472_ += f_491_
                                f_473_ += f_493_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                            while (--f_469_ >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_472_.toInt(), f_471_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_472_ += f_491_
                                f_471_ += f_492_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                        } else {
                            f_469_ -= f
                            f -= f_470_
                            f_470_ = (this.lineOffsets[f_470_.toInt()]).toFloat()
                            while (--f >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_473_.toInt(), f_472_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_472_ += f_491_
                                f_473_ += f_493_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                            while (--f_469_ >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_471_.toInt(), f_472_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_472_ += f_491_
                                f_471_ += f_492_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                        }
                    } else {
                        f_471_ = f_473_
                        if (f_470_ < 0.0f) {
                            f_471_ -= f_491_ * f_470_
                            f_473_ -= f_493_ * f_470_
                            f_476_ -= f_496_ * f_470_
                            f_509_ -= f_498_ * f_470_
                            f_510_ -= f_500_ * f_470_
                            f_511_ -= f_502_ * f_470_
                            f_470_ = 0.0f
                        }
                        if (f_469_ < 0.0f) {
                            f_472_ -= f_492_ * f_469_
                            f_469_ = 0.0f
                        }
                        if (f_491_ < f_493_) {
                            f -= f_469_
                            f_469_ -= f_470_
                            f_470_ = (this.lineOffsets[f_470_.toInt()]).toFloat()
                            while (--f_469_ >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_471_.toInt(), f_473_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_471_ += f_491_
                                f_473_ += f_493_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                            while (--f >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_472_.toInt(), f_473_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_472_ += f_492_
                                f_473_ += f_493_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                        } else {
                            f -= f_469_
                            f_469_ -= f_470_
                            f_470_ = (this.lineOffsets[f_470_.toInt()]).toFloat()
                            while (--f_469_ >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_473_.toInt(), f_471_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_471_ += f_491_
                                f_473_ += f_493_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                            while (--f >= 0.0f) {
                                method1021(anIntArray1673, aFloatArray1677, f_470_.toInt(), 0, 0, f_473_.toInt(), f_472_.toInt(), f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_)
                                f_472_ += f_492_
                                f_473_ += f_493_
                                f_476_ += f_496_
                                f_509_ += f_498_
                                f_510_ += f_500_
                                f_511_ += f_502_
                                f_470_ += anInt1678.toFloat()
                            }
                        }
                    }
                }
            }
        }
    }

    fun method1028(): Int {
        return this.lineOffsets[0] % anInt1678
    }

    init {
        anInt1687 = -1
        anInt1689 = -1
        anInt1697 = -1
        anInt1678 = aHa_Sub1_1666.anInt7477
        anIntArray1673 = aHa_Sub1_1666.anIntArray7483
        aFloatArray1677 = aHa_Sub1_1666.aFloatArray7511
    }
}
