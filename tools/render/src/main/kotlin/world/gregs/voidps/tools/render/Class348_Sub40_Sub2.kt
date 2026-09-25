package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub2 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub2 : Class348_Sub40(0, true) {
    private var anInt9095 = 1
    private var anInt9098 = 204
    private var anInt9099 = 1

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9099 = packet!!.readUnsignedByte()
            1 -> anInt9095 = packet!!.readUnsignedByte()
            2 -> anInt9098 = packet!!.readUnsignedShort()
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            for (i_3_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                val i_4_ = Class348_Sub40_Sub8.anIntArray6432!![i_3_]
                val i_5_ = Class79.anIntArray6035!![i]
                var i_6_ = anInt9099 * i_4_ shr 12
                val i_7_ = anInt9095 * i_5_ shr 12
                val i_8_ = i_4_ % (4096 / anInt9099) * anInt9099
                val i_9_ = i_5_ % (4096 / anInt9095) * anInt9095
                if (i_9_ < anInt9098) {
                    i_6_ -= i_7_
                    while (i_6_ < 0) i_6_ += 4
                    while (i_6_ > 3) i_6_ -= 4
                    if (i_6_ != 1) {
                        `is`!![i_3_] = 0
                        continue
                    }
                    if (anInt9098 > i_8_) {
                        `is`!![i_3_] = 0
                        continue
                    }
                }
                if (i_8_ < anInt9098) {
                    i_6_ -= i_7_
                    while (i_6_ < 0) i_6_ += 4
                    while (i_6_ > 3) i_6_ -= 4
                    if (i_6_ > 0) {
                        `is`!![i_3_] = 0
                        continue
                    }
                }
                `is`!![i_3_] = 4096
            }
        }
        return `is`
    }
}
