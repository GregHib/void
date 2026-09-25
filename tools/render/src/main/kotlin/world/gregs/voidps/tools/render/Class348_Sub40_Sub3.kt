package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub3 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub3 : Class348_Sub40(1, true) {
    private var anInt9104 = 0
    private var anInt9107 = 4096

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9104 = packet!!.readUnsignedShort()
            1 -> anInt9107 = packet!!.readUnsignedShort()
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_4_ = this.method3048(i, 0)!!
            for (i_5_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                val i_6_ = is_4_[i_5_]
                `is`!![i_5_] = if (i_6_ >= anInt9104 && i_6_ <= anInt9107) 4096 else 0
            }
        }
        return `is`
    }
}
