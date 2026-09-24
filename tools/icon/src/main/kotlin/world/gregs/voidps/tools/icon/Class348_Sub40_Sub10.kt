package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub10 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub10 : Class348_Sub40(1, false) {
    private var anInt9175 = 3072
    private var anInt9176 = 1024
    private var anInt9182 = 2048

    override fun method3049(packet: Packet?, i: Int, i_0_: Int) {
        anInt9179++
        val i_1_ = i
        while_152_@ do {
            do {
                if (i_1_ == 0) {
                    anInt9176 = packet!!.readUnsignedShort(i_0_ xor 0x323581df)
                    break@while_152_
                } else if (i_1_ != 1) {
                    if (i_1_ == 2) break
                    break@while_152_
                }
                anInt9175 = packet!!.readUnsignedShort(842397944)
                break@while_152_
            } while (false)
            this.aBoolean7045 = packet!!.readUnsignedByte(255) == 1
        } while (false)
        if (i_0_ != 31015) method3049(null, -85, -85)
    }

    override fun method3044(i: Int) {
        anInt9174++
        anInt9182 = anInt9175 - anInt9176
        if (i < 108) method3042(-31, 9)
    }

    override fun method3042(i: Int, i_2_: Int): IntArray? {
        anInt9177++
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_3_ = this.method3048(i, 633706337, 0)
            var i_4_ = 0
            while (Class348_Sub40_Sub6.Companion.anInt9139 > i_4_) {
                `is`!![i_4_] = anInt9176 - -(anInt9182 * is_3_!![i_4_] shr 12)
                i_4_++
            }
        }
        if (i_2_ != 255) return null
        return `is`
    }

    override fun method3047(i: Int, i_5_: Int): Array<IntArray> {
        anInt9178++
        val `is` = this.aClass322_7033!!.method2557(-113, i)
        if (i_5_ != -1564599039) method3049(null, -10, -112)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_6_ = this.method3039((-55).toByte(), i, 0)
            val is_7_ = is_6_!![0]
            val is_8_ = is_6_[1]
            val is_9_ = is_6_[2]
            val is_10_ = `is`!![0]
            val is_11_ = `is`[1]
            val is_12_ = `is`[2]
            var i_13_ = 0
            while ((Class348_Sub40_Sub6.Companion.anInt9139 > i_13_)) {
                is_10_[i_13_] = (anInt9182 * is_7_[i_13_] shr 12) + anInt9176
                is_11_[i_13_] = (anInt9182 * is_8_[i_13_] shr 12) + anInt9176
                is_12_[i_13_] = (is_9_[i_13_] * anInt9182 shr 12) + anInt9176
                i_13_++
            }
        }
        return `is`!!
    }

    companion object {
        var anInt9174: Int = 0
        var anInt9177: Int = 0
        var anInt9178: Int = 0
        var anInt9179: Int = 0
    }
}
