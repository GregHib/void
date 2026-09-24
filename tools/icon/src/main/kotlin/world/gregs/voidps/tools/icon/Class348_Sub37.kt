package world.gregs.voidps.tools.icon

/* Class348_Sub37 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class348_Sub37 {
    var anInt6996: Int = 0

    fun method3031(i: Int, packet: Packet): Class348_Sub40? {
        anInt6996++
        packet.readUnsignedByte(255)
        val i_0_ = packet.readUnsignedByte(255)
        val class348_sub40 = Class59_Sub1_Sub1.method557(i_0_, -84.toByte())
        class348_sub40!!.anInt7036 = packet.readUnsignedByte(255)
        val i_1_ = packet.readUnsignedByte(255)
        if (i < 123) return null
        var i_2_ = 0
        while (i_1_ > i_2_) {
            val i_3_ = packet.readUnsignedByte(255)
            class348_sub40.method3049(packet, i_3_, 31015)
            i_2_++
        }
        class348_sub40.method3044(120)
        return class348_sub40
    }
}
