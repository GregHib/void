package world.gregs.voidps.tools.render

internal class Class348_Sub40_Sub37 : Class348_Sub40(1, false) {
    private var anInt9463 = 1
    private var anInt9466 = 1
    override fun method3049(packet: Packet?, i: Int) {
        val i_12_ = i
        while_212_@ do {
            do {
                if (i_12_ == 0) {
                    anInt9466 = packet!!.readUnsignedByte()
                    return
                } else if (i_12_ != 1) {
                    if (i_12_ == 2) break
                    break@while_212_
                }
                anInt9463 = packet!!.readUnsignedByte()
                return
            } while (false)
            this.aBoolean7045 = packet!!.readUnsignedByte() == 1
        } while (false)
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val i_14_ = 1 + (anInt9463 + anInt9463)
            val i_15_ = 65536 / i_14_
            val i_16_ = 1 + anInt9466 + anInt9466
            val i_17_ = 65536 / i_16_
            val is_18_ = arrayOfNulls<IntArray>(i_14_)
            for (i_19_ in -anInt9463 + i..i - -anInt9463) {
                val is_20_ = this.method3048(Class348_Sub40_Sub6.anInt6325 and i_19_, 0)!!
                val is_21_ = IntArray(Class348_Sub40_Sub6.anInt9139)
                var i_22_ = 0
                for (i_23_ in -anInt9466..anInt9466) i_22_ += is_20_[i_23_ and anInt6076]
                var i_24_ = 0
                while (i_24_ < Class348_Sub40_Sub6.anInt9139) {
                    is_21_[i_24_] = i_17_ * i_22_ shr 16
                    i_22_ -= (is_20_[-anInt9466 + i_24_ and anInt6076])
                    i_24_++
                    i_22_ += (is_20_[i_24_ + anInt9466 and anInt6076])
                }
                is_18_[-i + (i_19_ + anInt9463)] = is_21_
            }
            var i_25_ = 0
            while (Class348_Sub40_Sub6.anInt9139 > i_25_) {
                var i_26_ = 0
                for (i_27_ in 0..<i_14_) i_26_ += is_18_[i_27_]!![i_25_]
                `is`!![i_25_] = i_26_ * i_15_ shr 16
                i_25_++
            }
        }
        return `is`
    }

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val i_29_ = 1 + (anInt9463 + anInt9463)
            val i_30_ = 65536 / i_29_
            val i_31_ = anInt9466 + (anInt9466 + 1)
            val i_32_ = 65536 / i_31_
            val is_33_ = arrayOfNulls<Array<IntArray>>(i_29_)
            for (i_34_ in i + -anInt9463..anInt9463 + i) {
                val is_35_ = this.method3039(Class348_Sub40_Sub6.anInt6325 and i_34_, 0)
                val is_36_: Array<IntArray> = Array(3) { IntArray(Class348_Sub40_Sub6.anInt9139) }
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
                while (Class348_Sub40_Sub6.anInt9139 > i_48_) {
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
            while (Class348_Sub40_Sub6.anInt9139 > i_53_) {
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
    }
}
