package world.gregs.voidps.tools.render

import java.awt.Canvas
import java.util.*

/* ha - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

abstract class Toolkit(var textureSource: TextureSource?) {
    var index: Int
    abstract fun method3652()

    abstract fun method3711(`is`: IntArray, i: Int, i_212_: Int, i_213_: Int, i_214_: Int): Sprite?

    // abstract dependencies of Class213.method1562 (genuine), which calls
    // these on a `ha`-typed reference - all implemented concretely in ha_Sub1.
    abstract fun createModel(mesh: Mesh, functionMask: Int, featureMask: Int, ambient: Int, contrast: Int): Model?

    abstract fun method3654(): Matrix?

    abstract fun setCamera(matrix: Matrix?)

    abstract fun xa(f: Float)

    abstract fun ZA(i: Int, f: Float, f_573_: Float, f_574_: Float, f_575_: Float, f_576_: Float)

    abstract fun method3705(): Matrix?

    abstract fun i(): Int

    abstract fun XA(): Int

    abstract fun f(i: Int, i_156_: Int)

    abstract fun ya()

    abstract fun la()

    abstract fun aa(i: Int, i_334_: Int, i_335_: Int, i_336_: Int, i_337_: Int, i_338_: Int)

    abstract fun na(i: Int, i_0_: Int, i_1_: Int, i_2_: Int): IntArray?

    abstract fun DA(i: Int, i_223_: Int, i_224_: Int, i_225_: Int)

    fun method3635() {
        Class348_Sub40_Sub26.aBooleanArray9351!![this.index] = false
        method3652()
    }

    fun createSprite(i: Int, `is`: IntArray?, i_86_: Int, i_87_: Int): Sprite? {
        return method3711(`is`!!, 0, i_86_, i, i_87_)
    }

    init {
        var i = -1
        for (i_215_ in 0..7) {
            if (!Class348_Sub40_Sub26.aBooleanArray9351!![i_215_]) {
                Class348_Sub40_Sub26.aBooleanArray9351!![i_215_] = true
                i = i_215_
                break
            }
        }
        check(i != -1) { "NFTI" }
        this.index = i
    }

    companion object {
        var aClass308_8815: Class308 = Class308(16)
        fun method3664(i: Int): ByteArray? {
            var class348_sub42_sub3 = (aClass308_8815.method2302(i.toLong()) as Class348_Sub42_Sub3?)
            if (class348_sub42_sub3 == null) {
                val `is` = ByteArray(512)
                val random = Random(i.toLong())
                for (i_89_ in 0..254) `is`[i_89_] = i_89_.toByte()
                for (i_90_ in 0..254) {
                    val i_91_ = -i_90_ + 255
                    val i_92_: Int = Mesh.method1097(i_91_, random)
                    val i_93_ = `is`[i_92_]
                    `is`[i_92_] = `is`[i_91_]
                    `is`[511 + -i_90_] = i_93_
                    `is`[i_91_] = `is`[511 + -i_90_]
                }
                class348_sub42_sub3 = Class348_Sub42_Sub3(`is`)
                aClass308_8815.method2305(i.toLong(), class348_sub42_sub3)
            }
            return class348_sub42_sub3.aByteArray9499
        }

        @Synchronized
        fun method3692(i_168_: Int, i_169_: Int, var_textureSource: TextureSource?, canvas: Canvas?): Toolkit {
            return JavaToolkit(canvas, var_textureSource, i_169_, i_168_)
        }
    }
}
