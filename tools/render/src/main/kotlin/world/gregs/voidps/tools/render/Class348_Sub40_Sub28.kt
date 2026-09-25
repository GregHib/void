package world.gregs.voidps.tools.render

import java.util.*

internal class Class348_Sub40_Sub28 : Class348_Sub40(0, true) {
    private var anInt9362 = 2000
    private var anInt9364 = 4096
    private var anInt9367 = 0
    private var anInt9368 = 16
    private var anInt9369 = 0
    override fun method3044() {
        Class348_Sub40_Sub6.method1605()
    }

    override fun method3049(packet: Packet?, i: Int) {
        val i_1_ = i
        while_204_@ do {
            while_203_@ do {
                while_202_@ do {
                    do {
                        if (i_1_ == 0) {
                            anInt9367 = packet!!.readUnsignedByte()
                            return
                        } else if (i_1_ != 1) {
                            if (i_1_ != 2) {
                                if (i_1_ != 3) {
                                    if (i_1_ == 4) break@while_203_
                                    break@while_204_
                                }
                            } else break
                            break@while_202_
                        }
                        anInt9362 = packet!!.readUnsignedShort()
                        return
                    } while (false)
                    anInt9368 = packet!!.readUnsignedByte()
                    return
                } while (false)
                anInt9369 = packet!!.readUnsignedShort()
                return
            } while (false)
            anInt9364 = packet!!.readUnsignedShort()
        } while (false)
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_3_ = anInt9364 shr 1
            val is_4_ = this.aClass191_7032!!.method1427()
            val random = Random(anInt9367.toLong())
            var i_5_ = 0
            while (anInt9362 > i_5_) {
                var i_6_ = (if (anInt9364 > 0) anInt9369 + Mesh.method1097(anInt9364, random) + -i_3_ else anInt9369)
                i_6_ = i_6_ shr 4 and 0xff
                var i_7_: Int = Mesh.method1097(Class348_Sub40_Sub6.anInt9139, random)
                var i_8_: Int = Mesh.method1097(Class348_Sub40_Sub33.anInt6212, random)
                var i_9_ = i_7_ - -(anInt9368 * Class348_Sub40_Sub6.anIntArray4654!![i_6_] shr 12)
                var i_10_ = ((Class348_Sub40_Sub6.anIntArray3068!![i_6_] * anInt9368 shr 12) + i_8_)
                var i_11_ = -i_8_ + i_10_
                var i_12_ = -i_7_ + i_9_
                if (i_12_ != 0 || i_11_ != 0) {
                    if (i_11_ < 0) i_11_ = -i_11_
                    if (i_12_ < 0) i_12_ = -i_12_
                    val bool = i_12_ < i_11_
                    if (bool) {
                        val i_13_ = i_7_
                        val i_14_ = i_9_
                        i_7_ = i_8_
                        i_8_ = i_13_
                        i_9_ = i_10_
                        i_10_ = i_14_
                    }
                    if (i_9_ < i_7_) {
                        val i_15_ = i_7_
                        i_7_ = i_9_
                        val i_16_ = i_8_
                        i_9_ = i_15_
                        i_8_ = i_10_
                        i_10_ = i_16_
                    }
                    var i_17_ = i_8_
                    val i_18_ = -i_7_ + i_9_
                    var i_19_ = -i_8_ + i_10_
                    var i_20_ = -i_18_ / 2
                    val i_21_ = 2048 / i_18_
                    val i_22_: Int = 1024 - (Mesh.method1097(4096, random) shr 2)
                    if (i_19_ < 0) i_19_ = -i_19_
                    val i_23_ = if (i_10_ <= i_8_) -1 else 1
                    for (i_24_ in i_7_..<i_9_) {
                        val i_25_ = (i_24_ - i_7_) * i_21_ + (i_22_ + 1024)
                        val i_26_ = Class348_Sub40_Sub37.anInt6076 and i_24_
                        val i_27_ = i_17_ and Class348_Sub40_Sub6.anInt6325
                        if (bool) is_4_!![i_27_]!![i_26_] = i_25_
                        else is_4_!![i_26_]!![i_27_] = i_25_
                        i_20_ += i_19_
                        if (i_20_ > 0) {
                            i_17_ -= -i_23_
                            i_20_ = -i_18_ + i_20_
                        }
                    }
                }
                i_5_++
            }
        }
        return `is`
    }
}
