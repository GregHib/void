package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub24 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub24 : Class348_Sub40(0, true) {
    private var anInt9325 = 0
    private var anInt9329 = 10
    private var anIntArray9332: IntArray? = null
    private var anIntArray9333: IntArray? = null
    private var anInt9334 = 2048

    override fun method3042(i: Int, i_0_: Int): IntArray {
        if (i_0_ != 255) anIntArray9333 = null
        anInt9328++
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_1_ = Class239_Sub18.anIntArray6035!![i]
            if (anInt9325 == 0) {
                var i_7_ = 0
                for (i_8_ in 0..<anInt9329) {
                    if (i_1_ >= anIntArray9332!![i_8_] && (i_1_ < anIntArray9332!![i_8_ + 1])) {
                        if (anIntArray9333!![i_8_] > i_1_) i_7_ = 4096
                        break
                    }
                }
                Class214.method1579(`is`!!, 0, Class348_Sub40_Sub6.Companion.anInt9139, i_7_)
            } else {
                for (i_2_ in 0..<Class348_Sub40_Sub6.Companion.anInt9139) {
                    var i_3_ = 0
                    var i_4_ = 0
                    val i_5_ = Class318_Sub6.anIntArray6432!![i_2_]
                    var i_6_ = anInt9325
                    while_197_@ do {
                        do {
                            if (i_6_ == 1) {
                                i_3_ = i_5_
                                break@while_197_
                            } else if (i_6_ != 2) {
                                if (i_6_ == 3) break
                                break@while_197_
                            }
                            i_3_ = ((i_5_ + (-4096 - -i_1_) shr 1) + 2048)
                            break@while_197_
                        } while (false)
                        i_3_ = 2048 + (i_5_ - i_1_ shr 1)
                    } while (false)
                    i_6_ = 0
                    while (i_6_ < anInt9329) {
                        if (i_3_ >= anIntArray9332!![i_6_] && i_3_ < anIntArray9332!![1 + i_6_]) {
                            if (i_3_ < anIntArray9333!![i_6_]) i_4_ = 4096
                            break
                        }
                        i_6_++
                    }
                    `is`!![i_2_] = i_4_
                }
            }
        }
        return `is`!!
    }

    override fun method3044(i: Int) {
        if (i < 108) method3049(null, -68, -1)
        anInt9330++
        method3116((-111).toByte())
    }

    override fun method3049(packet: Packet?, i: Int, i_9_: Int) {
        anInt9331++
        val i_10_ = i
        while_198_@ do {
            do {
                if (i_10_ == 0) {
                    anInt9329 = packet!!.readUnsignedByte(255)
                    break@while_198_
                } else if (i_10_ != 1) {
                    if (i_10_ == 2) break
                    break@while_198_
                }
                anInt9334 = packet!!.readUnsignedShort(842397944)
                break@while_198_
            } while (false)
            anInt9325 = packet!!.readUnsignedByte(i_9_ xor 0x79d8)
        } while (false)
        if (i_9_ != 31015) method3049(null, 38, 1)
    }

    private fun method3116(i: Byte) {
        anInt9326++
        var i_11_ = 0
        anIntArray9333 = IntArray(anInt9329 - -1)
        anIntArray9332 = IntArray(anInt9329 - -1)
        val i_12_ = 4096 / anInt9329
        val i_13_ = i_12_ * anInt9334 shr 12
        var i_14_ = 0
        while (anInt9329 > i_14_) {
            anIntArray9332!![i_14_] = i_11_
            anIntArray9333!![i_14_] = i_13_ + i_11_
            i_11_ += i_12_
            i_14_++
        }
        if (i.toInt() != -111) anIntArray9332 = null
        anIntArray9332!![anInt9329] = 4096
        anIntArray9333!![anInt9329] = 4096 - -anIntArray9333!![0]
    }

    companion object {
        var anInt9326: Int = 0
        var anInt9328: Int = 0
        var anInt9330: Int = 0
        var anInt9331: Int = 0
    }
}
