package world.gregs.voidps.tools.render

/* Class348_Sub40 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

abstract class Class348_Sub40(i: Int, var aBoolean7045: Boolean) {
    var aClass348_Sub40Array7031: Array<Class348_Sub40?> = arrayOfNulls(i)
    var aClass191_7032: Class191? = null
    var aClass322_7033: Class322? = null
    var anInt7036: Int = 0

    open fun method3037(): Int {
        return -1
    }

    fun method3039(i_1_: Int, i_2_: Int): Array<IntArray>? {
        if (this.aClass348_Sub40Array7031[i_2_]!!.aBoolean7045) {
            val `is` = this.aClass348_Sub40Array7031[i_2_]!!.method3042(i_1_)!!
            return arrayOf(`is`, `is`, `is`)
        }
        return this.aClass348_Sub40Array7031[i_2_]!!.method3047(i_1_)
    }

    open fun method3042(i: Int): IntArray? {
        throw IllegalStateException("This operation does not have a monochrome output")
    }

    open fun method3043(): Int {
        return -1
    }

    open fun method3044() {
    }

    open fun method3045(i: Int, i_54_: Int) {
        val i_56_ = (if (this.anInt7036 != 255) this.anInt7036 else i_54_)
        if (this.aBoolean7045) this.aClass191_7032 = Class191(i_56_, i_54_, i)
        else this.aClass322_7033 = Class322(i_56_, i_54_, i)
    }

    open fun method3046() {
        if (this.aBoolean7045) {
            this.aClass191_7032!!.method1432()
            this.aClass191_7032 = null
        } else {
            this.aClass322_7033!!.method2558()
            this.aClass322_7033 = null
        }
    }

    open fun method3047(i: Int): Array<IntArray>? {
        throw IllegalStateException("This operation does not have a colour output")
    }

    fun method3048(i: Int, i_59_: Int): IntArray? {
        if (!this.aClass348_Sub40Array7031[i_59_]!!.aBoolean7045) return (this.aClass348_Sub40Array7031[i_59_]!!.method3047(i)!![0])
        return this.aClass348_Sub40Array7031[i_59_]!!.method3042(i)
    }

    open fun method3049(packet: Packet?, i: Int) {
    }

    companion object {
        /** Class59_Sub1_Sub1.method557 */
        internal fun method557(i: Int): Class348_Sub40? = when (i) {
            0 -> Class348_Sub40_Sub15()
            1 -> Class348_Sub40_Sub18()
            2 -> Class348_Sub40_Sub19()
            3 -> Class348_Sub40_Sub4()
            4 -> Class348_Sub40_Sub22()
            5 -> Class348_Sub40_Sub37()
            6 -> Class348_Sub40_Sub38()
            7 -> Class348_Sub40_Sub16()
            8 -> Class348_Sub40_Sub14()
            9 -> Class348_Sub40_Sub7()
            10 -> Class348_Sub40_Sub12()
            11 -> Class348_Sub40_Sub26()
            12 -> Class348_Sub40_Sub36()
            13 -> Class348_Sub40_Sub20()
            14 -> Class348_Sub40_Sub11()
            15 -> Class348_Sub40_Sub5()
            16 -> Class348_Sub40_Sub2()
            17 -> Class348_Sub40_Sub30()
            18 -> Class348_Sub40_Sub17_Sub1()
            19 -> Class348_Sub40_Sub6()
            20 -> Class348_Sub40_Sub31()
            21 -> Class348_Sub40_Sub27()
            22 -> Class348_Sub40_Sub32()
            23 -> Class348_Sub40_Sub33()
            24 -> Class348_Sub40_Sub13()
            25 -> Class348_Sub40_Sub1()
            26 -> Class348_Sub40_Sub3()
            27 -> Class348_Sub40_Sub24()
            28 -> Class348_Sub40_Sub23()
            29 -> Class348_Sub40_Sub39()
            30 -> Class348_Sub40_Sub10()
            31 -> Class348_Sub40_Sub25()
            32 -> Class348_Sub40_Sub35()
            33 -> Class348_Sub40_Sub34()
            34 -> Class348_Sub40_Sub8()
            35 -> Class348_Sub40_Sub9()
            36 -> Class348_Sub40_Sub29()
            37 -> Class348_Sub40_Sub21()
            38 -> Class348_Sub40_Sub28()
            39 -> Class348_Sub40_Sub17()
            else -> null
        }

        fun method3031(packet: Packet): Class348_Sub40? {
            packet.readUnsignedByte()
            val i_0_ = packet.readUnsignedByte()
            val class348_sub40 = method557(i_0_)
            class348_sub40!!.anInt7036 = packet.readUnsignedByte()
            val i_1_ = packet.readUnsignedByte()
            var i_2_ = 0
            while (i_1_ > i_2_) {
                val i_3_ = packet.readUnsignedByte()
                class348_sub40.method3049(packet, i_3_)
                i_2_++
            }
            class348_sub40.method3044()
            return class348_sub40
        }
    }
}
