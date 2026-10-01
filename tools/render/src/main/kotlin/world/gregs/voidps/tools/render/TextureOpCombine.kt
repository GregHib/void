package world.gregs.voidps.tools.render

import kotlin.math.max
import kotlin.math.min

/** Class348_Sub40_Sub16 **/
internal class TextureOpCombine : TextureOp(2, false) {
    private var anInt9226 = 6
    override fun method3049(packet: Packet?, i: Int) {
        val i_1_ = i
        do {
            if (i_1_ == 0) {
                anInt9226 = packet!!.readUnsignedByte()
                break
            } else if (i_1_ != 1) break
            this.aBoolean7045 = packet!!.readUnsignedByte() == 1
        } while (false)
    }

    override fun method3047(i: Int): Array<IntArray> {
        val `is` = this.aColourImageCache_7033!!.method2557(i)
        while_168_@ do {
            if (this.aColourImageCache_7033!!.aBoolean4035) {
                val is_3_ = this.method3039(i, 0)
                val is_4_ = this.method3039(i, 1)
                val is_5_ = `is`!![0]
                val is_6_ = `is`[1]
                val is_7_ = `is`[2]
                val is_8_ = is_3_!![0]
                val is_9_ = is_3_[1]
                val is_10_ = is_3_[2]
                val is_11_ = is_4_!![0]
                val is_12_ = is_4_[1]
                val is_13_ = is_4_[2]
                var i_14_ = anInt9226
                while_167_@ do {
                    while_166_@ do {
                        while_165_@ do {
                            while_164_@ do {
                                while_163_@ do {
                                    while_162_@ do {
                                        while_161_@ do {
                                            while_160_@ do {
                                                while_159_@ do {
                                                    do {
                                                        if (i_14_ == 1) {
                                                            i_14_ = 0
                                                            while (((TextureOpPolarDistortion.anInt9139) > i_14_)) {
                                                                is_5_[i_14_] = ((is_11_[i_14_]) + (is_8_[i_14_]))
                                                                is_6_[i_14_] = ((is_9_[i_14_]) + (is_12_[i_14_]))
                                                                is_7_[i_14_] = ((is_10_[i_14_]) + (is_13_[i_14_]))
                                                                i_14_++
                                                            }
                                                            break@while_168_
                                                        } else if (i_14_ != 2) {
                                                            if (i_14_ != 3) {
                                                                if (i_14_ != 4) {
                                                                    if (i_14_ != 5) {
                                                                        if (i_14_ != 6) {
                                                                            if (i_14_ != 7) {
                                                                                if (i_14_ != 8) {
                                                                                    if (i_14_ != 9) {
                                                                                        if (i_14_ != 10) {
                                                                                            if (i_14_ != 11) {
                                                                                                if (i_14_ != 12) break@while_168_
                                                                                            } else break@while_166_
                                                                                            break@while_167_
                                                                                        }
                                                                                    } else break@while_164_
                                                                                    break@while_165_
                                                                                }
                                                                            } else break@while_162_
                                                                            break@while_163_
                                                                        }
                                                                    } else break@while_160_
                                                                    break@while_161_
                                                                }
                                                            } else break
                                                            break@while_159_
                                                        }
                                                        i_14_ = 0
                                                        while (((TextureOpPolarDistortion.anInt9139) > i_14_)) {
                                                            is_5_[i_14_] = (is_8_[i_14_] - (is_11_[i_14_]))
                                                            is_6_[i_14_] = (is_9_[i_14_] + -(is_12_[i_14_]))
                                                            is_7_[i_14_] = (-(is_13_[i_14_]) + (is_10_[i_14_]))
                                                            i_14_++
                                                        }
                                                        break@while_168_
                                                    } while (false)
                                                    i_14_ = 0
                                                    while ((i_14_ < (TextureOpPolarDistortion.anInt9139))) {
                                                        is_5_[i_14_] = ((is_11_[i_14_] * is_8_[i_14_]) shr 12)
                                                        is_6_[i_14_] = ((is_12_[i_14_] * is_9_[i_14_]) shr 12)
                                                        is_7_[i_14_] = ((is_10_[i_14_] * (is_13_[i_14_])) shr 12)
                                                        i_14_++
                                                    }
                                                    break@while_168_
                                                } while (false)
                                                i_14_ = 0
                                                while ((i_14_ < (TextureOpPolarDistortion.anInt9139))) {
                                                    val i_15_ = is_11_[i_14_]
                                                    val i_16_ = is_13_[i_14_]
                                                    val i_17_ = is_12_[i_14_]
                                                    is_5_[i_14_] = (if (i_15_ != 0) ((is_8_[i_14_] shl 12) / i_15_) else 4096)
                                                    is_6_[i_14_] = (if (i_17_ == 0) 4096 else ((is_9_[i_14_] shl 12) / i_17_))
                                                    is_7_[i_14_] = (if (i_16_ == 0) 4096 else ((is_10_[i_14_] shl 12) / i_16_))
                                                    i_14_++
                                                }
                                                break@while_168_
                                            } while (false)
                                            i_14_ = 0
                                            while ((i_14_ < (TextureOpPolarDistortion.anInt9139))) {
                                                is_5_[i_14_] = (4096 + -(((-is_11_[i_14_] + 4096) * (4096 - is_8_[i_14_])) shr 12))
                                                is_6_[i_14_] = (4096 + -(((4096 + -is_12_[i_14_]) * (4096 - is_9_[i_14_])) shr 12))
                                                is_7_[i_14_] = (4096 - (((-is_13_[i_14_] + 4096) * (4096 - is_10_[i_14_])) shr 12))
                                                i_14_++
                                            }
                                            break@while_168_
                                        } while (false)
                                        i_14_ = 0
                                        while ((TextureOpPolarDistortion.anInt9139 > i_14_)) {
                                            val i_18_ = is_11_[i_14_]
                                            val i_19_ = is_13_[i_14_]
                                            val i_20_ = is_12_[i_14_]
                                            is_5_[i_14_] = (if (i_18_ >= 2048) -(((4096 - is_8_[i_14_]) * (-i_18_ + 4096)) shr 11) + 4096 else (is_8_[i_14_] * i_18_ shr 11))
                                            is_6_[i_14_] = (if (i_20_ >= 2048) -(((-i_20_ + 4096) * (4096 - is_9_[i_14_])) shr 11) + 4096 else (i_20_ * is_9_[i_14_] shr 11))
                                            is_7_[i_14_] = (if (i_19_ < 2048) (i_19_ * is_10_[i_14_] shr 11) else -(((-is_10_[i_14_] + 4096) * (-i_19_ + 4096)) shr 11) + 4096)
                                            i_14_++
                                        }
                                        break@while_168_
                                    } while (false)
                                    i_14_ = 0
                                    while (TextureOpPolarDistortion.anInt9139 > i_14_) {
                                        val i_21_ = is_10_[i_14_]
                                        val i_22_ = is_9_[i_14_]
                                        val i_23_ = is_8_[i_14_]
                                        is_5_[i_14_] = (if (i_23_ == 4096) 4096 else ((is_11_[i_14_] shl 12) / (-i_23_ + 4096)))
                                        is_6_[i_14_] = (if (i_22_ != 4096) ((is_12_[i_14_] shl 12) / (-i_22_ + 4096)) else 4096)
                                        is_7_[i_14_] = (if (i_21_ == 4096) 4096 else ((is_13_[i_14_] shl 12) / (4096 - i_21_)))
                                        i_14_++
                                    }
                                    break@while_168_
                                } while (false)
                                i_14_ = 0
                                while ((TextureOpPolarDistortion.anInt9139 > i_14_)) {
                                    val i_24_ = is_9_[i_14_]
                                    val i_25_ = is_8_[i_14_]
                                    val i_26_ = is_10_[i_14_]
                                    is_5_[i_14_] = (if (i_25_ == 0) 0 else -((-is_11_[i_14_] + 4096 shl 12) / i_25_) + 4096)
                                    is_6_[i_14_] = if (i_24_ != 0) -((4096 + -is_12_[i_14_] shl 12) / i_24_) + 4096 else 0
                                    is_7_[i_14_] = (if (i_26_ == 0) 0 else -((-is_13_[i_14_] + 4096 shl 12) / i_26_) + 4096)
                                    i_14_++
                                }
                                break@while_168_
                            } while (false)
                            i_14_ = 0
                            while (i_14_ < TextureOpPolarDistortion.anInt9139) {
                                val i_27_ = is_9_[i_14_]
                                val i_28_ = is_12_[i_14_]
                                val i_29_ = is_13_[i_14_]
                                val i_30_ = is_11_[i_14_]
                                val i_31_ = is_10_[i_14_]
                                val i_32_ = is_8_[i_14_]
                                is_5_[i_14_] = (min(i_30_, i_32_))
                                is_6_[i_14_] = (min(i_28_, i_27_))
                                is_7_[i_14_] = min(i_29_, i_31_)
                                i_14_++
                            }
                            break@while_168_
                        } while (false)
                        i_14_ = 0
                        while ((TextureOpPolarDistortion.anInt9139 > i_14_)) {
                            val i_33_ = is_9_[i_14_]
                            val i_34_ = is_10_[i_14_]
                            val i_35_ = is_12_[i_14_]
                            val i_36_ = is_11_[i_14_]
                            val i_37_ = is_13_[i_14_]
                            val i_38_ = is_8_[i_14_]
                            is_5_[i_14_] = max(i_36_, i_38_)
                            is_6_[i_14_] = (max(i_33_, i_35_))
                            is_7_[i_14_] = (max(i_37_, i_34_))
                            i_14_++
                        }
                        break@while_168_
                    } while (false)
                    i_14_ = 0
                    while (TextureOpPolarDistortion.anInt9139 > i_14_) {
                        val i_39_ = is_13_[i_14_]
                        val i_40_ = is_9_[i_14_]
                        val i_41_ = is_10_[i_14_]
                        val i_42_ = is_11_[i_14_]
                        val i_43_ = is_8_[i_14_]
                        val i_44_ = is_12_[i_14_]
                        is_5_[i_14_] = (if (i_43_ > i_42_) -i_42_ + i_43_ else i_42_ - i_43_)
                        is_6_[i_14_] = (if (i_44_ >= i_40_) i_44_ + -i_40_ else -i_44_ + i_40_)
                        is_7_[i_14_] = if (i_41_ <= i_39_) i_39_ - i_41_ else -i_39_ + i_41_
                        i_14_++
                    }
                    break@while_168_
                } while (false)
                i_14_ = 0
                while ((TextureOpPolarDistortion.anInt9139 > i_14_)) {
                    val i_45_ = is_8_[i_14_]
                    val i_46_ = is_10_[i_14_]
                    val i_47_ = is_13_[i_14_]
                    val i_48_ = is_11_[i_14_]
                    val i_49_ = is_9_[i_14_]
                    val i_50_ = is_12_[i_14_]
                    is_5_[i_14_] = -(i_45_ * i_48_ shr 11) + (i_48_ + i_45_)
                    is_6_[i_14_] = -(i_49_ * i_50_ shr 11) + (i_49_ + i_50_)
                    is_7_[i_14_] = i_47_ + i_46_ - (i_46_ * i_47_ shr 11)
                    i_14_++
                }
            }
        } while (false)
        return `is`!!
    }

