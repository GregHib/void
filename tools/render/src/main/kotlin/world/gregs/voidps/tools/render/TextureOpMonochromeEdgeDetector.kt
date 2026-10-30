package world.gregs.voidps.tools.render

/** Class348_Sub40_Sub9 **/
internal class TextureOpMonochromeEdgeDetector : TextureOp(1, true) {
    private var anInt9167 = 4096

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) anInt9167 = packet!!.readUnsignedShort()
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aMonochromeImageCache_7032!!.method1433(i)
        if (this.aMonochromeImageCache_7032!!.aBoolean2570) {
            val is_6_ = this.method3048(TextureOpPolarDistortion.anInt6325 and -1 + i, 0)!!
            val is_7_ = this.method3048(i, 0)!!
            val is_8_ = this.method3048(i + 1 and TextureOpPolarDistortion.anInt6325, 0)!!
            for (i_9_ in 0..<TextureOpPolarDistortion.anInt9139) {
                val i_10_ = anInt9167 * (-is_6_[i_9_] + is_8_[i_9_])
                val i_11_ = anInt9167 * (-is_7_[TextureOpBoxBlur.anInt6076 and -1 + i_9_] + is_7_[TextureOpBoxBlur.anInt6076 and i_9_ + 1])
                val i_12_ = i_11_ shr 12
                val i_13_ = i_10_ shr 12
                val i_14_ = i_12_ * i_12_ shr 12
                val i_15_ = i_13_ * i_13_ shr 12
                val i_16_ = (4096.0 * Math.sqrt(((i_15_ + (i_14_ + 4096)).toFloat() / 4096.0f).toDouble())).toInt()
                val i_17_ = if (i_16_ != 0) 16777216 / i_16_ else 0
                `is`!![i_9_] = 4096 - i_17_
            }
        }
        return `is`
    }
}
