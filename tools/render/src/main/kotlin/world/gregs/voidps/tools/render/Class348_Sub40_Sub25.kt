package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub25 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub25 : Class348_Sub40(0, true) {
    private var anInt9338 = 0
    private var anInt9339 = 0
    private var anInt9340 = 1365
    private var anInt9343 = 20

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9340 = packet!!.readUnsignedShort()
            1 -> anInt9343 = packet!!.readUnsignedShort()
            2 -> anInt9339 = packet!!.readUnsignedShort()
            3 -> anInt9338 = packet!!.readUnsignedShort()
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            var i_3_ = 0
            while (i_3_ < Class348_Sub40_Sub6.anInt9139) {
                val i_4_ = anInt9339 + (Class348_Sub40_Sub8.anIntArray6432!![i_3_] shl 12) / anInt9340
                val i_5_ = anInt9338 + (Class79.anIntArray6035!![i] shl 12) / anInt9340
                val i_6_ = i_4_
                val i_7_ = i_5_
                var i_8_ = i_4_
                var i_9_ = i_5_
                var i_10_ = i_4_ * i_4_ shr 12
                var i_11_ = i_5_ * i_5_ shr 12
                var i_12_ = 0
                while (i_11_ + i_10_ < 16384 && anInt9343 > i_12_) {
                    i_9_ = i_7_ + 2 * (i_9_ * i_8_ shr 12)
                    i_8_ = i_6_ + i_10_ - i_11_
                    i_12_++
                    i_11_ = i_9_ * i_9_ shr 12
                    i_10_ = i_8_ * i_8_ shr 12
                }
                `is`!![i_3_] = if (i_12_ >= anInt9343 - 1) 0 else (i_12_ shl 12) / anInt9343
                i_3_++
            }
        }
        return `is`
    }
}
