package world.gregs.voidps.tools.icon

/* Class262 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class262 {
    var aClass348_3334: Class348 = Class348()
    private var aClass348_3342: Class348? = null

    fun method1993(i: Int): Class348? {
        anInt3336++
        val class348 = this.aClass348_3334.aClass348_4295
        if (this.aClass348_3334 === class348) {
            aClass348_3342 = null
            return null
        }
        aClass348_3342 = class348!!.aClass348_4295
        if (i > -65) method1993(67)
        return class348
    }

    fun method1996(i: Int) {
        if (i > 97) {
            anInt3339++
            while (true) {
                val class348 = (this.aClass348_3334.aClass348_4294)
                if (this.aClass348_3334 === class348) break
                class348!!.unlink(24.toByte())
            }
            aClass348_3342 = null
        }
    }

    fun method1997(i: Int): Class348? {
        anInt3341++
        if (i != 8) aClass190ArrayArray3335 = null
        val class348 = this.aClass348_3334.aClass348_4294
        if (this.aClass348_3334 === class348) return null
        class348!!.unlink(114.toByte())
        return class348
    }

    fun method2001(class348: Class348, i: Int) {
        anInt3330++
        if (class348.aClass348_4295 != null) class348.unlink(63.toByte())
        class348.aClass348_4295 = this.aClass348_3334
        class348.aClass348_4294 = this.aClass348_3334.aClass348_4294
        if (i > -89) aFont_3326 = null
        class348.aClass348_4295!!.aClass348_4294 = class348
        class348.aClass348_4294!!.aClass348_4295 = class348
    }

    init {
        this.aClass348_3334.aClass348_4295 = this.aClass348_3334
        this.aClass348_3334.aClass348_4294 = this.aClass348_3334
    }

    companion object {
        var aFont_3326: Font? = null
        var anInt3330: Int = 0
        var aClass190ArrayArray3335: Array<Array<Class190?>?>? = null
        var anInt3336: Int = 0
        var anInt3338: Int = 0
        var anInt3339: Int = 0
        var anInt3341: Int = 0
        fun cubeMap(f: Float, f_3_: Float, fs: FloatArray?, i: Int, i_4_: Int, bool: Boolean, i_5_: Int, i_6_: Int, i_7_: Int, i_8_: Int, f_9_: Float, fs_10_: FloatArray?, i_11_: Int, i_12_: Int) {
            var i = i
            var i_7_ = i_7_
            var i_8_ = i_8_
            do {
                try {
                    anInt3338++
                    i_7_ -= i_6_
                    i -= i_4_
                    i_8_ -= i_11_
                    val f_13_ = fs!![2] * i.toFloat() + (fs[1] * i_8_.toFloat() + fs[0] * i_7_.toFloat())
                    val f_14_ = (i_7_.toFloat() * fs[3] + i_8_.toFloat() * fs[4] + i.toFloat() * fs[5])
                    val f_15_ = fs[8] * i.toFloat() + (fs[6] * i_7_.toFloat() + i_8_.toFloat() * fs[7])
                    var f_16_: Float
                    var f_17_: Float
                    if (i_12_ == 0) {
                        f_16_ = 0.5f + (f_3_ + f_13_)
                        f_17_ = -f_15_ + f + 0.5f
                    } else if (i_12_ == 1) {
                        f_17_ = 0.5f + (f_15_ + f)
                        f_16_ = 0.5f + (f_3_ + f_13_)
                    } else if (i_12_ == 2) {
                        f_16_ = 0.5f + (-f_13_ + f_3_)
                        f_17_ = -f_14_ + f_9_ + 0.5f
                    } else if (i_12_ == 3) {
                        f_17_ = -f_14_ + f_9_ + 0.5f
                        f_16_ = f_13_ + f_3_ + 0.5f
                    } else if (i_12_ == 4) {
                        f_16_ = f_15_ + f + 0.5f
                        f_17_ = -f_14_ + f_9_ + 0.5f
                    } else {
                        f_16_ = 0.5f + (f + -f_15_)
                        f_17_ = -f_14_ + f_9_ + 0.5f
                    }
                    if (i_5_ == 1) {
                        val f_18_ = f_16_
                        f_16_ = -f_17_
                        f_17_ = f_18_
                    } else if (i_5_ == 2) {
                        f_17_ = -f_17_
                        f_16_ = -f_16_
                    } else if (i_5_ == 3) {
                        val f_19_ = f_16_
                        f_16_ = f_17_
                        f_17_ = -f_19_
                    }
                    fs_10_!![1] = f_17_
                    fs_10_[0] = f_16_
                    if (bool == false) break
                    cubeMap(0.31271333f, 1.5829445f, null, -17, 88, true, -70, -107, 8, 5, -0.347415f, null, -24, -19)
                } catch (runtimeexception: RuntimeException) {
                    throw Class348_Sub17.method2929(runtimeexception, ("uh.B(" + f + ',' + f_3_ + ',' + (if (fs != null) "{...}" else "null") + ',' + i + ',' + i_4_ + ',' + bool + ',' + i_5_ + ',' + i_6_ + ',' + i_7_ + ',' + i_8_ + ',' + f_9_ + ',' + (if (fs_10_ != null) "{...}" else "null") + ',' + i_11_ + ',' + i_12_ + ')'))
                }
                break
            } while (false)
        }
    }
}
