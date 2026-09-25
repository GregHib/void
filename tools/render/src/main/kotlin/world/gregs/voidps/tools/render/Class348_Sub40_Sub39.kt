package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub39 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*
* Draws vector shapes (lines, curves, rectangles, ellipses) - see Class50 and Class17.
*/

internal class Class348_Sub40_Sub39 : Class348_Sub40(0, true) {
    private var aClass50Array9481: Array<Class50?>? = null

    private fun method3151(`is`: Array<IntArray?>?) {
        val i_0_ = Class348_Sub40_Sub6.anInt9139
        val i_1_ = Class348_Sub40_Sub33.anInt6212
        Class17.method224(`is`)
        Class17.method3000(Class348_Sub40_Sub37.anInt6076, 0, Class348_Sub40_Sub6.anInt6325, 0)
        val shapes = aClass50Array9481
        if (shapes != null) {
            for (i_2_ in shapes.indices) {
                val class50 = shapes[i_2_]!!
                val i_3_ = class50.anInt864
                val i_4_ = class50.anInt865
                if (i_3_ < 0) {
                    if (i_4_ >= 0) class50.method457(i_1_, i_0_)
                } else if (i_4_ < 0) {
                    class50.method456(i_0_, i_1_)
                } else {
                    class50.method455(i_0_, i_1_)
                }
            }
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) method3151(this.aClass191_7032!!.method1427())
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) {
            val shapes = arrayOfNulls<Class50>(packet!!.readUnsignedByte())
            aClass50Array9481 = shapes
            for (i_7_ in shapes.indices) {
                when (packet.readUnsignedByte()) {
                    0 -> shapes[i_7_] = Class50.method1374(packet)
                    1 -> shapes[i_7_] = Class50.method2970(packet)
                    2 -> shapes[i_7_] = Class50.method3036(packet)
                    3 -> shapes[i_7_] = Class50.method2022(packet)
                }
            }
        } else if (i == 1) {
            this.aBoolean7045 = packet!!.readUnsignedByte() == 1
        }
    }

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val i_11_ = Class348_Sub40_Sub6.anInt9139
            val i_12_ = Class348_Sub40_Sub33.anInt6212
            val is_13_: Array<IntArray?> = Array(i_12_) { IntArray(i_11_) }
            val is_14_ = this.aClass322_7033!!.method2553()!!
            method3151(is_13_)
            for (i_15_ in 0..<Class348_Sub40_Sub33.anInt6212) {
                val is_16_ = is_13_[i_15_]!!
                val is_17_ = is_14_[i_15_]
                val is_18_ = is_17_[0]
                val is_19_ = is_17_[1]
                val is_20_ = is_17_[2]
                for (i_21_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                    val i_22_ = is_16_[i_21_]
                    is_20_[i_21_] = (i_22_ and 255) shl 4
                    is_19_[i_21_] = (i_22_ shr 4) and 4080
                    is_18_[i_21_] = 4080 and (i_22_ shr 12)
                }
            }
        }
        return `is`
    }
}
