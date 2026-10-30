package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub3 **/
internal class TextureOpBinary : TextureOp(1, true) {
    private var anInt9104 = 0
    private var anInt9107 = 4096

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9104 = packet!!.readUnsignedShort()
            1 -> anInt9107 = packet!!.readUnsignedShort()
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aMonochromeImageCache_7032!!.method1433(i)
        if (this.aMonochromeImageCache_7032!!.aBoolean2570) {
            val is_4_ = this.method3048(i, 0)!!
            for (i_5_ in 0..<TextureOpPolarDistortion.anInt9139) {
                val i_6_ = is_4_[i_5_]
                `is`!![i_5_] = if (i_6_ >= anInt9104 && i_6_ <= anInt9107) 4096 else 0
            }
        }
        return `is`
    }
}
