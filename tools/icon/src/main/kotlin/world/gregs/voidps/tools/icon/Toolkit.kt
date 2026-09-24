package world.gregs.voidps.tools.icon

import java.awt.Canvas
import java.util.*

/* ha - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal abstract class Toolkit(var textureSource: TextureSource?) {
    var index: Int
    abstract fun method3652()

    abstract fun method3711(`is`: IntArray, i: Int, i_212_: Int, i_213_: Int, i_214_: Int, bool: Boolean): Sprite?

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

    fun method3635(i: Byte) {
        val i_15_ = -90 % ((i - 8) / 33)
        anInt4573++
        Class348_Sub40_Sub26.aBooleanArray9351!![this.index] = false
        method3652()
    }

    fun createSprite(i: Int, `is`: IntArray?, i_84_: Byte, i_85_: Int, i_86_: Int, i_87_: Int): Sprite? {
        anInt4565++
        if (i_84_.toInt() != 94) return null
        return method3711(`is`!!, i_85_, i_86_, i, i_87_, true)
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
        var anInt4563: Int = 0
        var anInt4564: Int = 0
        var anInt4565: Int = 0
        var anInt4573: Int = 0
        var anInt4576: Int = 0
        var anInt4583: Int = 0

        fun method3664(i: Int, i_88_: Int): ByteArray? {
            anInt4564++
            if (i_88_ <= 21) anInt4583 = 60
            var class348_sub42_sub3 = (Class348_Sub1_Sub2.aClass308_8815.method2302(i.toLong(), (-120).toByte()) as Class348_Sub42_Sub3?)
            if (class348_sub42_sub3 == null) {
                val `is` = ByteArray(512)
                val random = Random(i.toLong())
                for (i_89_ in 0..254) `is`[i_89_] = i_89_.toByte()
                for (i_90_ in 0..254) {
                    val i_91_ = -i_90_ + 255
                    val i_92_: Int = Mesh.Companion.method1097(95.toByte(), i_91_, random)
                    val i_93_ = `is`[i_92_]
                    `is`[i_92_] = `is`[i_91_]
                    `is`[511 + -i_90_] = i_93_
                    `is`[i_91_] = `is`[511 + -i_90_]
                }
                class348_sub42_sub3 = Class348_Sub42_Sub3(`is`)
                Class348_Sub1_Sub2.aClass308_8815.method2305(i.toLong(), class348_sub42_sub3, -1)
            }
            return class348_sub42_sub3.aByteArray9499
        }

        @Synchronized
        fun method3692(i: Int, i_168_: Int, i_169_: Int, i_170_: Int, var_textureSource: TextureSource?, canvas: Canvas?, i_171_: Int): Toolkit {
            try {
                anInt4576++
                // Only the i_170_ == i_171_ branch is ever reachable from this
                // renderer (CacheItemSpriteDumper always calls with both 0); the
                // other renderer-selection branches (Class306/Class262/Class93/
                // Class96) are unreachable per JaCoCo coverage and were dropped.
                if (i_170_ == i_171_) return Class348_Sub5.method2753(true, i_168_, i_169_, canvas, var_textureSource)
                throw IllegalArgumentException("UM")
            } catch (runtimeexception: RuntimeException) {
                throw Class348_Sub17.method2929(runtimeexception, ("ha.TJ(" + i + ',' + i_168_ + ',' + i_169_ + ',' + i_170_ + ',' + (if (var_textureSource != null) "{...}" else "null") + ',' + (if (canvas != null) "{...}" else "null") + ',' + i_171_ + ')'))
            }
        }
    }
}
