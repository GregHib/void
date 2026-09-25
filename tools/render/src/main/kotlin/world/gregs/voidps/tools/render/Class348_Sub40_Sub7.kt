package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub7 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub7 : Class348_Sub40(1, false) {
    private var aBoolean9140 = true
    private var aBoolean9147 = true

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_1_ = this.method3039(if (aBoolean9147) -i + Class348_Sub40_Sub6.anInt6325 else i, 0)!!
            val is_2_ = is_1_[0]
            val is_3_ = is_1_[1]
            val is_4_ = is_1_[2]
            val is_5_ = `is`!![0]
            val is_6_ = `is`[1]
            val is_7_ = `is`[2]
            if (aBoolean9140) {
                for (i_8_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                    is_5_[i_8_] = is_2_[Class348_Sub40_Sub37.anInt6076 - i_8_]
                    is_6_[i_8_] = is_3_[Class348_Sub40_Sub37.anInt6076 - i_8_]
                    is_7_[i_8_] = is_4_[Class348_Sub40_Sub37.anInt6076 - i_8_]
                }
            } else {
                for (i_9_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                    is_5_[i_9_] = is_2_[i_9_]
                    is_6_[i_9_] = is_3_[i_9_]
                    is_7_[i_9_] = is_4_[i_9_]
                }
            }
        }
        return `is`
    }

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> aBoolean9140 = packet!!.readUnsignedByte() == 1
            1 -> aBoolean9147 = packet!!.readUnsignedByte() == 1
            2 -> this.aBoolean7045 = packet!!.readUnsignedByte() == 1
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) {
            val is_76_ = this.method3048(if (aBoolean9147) Class348_Sub40_Sub6.anInt6325 - i else i, 0)!!
            if (aBoolean9140) {
                for (i_77_ in 0..<Class348_Sub40_Sub6.anInt9139) `is`!![i_77_] = is_76_[Class348_Sub40_Sub37.anInt6076 - i_77_]
            } else {
                // Class214.method1578 in the client (an arraycopy)
                System.arraycopy(is_76_, 0, `is`!!, 0, Class348_Sub40_Sub6.anInt9139)
            }
        }
        return `is`
    }
}
