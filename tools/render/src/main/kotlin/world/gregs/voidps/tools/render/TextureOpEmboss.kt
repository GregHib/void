package world.gregs.voidps.tools.render

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Class348_Sub40_Sub35 **/
internal class TextureOpEmboss : TextureOp(1, true) {
    private var anInt9445 = 3216
    private var anInt9447 = 3216
    private var anInt9448 = 4096
    private val anIntArray9449 = IntArray(3)

    private fun method3143() {
        val d = cos((anInt9445.toFloat() / 4096.0f).toDouble())
        anIntArray9449[0] = (d * sin((anInt9447.toFloat() / 4096.0f).toDouble()) * 4096.0).toInt()
        anIntArray9449[1] = (d * cos((anInt9447.toFloat() / 4096.0f).toDouble()) * 4096.0).toInt()
        anIntArray9449[2] = (sin((anInt9445.toFloat() / 4096.0f).toDouble()) * 4096.0).toInt()
        val i_0_ = anIntArray9449[0] * anIntArray9449[0] shr 12
        val i_1_ = anIntArray9449[1] * anIntArray9449[1] shr 12
        val i_2_ = anIntArray9449[2] * anIntArray9449[2] shr 12
        val i_3_ = (4096.0 * sqrt((i_1_ + i_0_ + i_2_ shr 12).toDouble())).toInt()
        if (i_3_ != 0) {
            anIntArray9449[1] = (anIntArray9449[1] shl 12) / i_3_
            anIntArray9449[0] = (anIntArray9449[0] shl 12) / i_3_
            anIntArray9449[2] = (anIntArray9449[2] shl 12) / i_3_
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aMonochromeImageCache_7032!!.method1433(i)
        if (this.aMonochromeImageCache_7032!!.aBoolean2570) {
            val i_5_ = TextureOpKaleidoscope.anInt3201 * anInt9448 shr 12
            val is_6_ = this.method3048(TextureOpPolarDistortion.anInt6325 and -1 + i, 0)
            val is_7_ = this.method3048(i, 0)
            val is_8_ = this.method3048(TextureOpPolarDistortion.anInt6325 and i - -1, 0)
            var i_9_ = 0
            while (TextureOpPolarDistortion.anInt9139 > i_9_) {
                val i_10_ = i_5_ * (is_8_!![i_9_] - is_6_!![i_9_]) shr 12
                val i_11_ = (i_5_ * (-is_7_!![TextureOpBoxBlur.anInt6076 and i_9_ - -1] + is_7_[-1 + i_9_ and TextureOpBoxBlur.anInt6076]) shr 12)
                var i_12_ = i_11_ shr 4
                var i_13_ = i_10_ shr 4
                if (i_12_ < 0) i_12_ = -i_12_
                if (i_13_ < 0) i_13_ = -i_13_
                if (i_12_ > 255) i_12_ = 255
                if (i_13_ > 255) i_13_ = 255
                val i_14_ = (aByteArray821!![i_12_ + (i_13_ * (1 + i_13_) shr 1)].toInt() and 0xff)
                var i_15_ = i_14_ * 4096 shr 8
                var i_16_ = i_14_ * i_11_ shr 8
                var i_17_ = i_10_ * i_14_ shr 8
                i_17_ = i_17_ * anIntArray9449[1] shr 12
                i_16_ = i_16_ * anIntArray9449[0] shr 12
                i_15_ = anIntArray9449[2] * i_15_ shr 12
                `is`!![i_9_] = i_15_ + i_16_ - -i_17_
                i_9_++
            }
        }
        return `is`
    }

    override fun method3044() {
        method3143()
    }

    override fun method3049(packet: Packet?, i: Int) {
        val i_19_ = i
        while_210_@ do {
            do {
                if (i_19_ == 0) {
                    anInt9448 = packet!!.readUnsignedShort()
                    break@while_210_
                } else if (i_19_ != 1) {
                    if (i_19_ == 2) break
                    break@while_210_
                }
                anInt9447 = packet!!.readUnsignedShort()
                break@while_210_
            } while (false)
            anInt9445 = packet!!.readUnsignedShort()
        } while (false)
    }

    companion object {
        /** Class46.aByteArray821 */
        var aByteArray821: ByteArray? = ByteArray(32896)

        /** Class46 init */
        init {
            var i = 0
            for (i_69_ in 0..255) {
                var i_70_ = 0
                while (i_69_ >= i_70_) {
                    aByteArray821!![i++] = (255.0 / sqrt((((i_69_ * i_69_) + (i_70_ * i_70_) - -65535).toFloat() / 65535.0f).toDouble())).toInt().toByte()
                    i_70_++
                }
            }
        }
    }
}
