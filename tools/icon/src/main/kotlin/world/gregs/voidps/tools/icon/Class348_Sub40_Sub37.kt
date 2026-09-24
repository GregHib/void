package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub37 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub37 : Class348_Sub40(1, false) {
    private var anInt9463 = 1
    private var anInt9466 = 1
    override fun method3049(packet: Packet?, i: Int, i_11_: Int) {
        anInt9460++
        if (i_11_ != 31015) method3147(32.toByte())
        val i_12_ = i
        while_212_@ do {
            do {
                if (i_12_ == 0) {
                    anInt9466 = packet!!.readUnsignedByte(255)
                    return
                } else if (i_12_ != 1) {
                    if (i_12_ == 2) break
                    break@while_212_
                }
                anInt9463 = packet!!.readUnsignedByte(255)
                return
            } while (false)
            this.aBoolean7045 = packet!!.readUnsignedByte(i_11_ xor 0x79d8) == 1
        } while (false)
    }

    override fun method3047(i: Int, i_28_: Int): Array<IntArray>? {
        if (i_28_ != -1564599039) return null
        anInt9462++
        val `is` = this.aClass322_7033!!.method2557(i_28_ xor 0x5d41e2a6, i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val i_29_ = 1 + (anInt9463 + anInt9463)
            val i_30_ = 65536 / i_29_
            val i_31_ = anInt9466 + (anInt9466 + 1)
            val i_32_ = 65536 / i_31_
            val is_33_ = arrayOfNulls<Array<IntArray>>(i_29_)
            for (i_34_ in i + -anInt9463..anInt9463 + i) {
                val is_35_ = this.method3039(55.toByte(), Class348_Sub40_Sub6.anInt6325 and i_34_, 0)
                val is_36_: Array<IntArray> = Array(3) { IntArray(Class348_Sub40_Sub6.Companion.anInt9139) }
                var i_37_ = 0
                var i_38_ = 0
                var i_39_ = 0
                val is_40_ = is_35_!![0]
                val is_41_ = is_35_[1]
                val is_42_ = is_35_[2]
                for (i_43_ in -anInt9466..anInt9466) {
                    val i_44_ = anInt6076 and i_43_
                    i_39_ += is_42_[i_44_]
                    i_37_ += is_40_[i_44_]
                    i_38_ += is_41_[i_44_]
                }
                val is_45_ = is_36_[0]
                val is_46_ = is_36_[1]
                val is_47_ = is_36_[2]
                var i_48_ = 0
                while (Class348_Sub40_Sub6.Companion.anInt9139 > i_48_) {
                    is_45_[i_48_] = i_37_ * i_32_ shr 16
                    is_46_[i_48_] = i_38_ * i_32_ shr 16
                    is_47_[i_48_] = i_39_ * i_32_ shr 16
                    var i_49_ = anInt6076 and -anInt9466 + i_48_
                    i_37_ -= is_40_[i_49_]
                    i_48_++
                    i_38_ -= is_41_[i_49_]
                    i_39_ -= is_42_[i_49_]
                    i_49_ = i_48_ - -anInt9466 and anInt6076
                    i_39_ += is_42_[i_49_]
                    i_38_ += is_41_[i_49_]
                    i_37_ += is_40_[i_49_]
                }
                is_33_[i_34_ + anInt9463 + -i] = is_36_
            }
            val is_50_ = `is`!![0]
            val is_51_ = `is`[1]
            val is_52_ = `is`[2]
            var i_53_ = 0
            while (Class348_Sub40_Sub6.Companion.anInt9139 > i_53_) {
                var i_54_ = 0
                var i_55_ = 0
                var i_56_ = 0
                for (i_57_ in 0..<i_29_) {
                    val is_58_ = is_33_[i_57_]
                    i_54_ += is_58_!![0][i_53_]
                    i_55_ += is_58_[1][i_53_]
                    i_56_ += is_58_[2][i_53_]
                }
                is_50_[i_53_] = i_54_ * i_30_ shr 16
                is_51_[i_53_] = i_55_ * i_30_ shr 16
                is_52_[i_53_] = i_30_ * i_56_ shr 16
                i_53_++
            }
        }
        return `is`
    }

    companion object {
        var anInt6076: Int = 0
        var anInt9459: Int = 0
        var anInt9464: Int = 0
        var aLongArray9465: LongArray? = null
        var aSpriteArray9467: Array<Sprite?>? = null
        var anIntArray9458: IntArray?
        var anInt9460: Int = 0
        var anInt9462: Int = 0

        fun method3146(i: Int, i_0_: Int, i_1_: Byte, i_2_: Int, i_3_: Int, i_4_: Int, `is`: ByteArray?, is_5_: ByteArray?, i_6_: Int) {
            var i_0_ = i_0_
            var i_4_ = i_4_
            var i_6_ = i_6_
            try {
                anInt9464++
                val i_7_ = -(i_0_ shr 2)
                i_0_ = -(i_0_ and 0x3)
                if (i_1_ >= -12) method3148(true)
                for (i_8_ in -i_3_..-1) {
                    for (i_9_ in i_7_..-1) {
                        `is`!![i_4_++] = (`is`[i_4_++] + -is_5_!![i_6_++]).toByte()
                        `is`[i_4_++] = (`is`[i_4_++] + -is_5_[i_6_++]).toByte()
                        `is`[i_4_++] = (`is`[i_4_++] + -is_5_[i_6_++]).toByte()
                        `is`[i_4_++] = (`is`[i_4_++] + -is_5_[i_6_++]).toByte()
                    }
                    for (i_10_ in i_0_..-1) `is`!![i_4_++] = (`is`[i_4_++] + -is_5_!![i_6_++]).toByte()
                    i_4_ += i
                    i_6_ += i_2_
                }
            } catch (runtimeexception: RuntimeException) {
                throw ItemType.method2929(runtimeexception, ("fd.D(" + i + ',' + i_0_ + ',' + i_1_ + ',' + i_2_ + ',' + i_3_ + ',' + i_4_ + ',' + (if (`is` != null) "{...}" else "null") + ',' + (if (is_5_ != null) "{...}" else "null") + ',' + i_6_ + ')'))
            }
        }

        /* NOTE: method3147 is NOT in the genuine-methods list (0 JaCoCo hits)
     * for this renderer, so it is stubbed rather than pulling in
     * Class248/Class97/Class259's world-state fields to compile a path
     * that never executes here. */
        fun method3147(i: Byte) {
            if (i.toInt() != 27) method3146(-85, 87, 91.toByte(), 46, -77, 54, null, null, -117)
            anInt9459++
            throw IllegalStateException() // unreachable per JaCoCo coverage
        }

        fun method3148(bool: Boolean) {
            anIntArray9458 = null
            aLongArray9465 = null
            if (bool != true) Companion.method3146(79, -54, (-128).toByte(), -109, 85, -107, null, null, 58)
            aSpriteArray9467 = null
        }

        init {
            anIntArray9458 = IntArray(1000)
        }
    }
}
