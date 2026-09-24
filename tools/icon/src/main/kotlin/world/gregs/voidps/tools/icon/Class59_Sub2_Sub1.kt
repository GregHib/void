package world.gregs.voidps.tools.icon

/* Class59_Sub2_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class59_Sub2_Sub1 {
    var aJs5_8670: Js5? = null
    var anInt8671: Int = 0
    var anInt8673: Int = 0

    fun fromMesh(i: Int, i_0_: Int, mesh: Mesh?, `is`: IntArray?): TextureUniverse {
        try {
            anInt8673++
            var is_1_: IntArray? = null
            if (i != 255) aJs5_8670 = null
            var is_2_: IntArray? = null
            var is_3_: IntArray? = null
            var fs: Array<FloatArray?>? = null
            if (mesh!!.faceTexSpace != null) {
                val i_4_ = mesh.texSpaceCount
                val is_5_ = IntArray(i_4_)
                val is_6_ = IntArray(i_4_)
                val is_7_ = IntArray(i_4_)
                val is_8_ = IntArray(i_4_)
                val is_9_ = IntArray(i_4_)
                val is_10_ = IntArray(i_4_)
                for (i_11_ in 0..<i_4_) {
                    is_5_[i_11_] = 2147483647
                    is_6_[i_11_] = -2147483647
                    is_7_[i_11_] = 2147483647
                    is_8_[i_11_] = -2147483647
                    is_9_[i_11_] = 2147483647
                    is_10_[i_11_] = -2147483647
                }
                fs = arrayOfNulls<FloatArray>(i_4_)
                is_3_ = IntArray(i_4_)
                is_2_ = IntArray(i_4_)
                for (i_12_ in 0..<i_0_) {
                    val i_13_ = `is`!![i_12_]
                    if (mesh.faceTexSpace!![i_13_].toInt() != -1) {
                        val i_14_ = (mesh.faceTexSpace!![i_13_].toInt() and 0xff)
                        for (i_15_ in 0..2) {
                            val i_16_: Short
                            if (i_15_ != 0) {
                                if (i_15_ == 1) i_16_ = (mesh.faceB!![i_13_])
                                else i_16_ = (mesh.faceC!![i_13_])
                            } else i_16_ = (mesh.faceA!![i_13_])
                            val i_17_ = mesh.vertexX!![i_16_.toInt()]
                            val i_18_ = mesh.vertexY!![i_16_.toInt()]
                            val i_19_ = mesh.vertexZ!![i_16_.toInt()]
                            if (i_17_ < is_5_[i_14_]) is_5_[i_14_] = i_17_
                            if (is_6_[i_14_] < i_17_) is_6_[i_14_] = i_17_
                            if (is_7_[i_14_] > i_18_) is_7_[i_14_] = i_18_
                            if (i_18_ > is_8_[i_14_]) is_8_[i_14_] = i_18_
                            if (i_19_ < is_9_[i_14_]) is_9_[i_14_] = i_19_
                            if (i_19_ > is_10_[i_14_]) is_10_[i_14_] = i_19_
                        }
                    }
                }
                is_1_ = IntArray(i_4_)
                var i_20_ = 0
                while (i_4_ > i_20_) {
                    val i_21_ = mesh.texMappingType!![i_20_]
                    if (i_21_ > 0) {
                        is_1_[i_20_] = (is_6_[i_20_] + is_5_[i_20_]) / 2
                        is_2_[i_20_] = (is_8_[i_20_] + is_7_[i_20_]) / 2
                        is_3_[i_20_] = (is_9_[i_20_] + is_10_[i_20_]) / 2
                        val f: Float
                        val f_22_: Float
                        val f_23_: Float
                        if (i_21_.toInt() == 1) {
                            val i_24_ = mesh.texSpaceScaleX!![i_20_]
                            if (i_24_ == 0) {
                                f_22_ = 1.0f
                                f_23_ = 1.0f
                            } else if (i_24_ <= 0) {
                                f_22_ = 1.0f
                                f_23_ = -i_24_.toFloat() / 1024.0f
                            } else {
                                f_23_ = 1.0f
                                f_22_ = i_24_.toFloat() / 1024.0f
                            }
                            f = 64.0f / (mesh.texSpaceScaleY!![i_20_]).toFloat()
                        } else if (i_21_.toInt() == 2) {
                            f = 64.0f / (mesh.texSpaceScaleY!![i_20_]).toFloat()
                            f_22_ = 64.0f / (mesh.texSpaceScaleZ!![i_20_]).toFloat()
                            f_23_ = 64.0f / (mesh.texSpaceScaleX!![i_20_]).toFloat()
                        } else {
                            f = (mesh.texSpaceScaleY!![i_20_]).toFloat() / 1024.0f
                            f_22_ = (mesh.texSpaceScaleZ!![i_20_]).toFloat() / 1024.0f
                            f_23_ = (mesh.texSpaceScaleX!![i_20_]).toFloat() / 1024.0f
                        }
                        fs[i_20_] = (KeyedReferenceCache.Companion.method1347(mesh.texSpaceDefC!![i_20_].toInt(), mesh.texSpaceDefB!![i_20_].toInt(), f_22_, f_23_, 126, f, mesh.texSpaceDefA!![i_20_].toInt(), Class139.method1166(255, (mesh.texRotation!![i_20_]).toInt())))
                    }
                    i_20_++
                }
            }
            return TextureUniverse(is_1_, is_2_, is_3_, fs)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("dha.B(" + i + ',' + i_0_ + ',' + (if (mesh != null) "{...}" else "null") + ',' + (if (`is` != null) "{...}" else "null") + ')'))
        }
    }

    fun method566(bool: Boolean, bool_25_: Boolean, i: Byte) {
        anInt8671++
        val i_26_ = -94 / ((-67 - i) / 59)
        if (bool) {
            Class348_Sub40_Sub26.anInt9346++
            Class239_Sub25.method1827(1415665776)
        }
        if (bool_25_) {
            Class26.anInt383++
            Class348_Sub6.Companion.method2770(2)
        }
    }
}
