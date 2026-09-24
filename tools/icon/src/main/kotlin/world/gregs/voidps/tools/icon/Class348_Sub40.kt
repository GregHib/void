package world.gregs.voidps.tools.icon

/* Class348_Sub40 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal abstract class Class348_Sub40(i: Int, var aBoolean7045: Boolean) {
    var aClass348_Sub40Array7031: Array<Class348_Sub40?>
    var aClass191_7032: Class191? = null
    var aClass322_7033: Class322? = null
    var anInt7036: Int = 0

    open fun method3037(i: Int): Int {
        if (i >= -113) method3048(-125, -85, 60)
        anInt7027++
        return -1
    }

    fun method3039(i: Byte, i_1_: Int, i_2_: Int): Array<IntArray>? {
        anInt7039++
        val i_3_ = 9 / ((6 - i) / 37)
        if (this.aClass348_Sub40Array7031[i_2_]!!.aBoolean7045) {
            val `is` = this.aClass348_Sub40Array7031[i_2_]!!.method3042(i_1_, 255)!!
            return arrayOf(`is`, `is`, `is`)
        }
        return this.aClass348_Sub40Array7031[i_2_]!!.method3047(i_1_, -1564599039)
    }

    open fun method3042(i: Int, i_53_: Int): IntArray? {
        if (i_53_ != 255) return null
        anInt7035++
        throw IllegalStateException("This operation does not have a monochrome output")
    }

    open fun method3043(i: Int): Int {
        anInt7037++
        if (i != -1) this.aClass322_7033 = null
        return -1
    }

    open fun method3044(i: Int) {
        if (i <= 108) this.aClass191_7032 = null
        anInt7029++
    }

    open fun method3045(i: Int, i_54_: Int, i_55_: Int) {
        anInt7043++
        val i_56_ = (if (i_55_ != (this.anInt7036.inv())) this.anInt7036 else i_54_)
        if (this.aBoolean7045) this.aClass191_7032 = Class191(i_56_, i_54_, i)
        else this.aClass322_7033 = Class322(i_56_, i_54_, i)
    }

    open fun method3046(i: Byte) {
        anInt7038++
        if (i > -102) method3046((-112).toByte())
        if (this.aBoolean7045) {
            this.aClass191_7032!!.method1432(124.toByte())
            this.aClass191_7032 = null
        } else {
            this.aClass322_7033!!.method2558(6144)
            this.aClass322_7033 = null
        }
    }

    open fun method3047(i: Int, i_57_: Int): Array<IntArray>? {
        anInt7040++
        if (i_57_ != -1564599039) method3048(-4, -64, 20)
        throw IllegalStateException("This operation does not have a colour output")
    }

    fun method3048(i: Int, i_58_: Int, i_59_: Int): IntArray? {
        anInt7034++
        if (i_58_ != 633706337) this.aClass191_7032 = null
        if (!this.aClass348_Sub40Array7031[i_59_]!!.aBoolean7045) return (this.aClass348_Sub40Array7031[i_59_]!!.method3047(i, -1564599039)!![0])
        return this.aClass348_Sub40Array7031[i_59_]!!.method3042(i, i_58_ + -633706082)
    }

    open fun method3049(packet: Packet?, i: Int, i_60_: Int) {
        anInt7028++
        if (i_60_ != 31015) method3038(-16)
    }


    init {
        this.aClass348_Sub40Array7031 = arrayOfNulls<Class348_Sub40>(i)
    }

    companion object {
        var anInt7027: Int = 0
        var anInt7028: Int = 0
        var anInt7034: Int = 0
        var anInt7035: Int = 0
        var anInt7037: Int = 0
        var anInt7038: Int = 0
        var anInt7039: Int = 0
        var anInt7040: Int = 0
        var anInt7029: Int = 0
        var anInt7043: Int = 0
        var anInt7044: Int = 0

        // Trimmed for item_renderer_standalone: the original body of method3038
        // pushed flags out to a dozen unrelated subsystems (sound engine, HUD
        // components, minimap, chat, etc.) reached only through the giant
        // Class348_Sub51 client singleton graph -- none of which exist in, or
        // are relevant to, this standalone item-icon renderer. This is also
        // confirmed dead in practice: the only call site
        // (Class348_Sub37.method3031 -> Class348_Sub40.method3049) always
        // passes i_60_ == 31015, so the "if (i_60_ != 31015) method3038(-16);"
        // guard in method3049 below never actually invokes this method.
        fun method3038(i: Int) {
            anInt7044++
        }
    }
}
