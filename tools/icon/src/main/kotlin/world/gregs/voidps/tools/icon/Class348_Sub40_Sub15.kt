package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub15 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub15(i: Int) : Class348_Sub40(0, true) {
    private var anInt9220 = 4096
    override fun method3049(packet: Packet, i: Int, i_0_: Int) {
        val i_1_ = i
        if (i_1_ == 0) anInt9220 = (packet.readUnsignedByte(255) shl 12) / 255
        if (i_0_ == 31015) anInt9217++
    }

    init {
        anInt9220 = i
    }

    override fun method3042(i: Int, i_8_: Int): IntArray? {
        anInt9221++
        val `is` = this.aClass191_7032.method1433(0, i)
        if (this.aClass191_7032.aBoolean2570) Class214.method1579(`is`, 0, Class348_Sub40_Sub6.Companion.anInt9139, anInt9220)
        if (i_8_ != 255) method3085(63)
        return `is`
    }

    constructor() : this(4096)

    companion object {
        var aClass114_9216: Class114? = Class114(91, 2)
        var anInt9217: Int = 0
        var anInt9221: Int = 0

        fun method3085(i: Int) {
            aClass114_9216 = null
            if (i != 0) aClass114_9216 = null
        }
    }
}
