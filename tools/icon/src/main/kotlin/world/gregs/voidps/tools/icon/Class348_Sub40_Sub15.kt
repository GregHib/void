package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub15 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub15(i: Int) : Class348_Sub40(0, true) {
    private var anInt9220 = 4096
    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) anInt9220 = (packet!!.readUnsignedByte() shl 12) / 255
    }

    init {
        anInt9220 = i
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)
        if (this.aClass191_7032!!.aBoolean2570) Class214.method1579(`is`!!, 0, Class348_Sub40_Sub6.anInt9139, anInt9220)
        return `is`
    }

    constructor() : this(4096)
}
