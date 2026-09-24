package world.gregs.voidps.tools.icon

import kotlin.math.max
import kotlin.math.min

/* Class348_Sub40_Sub30 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub30 : Class348_Sub40(1, false) {
    private var anInt9386 = 0
    private var anInt9389 = 0
    private var anInt9390 = 0
    private var anInt9392 = 0
    private var anInt9396 = 0
    private var anInt9398 = 0
    private var anInt9400 = 0
    private var anInt9401 = 0
    private var anInt9402 = 0

    override fun method3047(i: Int, i_8_: Int): Array<IntArray> {
        anInt9393++
        val `is` = this.aClass322_7033.method2557(-105, i)
        if (this.aClass322_7033.aBoolean4035) {
            val is_9_ = this.method3039(50.toByte(), i, 0)
            val is_10_ = is_9_[0]
            val is_11_ = is_9_[1]
            val is_12_ = is_9_[2]
            val is_13_ = `is`[0]
            val is_14_ = `is`[1]
            val is_15_ = `is`[2]
            var i_16_ = 0
            while ((i_16_ < Class348_Sub40_Sub6.Companion.anInt9139)) {
                method3129(is_10_[i_16_], is_11_[i_16_], 82.toByte(), is_12_[i_16_])
                anInt9401 += anInt9390
                anInt9400 += anInt9402
                anInt9389 += anInt9398
                while ( /**/anInt9400 < 0) {
                    anInt9400 += 4096
                }
                if (anInt9401 < 0) anInt9401 = 0
                while ( /**/anInt9400 > 4096) {
                    anInt9400 -= 4096
                }
                if (anInt9389 < 0) anInt9389 = 0
                if (anInt9401 > 4096) anInt9401 = 4096
                if (anInt9389 > 4096) anInt9389 = 4096
                method3130(anInt9389, anInt9400, -120.toByte(), anInt9401)
                is_13_[i_16_] = anInt9386
                is_14_[i_16_] = anInt9396
                is_15_[i_16_] = anInt9392
                i_16_++
            }
        }
        if (i_8_ != -1564599039) anInt9399 = 25
        return `is`
    }

    private fun method3129(i: Int, i_17_: Int, i_18_: Byte, i_19_: Int) {
        anInt9394++
        var i_20_ = max(i_17_, i)
        if (i_18_.toInt() == 82) {
            i_20_ = max(i_19_, i_20_)
            var i_21_ = min(i, i_17_)
            i_21_ = min(i_19_, i_21_)
            anInt9389 = (i_20_ + i_21_) / 2
            val i_22_ = i_20_ - i_21_
            if (i_22_ > 0) {
                val i_23_ = (i_20_ + -i shl 12) / i_22_
                val i_24_ = (i_20_ + -i_17_ shl 12) / i_22_
                val i_25_ = (-i_19_ + i_20_ shl 12) / i_22_
                if (i == i_20_) anInt9400 = (if (i_17_ != i_21_) 4096 + -i_24_ else i_25_ + 20480)
                else if (i_17_ != i_20_) anInt9400 = if (i != i_21_) -i_23_ + 20480 else 12288 - -i_24_
                else anInt9400 = (if (i_21_ == i_19_) 4096 + i_23_ else -i_25_ + 12288)
                anInt9400 /= 6
            } else anInt9400 = 0
            if (anInt9389 > 0 && anInt9389 < 4096) anInt9401 = (i_22_ shl 12) / (if (anInt9389 > 2048) 8192 - anInt9389 * 2 else anInt9389 * 2)
            else anInt9401 = 0
        }
    }

    private fun method3130(i: Int, i_26_: Int, i_27_: Byte, i_28_: Int) {
        var i_26_ = i_26_
        anInt9397++
        val i_29_ = 31 / ((i_27_ - -74) / 40)
        val i_30_ = (if (i > 2048) i_28_ + (i - (i * i_28_ shr 12)) else i * (4096 - -i_28_) shr 12)
        if (i_30_ <= 0) {
            anInt9392 = i
            anInt9396 = anInt9392
            anInt9386 = anInt9396
        } else {
            i_26_ *= 6
            val i_31_ = -i_30_ + i - -i
            val i_32_ = (-i_31_ + i_30_ shl 12) / i_30_
            val i_33_ = i_26_ shr 12
            val i_34_ = i_26_ + -(i_33_ shl 12)
            var i_35_ = i_30_
            i_35_ = i_35_ * i_32_ shr 12
            i_35_ = i_35_ * i_34_ shr 12
            val i_36_ = i_35_ + i_31_
            val i_37_ = i_30_ - i_35_
            val i_38_ = i_33_
            while_208_@ do {
                while_207_@ do {
                    while_206_@ do {
                        while_205_@ do {
                            do {
                                if (i_38_ == 0) {
                                    anInt9396 = i_36_
                                    anInt9386 = i_30_
                                    anInt9392 = i_31_
                                    return
                                } else if (i_38_ != 1) {
                                    if (i_38_ != 2) {
                                        if (i_38_ != 3) {
                                            if (i_38_ != 4) {
                                                if (i_38_ != 5) break@while_208_
                                            } else break@while_206_
                                            break@while_207_
                                        }
                                    } else break
                                    break@while_205_
                                }
                                anInt9392 = i_31_
                                anInt9386 = i_37_
                                anInt9396 = i_30_
                                return
                            } while (false)
                            anInt9392 = i_36_
                            anInt9386 = i_31_
                            anInt9396 = i_30_
                            return
                        } while (false)
                        anInt9396 = i_37_
                        anInt9386 = i_31_
                        anInt9392 = i_30_
                        return
                    } while (false)
                    anInt9396 = i_31_
                    anInt9386 = i_36_
                    anInt9392 = i_30_
                    return
                } while (false)
                anInt9386 = i_30_
                anInt9392 = i_37_
                anInt9396 = i_31_
            } while (false)
        }
    }

    override fun method3049(packet: Packet?, i: Int, i_39_: Int) {
        while_209_@ do {
            try {
                anInt9384++
                if (i_39_ == 31015) {
                    val i_40_ = i
                    do {
                        if (i_40_ == 0) {
                            anInt9402 = packet!!.readShort(13638)
                            return
                        } else if (i_40_ != 1) {
                            if (i_40_ == 2) break
                            break@while_209_
                        }
                        anInt9390 = (packet!!.readByte(-83).toInt() shl 12) / 100
                        return
                    } while (false)
                    anInt9398 = (packet!!.readByte(i_39_ + -31101).toInt() shl 12) / 100
                    break
                }
                break
            } catch (runtimeexception: RuntimeException) {
                throw Class348_Sub17.method2929(runtimeexception, ("vj.F(" + (if (packet != null) "{...}" else "null") + ',' + i + ',' + i_39_ + ')'))
            }
        } while (false)
    }

    companion object {
        var anInt9384: Int = 0
        var anInt9393: Int = 0
        var anInt9394: Int = 0
        var anInt9397: Int = 0
        var anInt9399: Int = -1
    }
}
