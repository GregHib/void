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
        fun method3031(packet: Packet): Class348_Sub40? {
            packet.readUnsignedByte()
            val i_0_ = packet.readUnsignedByte()
            val class348_sub40 = Class59_Sub1_Sub1.method557(i_0_)
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
