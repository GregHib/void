package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub17 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal open class Class348_Sub40_Sub17 : Class348_Sub40(0, false) {
    var anIntArray9232: IntArray?
    var anInt9237: Int = 0
    var anInt9241: Int = 0
    private var anInt9243 = -1
    fun method3090(bool: Boolean): Boolean {
        anInt9235++
        if (bool != true) return true
        if (this.anIntArray9232 != null) return true
        if (anInt9243 >= 0) {
            val class207: Class207? = (if (Matrix_Sub2.anInt5713 >= 0) Class207.Companion.method1521(Class348.Companion.aJs5_4286, Matrix_Sub2.anInt5713, anInt9243) else Class207.Companion.method1512(Class348.Companion.aJs5_4286, anInt9243))
            class207!!.method1524()
            this.anIntArray9232 = class207.method1516()
            this.anInt9237 = class207.anInt2702
            this.anInt9241 = class207.anInt2696
            return true
        }
        return false
    }

    override fun method3049(packet: Packet, i: Int, i_0_: Int) {
        if (i_0_ == 31015) {
            if (i == 0) anInt9243 = packet.readUnsignedShort(842397944)
            anInt9236++
        }
    }

    override fun method3037(i: Int): Int {
        anInt9233++
        if (i > -113) aBoolean9242 = false
        return anInt9243
    }

    override fun method3046(i: Byte) {
        super.method3046(-112.toByte())
        if (i < -102) {
            anInt9238++
            this.anIntArray9232 = null
        }
    }

    override fun method3047(i: Int, i_1_: Int): Array<IntArray> {
        anInt9239++
        if (i_1_ != -1564599039) method3047(8, -86)
        val `is` = this.aClass322_7033.method2557(-108, i)
        if (this.aClass322_7033.aBoolean4035 && method3090(true)) {
            val is_2_ = `is`[0]
            val is_3_ = `is`[1]
            val is_4_ = `is`[2]
            var i_5_ = (this.anInt9237 * (if (this.anInt9241 != Class286_Sub2.anInt6212) (this.anInt9241 * i / Class286_Sub2.anInt6212) else i))
            if (Class348_Sub40_Sub6.Companion.anInt9139 == this.anInt9237) {
                var i_6_ = 0
                while ((Class348_Sub40_Sub6.Companion.anInt9139 > i_6_)) {
                    val i_7_ = this.anIntArray9232!![i_5_++]
                    is_4_[i_6_] = Class139.method1166(4080, i_7_ shl 4)
                    is_3_[i_6_] = Class139.method1166(65280, i_7_) shr 4
                    is_2_[i_6_] = Class139.method1166(4080, i_7_ shr 12)
                    i_6_++
                }
            } else {
                var i_8_ = 0
                while ((Class348_Sub40_Sub6.Companion.anInt9139 > i_8_)) {
                    val i_9_: Int = (this.anInt9237 * i_8_ / Class348_Sub40_Sub6.Companion.anInt9139)
                    val i_10_ = (this.anIntArray9232!![i_9_ + i_5_])
                    is_4_[i_8_] = Class139.method1166(i_10_, 255) shl 4
                    is_3_[i_8_] = Class139.method1166(i_10_ shr 4, 4080)
                    is_2_[i_8_] = Class139.method1166(i_10_, 16711680) shr 12
                    i_8_++
                }
            }
        }
        return `is`
    }

    companion object {
        var anInt9233: Int = 0
        var anInt9238: Int = 0
        var anInt9239: Int = 0
        var aBoolean9242: Boolean = false
        var anInt9235: Int = 0
        var anInt9236: Int = 0
    }
}