    override fun method3042(i: Int): IntArray {
        val `is` = this.aMonochromeImageCache_7032!!.method1433(i)
        while_178_@ do {
            if (this.aMonochromeImageCache_7032!!.aBoolean2570) {
                val is_52_ = this.method3048(i, 0)
                val is_53_ = this.method3048(i, 1)
                var i_54_ = anInt9226
                while_177_@ do {
                    while_176_@ do {
                        while_175_@ do {
                            while_174_@ do {
                                while_173_@ do {
                                    while_172_@ do {
                                        while_171_@ do {
                                            while_170_@ do {
                                                while_169_@ do {
                                                    do {
                                                        if (i_54_ == 1) {
                                                            i_54_ = 0
                                                            while (((TextureOpPolarDistortion.anInt9139) > i_54_)) {
                                                                `is`!![i_54_] = ((is_52_!![i_54_]) + (is_53_!![i_54_]))
                                                                i_54_++
                                                            }
                                                            break@while_178_
                                                        } else if (i_54_ != 2) {
                                                            if (i_54_ != 3) {
                                                                if (i_54_ != 4) {
                                                                    if (i_54_ != 5) {
                                                                        if (i_54_ != 6) {
                                                                            if (i_54_ != 7) {
                                                                                if (i_54_ != 8) {
                                                                                    if (i_54_ != 9) {
                                                                                        if (i_54_ != 10) {
                                                                                            if (i_54_ != 11) {
                                                                                                if (i_54_ != 12) break@while_178_
                                                                                            } else break@while_176_
                                                                                            break@while_177_
                                                                                        }
                                                                                    } else break@while_174_
                                                                                    break@while_175_
                                                                                }
                                                                            } else break@while_172_
                                                                            break@while_173_
                                                                        }
                                                                    } else break@while_170_
                                                                    break@while_171_
                                                                }
                                                            } else break
                                                            break@while_169_
                                                        }
                                                        i_54_ = 0
                                                        while (((TextureOpPolarDistortion.anInt9139) > i_54_)) {
                                                            `is`!![i_54_] = ((is_52_!![i_54_]) + -(is_53_!![i_54_]))
                                                            i_54_++
                                                        }
                                                        break@while_178_
                                                    } while (false)
                                                    i_54_ = 0
                                                    while (((TextureOpPolarDistortion.anInt9139) > i_54_)) {
                                                        `is`!![i_54_] = ((is_53_!![i_54_] * (is_52_!![i_54_])) shr 12)
                                                        i_54_++
                                                    }
                                                    break@while_178_
                                                } while (false)
                                                i_54_ = 0
                                                while (((TextureOpPolarDistortion.anInt9139) > i_54_)) {
                                                    val i_55_ = is_53_!![i_54_]
                                                    `is`!![i_54_] = (if (i_55_ != 0) ((is_52_!![i_54_] shl 12) / i_55_) else 4096)
                                                    i_54_++
                                                }
                                                break@while_178_
                                            } while (false)
                                            i_54_ = 0
                                            while ((TextureOpPolarDistortion.anInt9139 > i_54_)) {
                                                `is`!![i_54_] = -(((4096 - is_52_!![i_54_]) * (4096 - is_53_!![i_54_])) shr 12) + 4096
                                                i_54_++
                                            }
                                            break@while_178_
                                        } while (false)
                                        i_54_ = 0
                                        while ((i_54_ < TextureOpPolarDistortion.anInt9139)) {
                                            val i_56_ = is_53_!![i_54_]
                                            `is`!![i_54_] = (if (i_56_ >= 2048) (4096 + -(((-i_56_ + 4096) * (4096 - is_52_!![i_54_])) shr 11)) else (i_56_ * is_52_!![i_54_] shr 11))
                                            i_54_++
                                        }
                                        break@while_178_
                                    } while (false)
                                    i_54_ = 0
                                    while (i_54_ < TextureOpPolarDistortion.anInt9139) {
                                        val i_57_ = is_52_!![i_54_]
                                        `is`!![i_54_] = (if (i_57_ != 4096) ((is_53_!![i_54_] shl 12) / (-i_57_ + 4096)) else 4096)
                                        i_54_++
                                    }
                                    break@while_178_
                                } while (false)
                                i_54_ = 0
                                while (i_54_ < TextureOpPolarDistortion.anInt9139) {
                                    val i_58_ = is_52_!![i_54_]
                                    `is`!![i_54_] = (if (i_58_ == 0) 0 else -((4096 - is_53_!![i_54_] shl 12) / i_58_) + 4096)
                                    i_54_++
                                }
                                break@while_178_
                            } while (false)
                            i_54_ = 0
                            while (i_54_ < TextureOpPolarDistortion.anInt9139) {
                                val i_59_ = is_53_!![i_54_]
                                val i_60_ = is_52_!![i_54_]
                                `is`!![i_54_] = (min(i_59_, i_60_))
                                i_54_++
                            }
                            break@while_178_
                        } while (false)
                        i_54_ = 0
                        while (TextureOpPolarDistortion.anInt9139 > i_54_) {
                            val i_61_ = is_53_!![i_54_]
                            val i_62_ = is_52_!![i_54_]
                            `is`!![i_54_] = max(i_62_, i_61_)
                            i_54_++
                        }
                        break@while_178_
                    } while (false)
                    i_54_ = 0
                    while (i_54_ < TextureOpPolarDistortion.anInt9139) {
                        val i_63_ = is_53_!![i_54_]
                        val i_64_ = is_52_!![i_54_]
                        `is`!![i_54_] = (if (i_63_ >= i_64_) i_63_ + -i_64_ else i_64_ - i_63_)
                        i_54_++
                    }
                    break@while_178_
                } while (false)
                i_54_ = 0
                while (TextureOpPolarDistortion.anInt9139 > i_54_) {
                    val i_65_ = is_53_!![i_54_]
                    val i_66_ = is_52_!![i_54_]
                    `is`!![i_54_] = i_66_ - -i_65_ - (i_66_ * i_65_ shr 11)
                    i_54_++
                }
            }
        } while (false)
        return `is`!!
    }
}
