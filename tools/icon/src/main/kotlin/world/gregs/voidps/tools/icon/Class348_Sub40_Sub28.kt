package world.gregs.voidps.tools.icon

import java.util.*

/* Class348_Sub40_Sub28 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub28 : Class348_Sub40(0, true) {
    private var anInt9362 = 2000
    private var anInt9364 = 4096
    private var anInt9367 = 0
    private var anInt9368 = 16
    private var anInt9369 = 0
    override fun method3044(i: Int) {
        anInt9363++
        Class220.method1605(26188)
        if (i < 108) Companion.method3122((-111).toByte())
    }

    override fun method3049(packet: Packet?, i: Int, i_0_: Int) {
        anInt9370++
        if (i_0_ != 31015) anInt9364 = -29
        val i_1_ = i
        while_204_@ do {
            while_203_@ do {
                while_202_@ do {
                    do {
                        if (i_1_ == 0) {
                            anInt9367 = packet!!.readUnsignedByte(255)
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
                        anInt9362 = packet!!.readUnsignedShort(842397944)
                        return
                    } while (false)
                    anInt9368 = packet!!.readUnsignedByte(255)
                    return
                } while (false)
                anInt9369 = packet!!.readUnsignedShort(842397944)
                return
            } while (false)
            anInt9364 = packet!!.readUnsignedShort(842397944)
        } while (false)
    }

    override fun method3042(i: Int, i_2_: Int): IntArray? {
        anInt9366++
        if (i_2_ != 255) method3044(-48)
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_3_ = anInt9364 shr 1
            val is_4_ = this.aClass191_7032!!.method1427(16.toByte())
            val random = Random(anInt9367.toLong())
            var i_5_ = 0
            while (anInt9362 > i_5_) {
                var i_6_ = (if (anInt9364 > 0) anInt9369 + Mesh.Companion.method1097(92.toByte(), anInt9364, random) + -i_3_ else anInt9369)
                i_6_ = i_6_ shr 4 and 0xff
                var i_7_: Int = Mesh.Companion.method1097(81.toByte(), Class348_Sub40_Sub6.Companion.anInt9139, random)
                var i_8_: Int = Mesh.Companion.method1097(123.toByte(), Class286_Sub2.anInt6212, random)
                var i_9_ = i_7_ - -(anInt9368 * Class127.anIntArray4654!![i_6_] shr 12)
                var i_10_ = ((Class235.anIntArray3068!![i_6_] * anInt9368 shr 12) + i_8_)
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
                    val i_22_: Int = 1024 - (Mesh.Companion.method1097(90.toByte(), 4096, random) shr 2)
                    if (i_19_ < 0) i_19_ = -i_19_
                    val i_23_ = if (i_10_ <= i_8_) -1 else 1
                    for (i_24_ in i_7_..<i_9_) {
                        val i_25_ = (i_24_ - i_7_) * i_21_ + (i_22_ + 1024)
                        val i_26_ = Class239_Sub22.anInt6076 and i_24_
                        val i_27_ = i_17_ and Class299_Sub2.anInt6325
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

    companion object {
        var anInt9361: Int = 0
        var anInt9363: Int = 0
        var aJs5_9365: Js5? = null
        var anInt9366: Int = 0
        var anInt9370: Int = 0

        /* NOTE: method3122 is NOT in the genuine-methods list (0 JaCoCo hits).
     * Its only caller, method3044, guards it behind "if (i < 108)" which is
     * never true for this renderer's usage. Stubbed rather than pulling in
     * Class55_Sub1/Class26/Class108/Class76/Class342's aClass364 fields and
     * Class367_Sub8 (owned by a parallel agent, off limits) to compile a
     * path that is provably dead here. */
        fun method3122(i: Byte): Array<Class364?>? {
            if (i < 86) aJs5_9365 = null
            anInt9361++
            throw IllegalStateException() // unreachable per JaCoCo coverage
        }
    }
}
