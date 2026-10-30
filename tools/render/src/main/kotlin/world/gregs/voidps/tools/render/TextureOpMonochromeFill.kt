package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub15(i: Int) **/
internal class TextureOpMonochromeFill(i: Int) : TextureOp(0, true) {
    private var anInt9220 = 4096
    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) anInt9220 = (packet!!.readUnsignedByte() shl 12) / 255
    }

    init {
        anInt9220 = i
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aMonochromeImageCache_7032!!.method1433(i)
        if (this.aMonochromeImageCache_7032!!.aBoolean2570) TextureOpIrregularBricks.method1579(`is`!!, 0, TextureOpPolarDistortion.anInt9139, anInt9220)
        return `is`
    }

    constructor() : this(4096)
}
