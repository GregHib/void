package world.gregs.voidps.tools.render

import kotlin.math.sqrt

/* Class64_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class JavaModel : Model {
    private var faceColour: ShortArray? = null
    private var vertexZ: IntArray? = null
    private var aClass360Array5313: Array<Class360?>? = null
    private var texCoordU: Array<FloatArray?>? = null
    private var anIntArray5315: IntArray? = null
    private var functionMask = 0
    private var faceA: ShortArray? = null
    private var aClass101_Sub1_5320: Matrix_Sub1? = null
    private var anIntArray5321: IntArray? = null
    private var emitters: Array<ModelParticleEmitter?>? = null
    private var aBoolean5323 = false
    private var aShort5324: Short = 0
    private var faceAlpha: ByteArray? = null
    private var faceIndices: ShortArray? = null
    private var aShort5329: Short = 0
    private var faceLabels: Array<IntArray?>? = null
    private var aShort5331: Short = 0
    private var vertexY: IntArray? = null
    private var vertexLabels: Array<IntArray?>? = null
    private var effectors: Array<ModelParticleEffector?>? = null
    private var anIntArray5337: IntArray? = null
    private var vertexCount = 0
    private var anIntArray5343: IntArray? = null
    private var ambient = 0
    private var texCoordV: Array<FloatArray?>? = null
    private var aShort5348: Short = 0
    private var contrast = 0
    private var faceCount: Int
    private var aShort5352: Short = 0
    private val toolkit: JavaToolkit
    private var anInt5354: Int
    private var anIntArray5355: IntArray? = null
    private var vertexX: IntArray? = null
    private var aBoolean5357 = false
    private var facePriority: ByteArray? = null
    private var aClass360Array5360: Array<Class360?>? = null
    private var billboardFaces: Array<JavaBillboardFace?>? = null
    private var anIntArray5362: IntArray? = null
    private var billboardAttributes: Array<JavaBillboardAttributes?>? = null
    private var faceC: ShortArray? = null
    private var aShort5365: Short = 0
    private var anIntArray5366: IntArray? = null
    private var aJavaThreadResource_5367: JavaThreadResource? = null
    private var anIntArray5368: IntArray? = null
    private var anIntArray5371: IntArray? = null
    private var anIntArray5377: IntArray? = null
    private var billboardLabels: Array<IntArray?>? = null
    private var anIntArray5381: IntArray? = null
    private var transparent: Boolean
    private var rasterizer: Rasterizer? = null
    private var anIntArray5384: IntArray? = null
    private var aClass41Array5385: Array<Class41?>? = null
    private var shadingType: ByteArray? = null
    private var maxVertex: Int
    private var faceTextures: ShortArray? = null
    private var billboardCount = 0
    private var movingTextures: Boolean
    private var anIntArray5392: IntArray? = null
    private var aShort5393: Short = 0
    private var faceB: ShortArray? = null
    private var aShort5395: Short = 0
    private var anIntArray5399: IntArray? = null
    private var anIntArray5400: IntArray? = null

    // Animation state: Class64_Sub1.anInt5338/anInt5375/anInt5342, aBoolean5372 and aBoolean5380
    private var originX = 0
    private var originY = 0
    private var originZ = 0
    private var upscaled = false
    private var coloursChanged = false

    private fun method629(i: Int): Boolean {
        if (faceAlpha == null) return false
        return faceAlpha!![i].toInt() != 0
    }

    private fun method632(thread: Thread?) {
        val javaThreadResource = toolkit.threadResource(thread)
        rasterizer = javaThreadResource!!.rasterizer
        if (javaThreadResource != aJavaThreadResource_5367) {
            aJavaThreadResource_5367 = javaThreadResource
            anIntArray5362 = aJavaThreadResource_5367!!.anIntArray2222
            anIntArray5399 = aJavaThreadResource_5367!!.anIntArray2244
            anIntArray5384 = aJavaThreadResource_5367!!.anIntArray2214
            anIntArray5392 = aJavaThreadResource_5367!!.anIntArray2237
            anIntArray5321 = aJavaThreadResource_5367!!.anIntArray2234
            anIntArray5343 = aJavaThreadResource_5367!!.anIntArray2230
            anIntArray5355 = aJavaThreadResource_5367!!.anIntArray2213
            anIntArray5315 = aJavaThreadResource_5367!!.anIntArray2238
            anIntArray5371 = aJavaThreadResource_5367!!.anIntArray2247
            anIntArray5381 = aJavaThreadResource_5367!!.anIntArray2235
            anIntArray5377 = aJavaThreadResource_5367!!.anIntArray2240
            anIntArray5400 = aJavaThreadResource_5367!!.anIntArray2242
        }
    }

    private fun method634(bool: Boolean) {
        if (toolkit.anInt7485 > 1) {
            synchronized(this) {
                method657(bool)
            }
        } else method657(bool)
    }

    private fun method635(i: Int) {
        val i_39_ = faceA!![i]
        val i_40_ = faceB!![i]
        val i_41_ = faceC!![i]
        if (faceTextures == null || faceTextures!![i].toInt() == -1) {
            if (faceAlpha == null) rasterizer!!.anInt1674 = 0
            else rasterizer!!.anInt1674 = faceAlpha!![i].toInt() and 0xff
            if (anIntArray5366!![i] == -1) rasterizer!!.method1018(
                anIntArray5343!![i_39_.toInt()].toFloat(),
                anIntArray5343!![i_40_.toInt()].toFloat(),
                anIntArray5343!![i_41_.toInt()].toFloat(),
                anIntArray5321!![i_39_.toInt()].toFloat(),
                anIntArray5321!![i_40_.toInt()].toFloat(),
                anIntArray5321!![i_41_.toInt()].toFloat(),
                anIntArray5355!![i_39_.toInt()].toFloat(),
                anIntArray5355!![i_40_.toInt()].toFloat(),
                anIntArray5355!![i_41_.toInt()].toFloat(),
                (ItemSpriteCacheKey.HSV_TO_RGB!![anIntArray5368!![i] and 0xffff])
            )
            else rasterizer!!.method1022(
                anIntArray5343!![i_39_.toInt()].toFloat(),
                anIntArray5343!![i_40_.toInt()].toFloat(),
                anIntArray5343!![i_41_.toInt()].toFloat(),
                anIntArray5321!![i_39_.toInt()].toFloat(),
                anIntArray5321!![i_40_.toInt()].toFloat(),
                anIntArray5321!![i_41_.toInt()].toFloat(),
                anIntArray5355!![i_39_.toInt()].toFloat(),
                anIntArray5355!![i_40_.toInt()].toFloat(),
                anIntArray5355!![i_41_.toInt()].toFloat(),
                (anIntArray5368!![i] and 0xffff).toFloat(),
                (anIntArray5337!![i] and 0xffff).toFloat(),
                (anIntArray5366!![i] and 0xffff).toFloat()
            )
        } else {
            var i_42_ = -16777216
            if (faceAlpha != null) i_42_ = 255 - (faceAlpha!![i].toInt() and 0xff) shl 24
            if (anIntArray5366!![i] == -1) {
                val i_43_ = i_42_ or (anIntArray5368!![i] and 0xffffff)
                rasterizer!!.method1024(
                    anIntArray5343!![i_39_.toInt()].toFloat(),
                    anIntArray5343!![i_40_.toInt()].toFloat(),
                    anIntArray5343!![i_41_.toInt()].toFloat(),
                    anIntArray5321!![i_39_.toInt()].toFloat(),
                    anIntArray5321!![i_40_.toInt()].toFloat(),
                    anIntArray5321!![i_41_.toInt()].toFloat(),
                    anIntArray5355!![i_39_.toInt()].toFloat(),
                    anIntArray5355!![i_40_.toInt()].toFloat(),
                    anIntArray5355!![i_41_.toInt()].toFloat(),
                    texCoordU!![i]!![0],
                    texCoordU!![i]!![1],
                    texCoordU!![i]!![2],
                    texCoordV!![i]!![0],
                    texCoordV!![i]!![1],
                    texCoordV!![i]!![2],
                    i_43_,
                    i_43_,
                    i_43_,
                    aJavaThreadResource_5367!!.anInt2192,
                    0,
                    0,
                    0,
                    faceTextures!![i].toInt()
                )
            } else rasterizer!!.method1024(
                anIntArray5343!![i_39_.toInt()].toFloat(),
                anIntArray5343!![i_40_.toInt()].toFloat(),
                anIntArray5343!![i_41_.toInt()].toFloat(),
                anIntArray5321!![i_39_.toInt()].toFloat(),
                anIntArray5321!![i_40_.toInt()].toFloat(),
                anIntArray5321!![i_41_.toInt()].toFloat(),
                anIntArray5355!![i_39_.toInt()].toFloat(),
                anIntArray5355!![i_40_.toInt()].toFloat(),
                anIntArray5355!![i_41_.toInt()].toFloat(),
                texCoordU!![i]!![0],
                texCoordU!![i]!![1],
                texCoordU!![i]!![2],
                texCoordV!![i]!![0],
                texCoordV!![i]!![1],
                texCoordV!![i]!![2],
                i_42_ or (anIntArray5368!![i] and 0xffffff),
                i_42_ or (anIntArray5337!![i] and 0xffffff),
                i_42_ or (anIntArray5366!![i] and 0xffffff),
                (aJavaThreadResource_5367!!.anInt2192),
                0,
                0,
                0,
                faceTextures!![i].toInt()
            )
        }
    }

    override fun render(matrix: Matrix?, i: Int) {
        method654(matrix, i)
    }

    private fun method636() {
        if (anInt5354 == 0 && aClass360Array5360 == null) {
            if (toolkit.anInt7485 > 1) {
                synchronized(this) {
                    method649()
                }
            } else method649()
        }
    }

    override fun fa(): Int {
        if (!aBoolean5323) method655()
        return aShort5329.toInt()
    }

    private fun method637(i: Int, i_283_: Int): Int {
        var i_283_ = i_283_
        i_283_ = i_283_ * (i and 0x7f) shr 7
        if (i_283_ < 2) i_283_ = 2
        else if (i_283_ > 126) i_283_ = 126
        return (i and 0xff80) + i_283_
    }

    private fun method638(i: Int): Boolean {
        if (anIntArray5400 == null) return false
        return anIntArray5400!![i] != -1
    }

    override fun loadedTextures(): Boolean {
        if (faceTextures == null) return true
        for (i in faceTextures!!.indices) {
            if (faceTextures!![i].toInt() != -1 && !toolkit.method3725(faceTextures!![i].toInt())) return false
        }
        return true
    }

    private fun method642(i: Int, i_305_: Short, i_306_: Int): Int {
        var i_307_ = Class348_Sub6.anIntArray179!![method637(i, i_306_)]
        val textureMetrics = toolkit.textureSource!!.getMetrics(i_305_.toInt() and 0xffff)
        val i_308_ = textureMetrics!!.alpha.toInt() and 0xff
        if (i_308_ != 0) {
            val i_309_ = 131586 * i_306_
            if (i_308_ == 256) i_307_ = i_309_
            else {
                val i_310_ = i_308_
                val i_311_ = 256 - i_308_
                i_307_ = ((((i_309_ and 0xff00ff) * i_310_ + (i_307_ and 0xff00ff) * i_311_) and 0xff00ff.inv()) + (((i_309_ and 0xff00) * i_310_ + (i_307_ and 0xff00) * i_311_) and 0xff0000)) shr 8
            }
        }
        var i_312_ = textureMetrics.aByte216.toInt() and 0xff
        if (i_312_ != 0) {
            i_312_ += 256
            var i_313_ = ((i_307_ and 0xff0000) shr 16) * i_312_
            if (i_313_ > 65535) i_313_ = 65535
            var i_314_ = ((i_307_ and 0xff00) shr 8) * i_312_
            if (i_314_ > 65535) i_314_ = 65535
            var i_315_ = (i_307_ and 0xff) * i_312_
            if (i_315_ > 65535) i_315_ = 65535
            i_307_ = (i_313_ shl 8 and 0xff0000) + (i_314_ and 0xff00) + (i_315_ shr 8)
        }
        return i_307_
    }

    private fun method643(i: Int, bool: Boolean, bool_317_: Boolean) {
        if (anIntArray5366!![i] != -2) {
            val i_318_ = faceA!![i]
            val i_319_ = faceB!![i]
            val i_320_ = faceC!![i]
            val i_321_ = anIntArray5321!![i_318_.toInt()]
            val i_322_ = anIntArray5321!![i_319_.toInt()]
            val i_323_ = anIntArray5321!![i_320_.toInt()]
            if (bool && (i_321_ == -5000 || i_322_ == -5000 || i_323_ == -5000)) {
                var i_324_ = anIntArray5399!![i_318_.toInt()]
                val i_325_ = anIntArray5399!![i_319_.toInt()]
                var i_326_ = anIntArray5399!![i_320_.toInt()]
                var i_327_ = anIntArray5384!![i_318_.toInt()]
                val i_328_ = anIntArray5384!![i_319_.toInt()]
                var i_329_ = anIntArray5384!![i_320_.toInt()]
                var i_330_ = anIntArray5392!![i_318_.toInt()]
                val i_331_ = anIntArray5392!![i_319_.toInt()]
                var i_332_ = anIntArray5392!![i_320_.toInt()]
                i_324_ -= i_325_
                i_326_ -= i_325_
                i_327_ -= i_328_
                i_329_ -= i_328_
                i_330_ -= i_331_
                i_332_ -= i_331_
                val i_333_ = i_327_ * i_332_ - i_330_ * i_329_
                val i_334_ = i_330_ * i_326_ - i_324_ * i_332_
                val i_335_ = i_324_ * i_329_ - i_327_ * i_326_
                if (i_325_ * i_333_ + i_328_ * i_334_ + i_331_ * i_335_ > 0) method646(i)
            } else if (anIntArray5400!![i] != -1 || ((i_321_ - i_322_) * (anIntArray5343!![i_320_.toInt()] - anIntArray5343!![i_319_.toInt()]) - ((anIntArray5343!![i_318_.toInt()] - anIntArray5343!![i_319_.toInt()]) * (i_323_ - i_322_))) > 0) {
                rasterizer!!.clamp = i_321_ < 0 || i_322_ < 0 || i_323_ < 0 || i_321_ > aJavaThreadResource_5367!!.anInt2221 || i_322_ > aJavaThreadResource_5367!!.anInt2221 || i_323_ > aJavaThreadResource_5367!!.anInt2221
                if (bool_317_) {
                    val i_336_ = anIntArray5400!![i]
                    if (i_336_ == -1 || !billboardFaces!![i_336_]!!.aBoolean145) method658(i)
                } else {
                    val i_337_ = anIntArray5400!![i]
                    if (i_337_ != -1) {
                        val javaBillboardFace = billboardFaces!![i_337_]
                        val javaBillboardAttributes = billboardAttributes!![i_337_]
                        if (!javaBillboardFace!!.aBoolean145) method635(i)
                        toolkit.method3720(javaBillboardAttributes!!.anInt4312, javaBillboardAttributes.anInt4310, javaBillboardAttributes.anInt4320, javaBillboardAttributes.anInt4309, javaBillboardAttributes.anInt4307, javaBillboardAttributes.anInt4308, javaBillboardFace.aShort146.toInt() and 0xffff, javaBillboardAttributes.anInt4313, javaBillboardFace.aByte148.toInt(), javaBillboardFace.aByte156.toInt())
                    } else method635(i)
                }
            }
        }
    }

    fun na(): Int {
        if (!aBoolean5323) method655()
        return aShort5324.toInt()
    }

    // dependency of method643, not in genuine list
    private fun method646(i: Int) {
        var i_540_ = 0
        val i_541_ = toolkit.anInt7482
        val i_542_ = faceA!![i]
        val i_543_ = faceB!![i]
        val i_544_ = faceC!![i]
        var i_545_ = anIntArray5392!![i_542_.toInt()]
        var i_546_ = anIntArray5392!![i_543_.toInt()]
        var i_547_ = anIntArray5392!![i_544_.toInt()]
        if (faceAlpha == null) rasterizer!!.anInt1674 = 0
        else rasterizer!!.anInt1674 = faceAlpha!![i].toInt() and 0xff
        if (i_545_ >= i_541_) {
            anIntArray5315!![i_540_] = anIntArray5321!![i_542_.toInt()]
            anIntArray5371!![i_540_] = anIntArray5343!![i_542_.toInt()]
            anIntArray5381!![i_540_] = anIntArray5355!![i_542_.toInt()]
            anIntArray5377!![i_540_++] = anIntArray5368!![i] and 0xffff
        } else {
            val i_548_ = anIntArray5399!![i_542_.toInt()]
            val i_549_ = anIntArray5384!![i_542_.toInt()]
            val i_550_ = anIntArray5368!![i] and 0xffff
            if (i_547_ >= i_541_) {
                val i_551_ = (i_541_ - i_545_) * (65536 / (i_547_ - i_545_))
                anIntArray5315!![i_540_] = (aJavaThreadResource_5367!!.anInt2229 + ((i_548_ + ((anIntArray5399!![i_544_.toInt()] - i_548_) * i_551_ shr 16)) * toolkit.anInt7491 / i_541_))
                anIntArray5371!![i_540_] = (aJavaThreadResource_5367!!.anInt2215 + ((i_549_ + ((anIntArray5384!![i_544_.toInt()] - i_549_) * i_551_ shr 16)) * toolkit.anInt7497 / i_541_))
                anIntArray5381!![i_540_] = i_541_
                anIntArray5377!![i_540_++] = (i_550_ + (((anIntArray5366!![i] and 0xffff) - i_550_) * i_551_ shr 16))
            }
            if (i_546_ >= i_541_) {
                val i_552_ = (i_541_ - i_545_) * (65536 / (i_546_ - i_545_))
                anIntArray5315!![i_540_] = (aJavaThreadResource_5367!!.anInt2229 + ((i_548_ + ((anIntArray5399!![i_543_.toInt()] - i_548_) * i_552_ shr 16)) * toolkit.anInt7491 / i_541_))
                anIntArray5371!![i_540_] = (aJavaThreadResource_5367!!.anInt2215 + ((i_549_ + ((anIntArray5384!![i_543_.toInt()] - i_549_) * i_552_ shr 16)) * toolkit.anInt7497 / i_541_))
                anIntArray5381!![i_540_] = i_541_
                anIntArray5377!![i_540_++] = (i_550_ + (((anIntArray5337!![i] and 0xffff) - i_550_) * i_552_ shr 16))
            }
        }
        if (i_546_ >= i_541_) {
            anIntArray5315!![i_540_] = anIntArray5321!![i_543_.toInt()]
            anIntArray5371!![i_540_] = anIntArray5343!![i_543_.toInt()]
            anIntArray5381!![i_540_] = anIntArray5355!![i_543_.toInt()]
            anIntArray5377!![i_540_++] = anIntArray5337!![i] and 0xffff
        } else {
            val i_553_ = anIntArray5399!![i_543_.toInt()]
            val i_554_ = anIntArray5384!![i_543_.toInt()]
            val i_555_ = anIntArray5337!![i] and 0xffff
            if (i_545_ >= i_541_) {
                val i_556_ = (i_541_ - i_546_) * (65536 / (i_545_ - i_546_))
                anIntArray5315!![i_540_] = (aJavaThreadResource_5367!!.anInt2229 + ((i_553_ + ((anIntArray5399!![i_542_.toInt()] - i_553_) * i_556_ shr 16)) * toolkit.anInt7491 / i_541_))
                anIntArray5371!![i_540_] = (aJavaThreadResource_5367!!.anInt2215 + ((i_554_ + ((anIntArray5384!![i_542_.toInt()] - i_554_) * i_556_ shr 16)) * toolkit.anInt7497 / i_541_))
                anIntArray5381!![i_540_] = i_541_
                anIntArray5377!![i_540_++] = (i_555_ + (((anIntArray5368!![i] and 0xffff) - i_555_) * i_556_ shr 16))
            }
            if (i_547_ >= i_541_) {
                val i_557_ = (i_541_ - i_546_) * (65536 / (i_547_ - i_546_))
                anIntArray5315!![i_540_] = (aJavaThreadResource_5367!!.anInt2229 + ((i_553_ + ((anIntArray5399!![i_544_.toInt()] - i_553_) * i_557_ shr 16)) * toolkit.anInt7491 / i_541_))
                anIntArray5371!![i_540_] = (aJavaThreadResource_5367!!.anInt2215 + ((i_554_ + ((anIntArray5384!![i_544_.toInt()] - i_554_) * i_557_ shr 16)) * toolkit.anInt7497 / i_541_))
                anIntArray5381!![i_540_] = i_541_
                anIntArray5377!![i_540_++] = (i_555_ + (((anIntArray5366!![i] and 0xffff) - i_555_) * i_557_ shr 16))
            }
        }
        if (i_547_ >= i_541_) {
            anIntArray5315!![i_540_] = anIntArray5321!![i_544_.toInt()]
            anIntArray5371!![i_540_] = anIntArray5343!![i_544_.toInt()]
            anIntArray5381!![i_540_] = anIntArray5355!![i_544_.toInt()]
            anIntArray5377!![i_540_++] = anIntArray5366!![i] and 0xffff
        } else {
            val i_558_ = anIntArray5399!![i_544_.toInt()]
            val i_559_ = anIntArray5384!![i_544_.toInt()]
            val i_560_ = anIntArray5366!![i] and 0xffff
            if (i_546_ >= i_541_) {
                val i_561_ = (i_541_ - i_547_) * (65536 / (i_546_ - i_547_))
                anIntArray5315!![i_540_] = (aJavaThreadResource_5367!!.anInt2229 + ((i_558_ + ((anIntArray5399!![i_543_.toInt()] - i_558_) * i_561_ shr 16)) * toolkit.anInt7491 / i_541_))
                anIntArray5371!![i_540_] = (aJavaThreadResource_5367!!.anInt2215 + ((i_559_ + ((anIntArray5384!![i_543_.toInt()] - i_559_) * i_561_ shr 16)) * toolkit.anInt7497 / i_541_))
                anIntArray5381!![i_540_] = i_541_
                anIntArray5377!![i_540_++] = (i_560_ + (((anIntArray5337!![i] and 0xffff) - i_560_) * i_561_ shr 16))
            }
            if (i_545_ >= i_541_) {
                val i_562_ = (i_541_ - i_547_) * (65536 / (i_545_ - i_547_))
                anIntArray5315!![i_540_] = (aJavaThreadResource_5367!!.anInt2229 + ((i_558_ + ((anIntArray5399!![i_542_.toInt()] - i_558_) * i_562_ shr 16)) * toolkit.anInt7491 / i_541_))
                anIntArray5371!![i_540_] = (aJavaThreadResource_5367!!.anInt2215 + ((i_559_ + ((anIntArray5384!![i_542_.toInt()] - i_559_) * i_562_ shr 16)) * toolkit.anInt7497 / i_541_))
                anIntArray5381!![i_540_] = i_541_
                anIntArray5377!![i_540_++] = (i_560_ + (((anIntArray5368!![i] and 0xffff) - i_560_) * i_562_ shr 16))
            }
        }
        val i_563_ = anIntArray5315!![0]
        val i_564_ = anIntArray5315!![1]
        val i_565_ = anIntArray5315!![2]
        val i_566_ = anIntArray5371!![0]
        val i_567_ = anIntArray5371!![1]
        val i_568_ = anIntArray5371!![2]
        i_545_ = anIntArray5381!![0]
        i_546_ = anIntArray5381!![1]
        i_547_ = anIntArray5381!![2]
        rasterizer!!.clamp = false
        if (i_540_ == 3) {
            if (i_563_ < 0 || i_564_ < 0 || i_565_ < 0 || i_563_ > aJavaThreadResource_5367!!.anInt2221 || i_564_ > aJavaThreadResource_5367!!.anInt2221 || i_565_ > aJavaThreadResource_5367!!.anInt2221) rasterizer!!.clamp = true
            if (faceTextures == null || faceTextures!![i].toInt() == -1) {
                if (anIntArray5366!![i] == -1) rasterizer!!.method1018(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), (ItemSpriteCacheKey.HSV_TO_RGB!![anIntArray5368!![i] and 0xffff]))
                else rasterizer!!.method1022(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), anIntArray5377!![0].toFloat(), anIntArray5377!![1].toFloat(), anIntArray5377!![2].toFloat())
            } else {
                var i_569_ = -16777216
                if (faceAlpha != null) i_569_ = 255 - (faceAlpha!![i].toInt() and 0xff) shl 24
                val i_570_ = i_569_ or (anIntArray5368!![i] and 0xffffff)
                if (anIntArray5366!![i] == -1) rasterizer!!.method1024(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), texCoordU!![i]!![0], texCoordU!![i]!![1], texCoordU!![i]!![2], texCoordV!![i]!![0], texCoordV!![i]!![1], texCoordV!![i]!![2], i_570_, i_570_, i_570_, (aJavaThreadResource_5367!!.anInt2192), 0, 0, 0, faceTextures!![i].toInt())
                else rasterizer!!.method1024(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), texCoordU!![i]!![0], texCoordU!![i]!![1], texCoordU!![i]!![2], texCoordV!![i]!![0], texCoordV!![i]!![1], texCoordV!![i]!![2], i_570_, i_570_, i_570_, (aJavaThreadResource_5367!!.anInt2192), 0, 0, 0, faceTextures!![i].toInt())
            }
        }
        if (i_540_ == 4) {
            if (i_563_ < 0 || i_564_ < 0 || i_565_ < 0 || i_563_ > aJavaThreadResource_5367!!.anInt2221 || i_564_ > aJavaThreadResource_5367!!.anInt2221 || i_565_ > aJavaThreadResource_5367!!.anInt2221 || anIntArray5315!![3] < 0 || anIntArray5315!![3] > aJavaThreadResource_5367!!.anInt2221) rasterizer!!.clamp = true
            if (faceTextures == null || faceTextures!![i].toInt() == -1) {
                if (anIntArray5366!![i] == -1) {
                    val i_571_: Int = ItemSpriteCacheKey.HSV_TO_RGB!![anIntArray5368!![i] and 0xffff]
                    rasterizer!!.method1018(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), i_571_)
                    rasterizer!!.method1018(i_566_.toFloat(), i_568_.toFloat(), anIntArray5371!![3].toFloat(), i_563_.toFloat(), i_565_.toFloat(), anIntArray5315!![3].toFloat(), i_545_.toFloat(), i_546_.toFloat(), anIntArray5381!![3].toFloat(), i_571_)
                } else {
                    rasterizer!!.method1022(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), anIntArray5377!![0].toFloat(), anIntArray5377!![1].toFloat(), anIntArray5377!![2].toFloat())
                    rasterizer!!.method1022(i_566_.toFloat(), i_568_.toFloat(), anIntArray5371!![3].toFloat(), i_563_.toFloat(), i_565_.toFloat(), anIntArray5315!![3].toFloat(), i_545_.toFloat(), i_546_.toFloat(), anIntArray5381!![3].toFloat(), anIntArray5377!![0].toFloat(), anIntArray5377!![2].toFloat(), anIntArray5377!![3].toFloat())
                }
            } else {
                var i_572_ = -16777216
                if (faceAlpha != null) i_572_ = 255 - (faceAlpha!![i].toInt() and 0xff) shl 24
                val i_573_ = i_572_ or (anIntArray5368!![i] and 0xffffff)
                if (anIntArray5366!![i] == -1) {
                    rasterizer!!.method1024(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), texCoordU!![i]!![0], texCoordU!![i]!![1], texCoordU!![i]!![2], texCoordV!![i]!![0], texCoordV!![i]!![1], texCoordV!![i]!![2], i_573_, i_573_, i_573_, (aJavaThreadResource_5367!!.anInt2192), 0, 0, 0, faceTextures!![i].toInt())
                    rasterizer!!.method1024(
                        i_566_.toFloat(),
                        i_568_.toFloat(),
                        anIntArray5371!![3].toFloat(),
                        i_563_.toFloat(),
                        i_565_.toFloat(),
                        anIntArray5315!![3].toFloat(),
                        i_545_.toFloat(),
                        i_547_.toFloat(),
                        anIntArray5381!![3].toFloat(),
                        texCoordU!![i]!![0],
                        texCoordU!![i]!![1],
                        texCoordU!![i]!![2],
                        texCoordV!![i]!![0],
                        texCoordV!![i]!![1],
                        texCoordV!![i]!![2],
                        i_573_,
                        i_573_,
                        i_573_,
                        (aJavaThreadResource_5367!!.anInt2192),
                        0,
                        0,
                        0,
                        faceTextures!![i].toInt()
                    )
                } else {
                    rasterizer!!.method1024(i_566_.toFloat(), i_567_.toFloat(), i_568_.toFloat(), i_563_.toFloat(), i_564_.toFloat(), i_565_.toFloat(), i_545_.toFloat(), i_546_.toFloat(), i_547_.toFloat(), texCoordU!![i]!![0], texCoordU!![i]!![1], texCoordU!![i]!![2], texCoordV!![i]!![0], texCoordV!![i]!![1], texCoordV!![i]!![2], i_573_, i_573_, i_573_, (aJavaThreadResource_5367!!.anInt2192), 0, 0, 0, faceTextures!![i].toInt())
                    rasterizer!!.method1024(
                        i_566_.toFloat(),
                        i_568_.toFloat(),
                        anIntArray5371!![3].toFloat(),
                        i_563_.toFloat(),
                        i_565_.toFloat(),
                        anIntArray5315!![3].toFloat(),
                        i_545_.toFloat(),
                        i_547_.toFloat(),
                        anIntArray5381!![3].toFloat(),
                        texCoordU!![i]!![0],
                        texCoordU!![i]!![1],
                        texCoordU!![i]!![2],
                        texCoordV!![i]!![0],
                        texCoordV!![i]!![1],
                        texCoordV!![i]!![2],
                        i_573_,
                        i_573_,
                        i_573_,
                        (aJavaThreadResource_5367!!.anInt2192),
                        0,
                        0,
                        0,
                        faceTextures!![i].toInt()
                    )
                }
            }
        }
    }

    // dependency of method657, not in genuine list
    private fun method647() {
        if (anInt5354 == 0) method634(false)
        else if (toolkit.anInt7485 > 1) {
            synchronized(this) {
                method640()
            }
        } else method640()
    }

    // dependency of method647, not in genuine list
    private fun method640() {
        for (i in 0..<faceCount) {
            val i_287_: Short = if (faceTextures != null) faceTextures!![i] else (-1).toShort()
            if (i_287_.toInt() == -1) {
                val i_288_ = faceColour!![i].toInt() and 0xffff
                val i_289_ = (i_288_ and 0x7f) * ambient shr 7
                val i_290_ = method303(i_288_ and 0x7f.inv() or i_289_)
                if (anIntArray5366!![i] == -1) {
                    val i_291_ = anIntArray5368!![i] and 0x1ffff.inv()
                    anIntArray5368!![i] = i_291_ or method2198(i_291_ shr 17, i_290_.toInt())
                } else if (anIntArray5366!![i] != -2) {
                    var i_292_ = anIntArray5368!![i] and 0x1ffff.inv()
                    anIntArray5368!![i] = i_292_ or method2198(i_292_ shr 17, i_290_.toInt())
                    i_292_ = anIntArray5337!![i] and 0x1ffff.inv()
                    anIntArray5337!![i] = i_292_ or method2198(i_292_ shr 17, i_290_.toInt())
                    i_292_ = anIntArray5366!![i] and 0x1ffff.inv()
                    anIntArray5366!![i] = i_292_ or method2198(i_292_ shr 17, i_290_.toInt())
                }
            }
        }
        anInt5354 = 2
    }

    private fun method649() {
        aClass360Array5360 = arrayOfNulls<Class360>(maxVertex)
        for (i in 0..<maxVertex) aClass360Array5360!![i] = Class360()
        for (i in 0..<faceCount) {
            val i_599_ = faceA!![i]
            val i_600_ = faceB!![i]
            val i_601_ = faceC!![i]
            val i_602_ = vertexX!![i_600_.toInt()] - vertexX!![i_599_.toInt()]
            val i_603_ = vertexY!![i_600_.toInt()] - vertexY!![i_599_.toInt()]
            val i_604_ = vertexZ!![i_600_.toInt()] - vertexZ!![i_599_.toInt()]
            val i_605_ = vertexX!![i_601_.toInt()] - vertexX!![i_599_.toInt()]
            val i_606_ = vertexY!![i_601_.toInt()] - vertexY!![i_599_.toInt()]
            val i_607_ = vertexZ!![i_601_.toInt()] - vertexZ!![i_599_.toInt()]
            var i_608_ = i_603_ * i_607_ - i_606_ * i_604_
            var i_609_ = i_604_ * i_605_ - i_607_ * i_602_
            var i_610_: Int
            i_610_ = i_602_ * i_606_ - i_605_ * i_603_
            while ((i_608_ > 8192 || i_609_ > 8192 || i_610_ > 8192 || i_608_ < -8192 || i_609_ < -8192 || i_610_ < -8192)) {
                i_608_ = i_608_ shr 1
                i_609_ = i_609_ shr 1
                i_610_ = i_610_ shr 1
            }
            var i_611_ = sqrt((i_608_ * i_608_ + i_609_ * i_609_ + i_610_ * i_610_).toDouble()).toInt()
            if (i_611_ <= 0) i_611_ = 1
            i_608_ = i_608_ * 256 / i_611_
            i_609_ = i_609_ * 256 / i_611_
            i_610_ = i_610_ * 256 / i_611_
            val i_612_: Byte
            if (shadingType == null) i_612_ = 0.toByte()
            else i_612_ = shadingType!![i]
            if (i_612_.toInt() == 0) {
                var class360 = aClass360Array5360!![i_599_.toInt()]
                class360!!.anInt4430 += i_608_
                class360.anInt4428 += i_609_
                class360.anInt4427 += i_610_
                class360.anInt4429++
                class360 = aClass360Array5360!![i_600_.toInt()]
                class360!!.anInt4430 += i_608_
                class360.anInt4428 += i_609_
                class360.anInt4427 += i_610_
                class360.anInt4429++
                class360 = aClass360Array5360!![i_601_.toInt()]
                class360!!.anInt4430 += i_608_
                class360.anInt4428 += i_609_
                class360.anInt4427 += i_610_
                class360.anInt4429++
            } else if (i_612_.toInt() == 1) {
                if (aClass41Array5385 == null) aClass41Array5385 = arrayOfNulls<Class41>(faceCount)
                aClass41Array5385!![i] = Class41()
                val class41 = aClass41Array5385!![i]
                class41!!.anInt561 = i_608_
                class41.anInt560 = i_609_
                class41.anInt559 = i_610_
            }
        }
    }

    private fun method650(bool: Boolean, bool_616_: Boolean, i: Int, i_617_: Int) {
        if (billboardFaces != null) {
            for (i_618_ in 0..<billboardCount) {
                val javaBillboardFace = billboardFaces!![i_618_]
                anIntArray5400!![javaBillboardFace!!.anInt144] = i_618_
            }
        }
        if (transparent || billboardFaces != null) {
            if ((functionMask and 0x100) == 0 && faceIndices != null) {
                for (i_619_ in 0..<faceCount) {
                    val i_620_ = faceIndices!![i_619_]
                    method643(i_620_.toInt(), bool, bool_616_)
                }
            } else {
                for (i_621_ in 0..<faceCount) {
                    if (!method629(i_621_) && !method638(i_621_)) method643(i_621_, bool, bool_616_)
                }
                if (facePriority == null) {
                    for (i_622_ in 0..<faceCount) {
                        if (method629(i_622_) || method638(i_622_)) method643(i_622_, bool, bool_616_)
                    }
                } else {
                    for (i_623_ in 0..11) {
                        for (i_624_ in 0..<faceCount) {
                            if (facePriority!![i_624_].toInt() == i_623_ && (method629(i_624_) || method638(i_624_))) method643(i_624_, bool, bool_616_)
                        }
                    }
                }
            }
        } else {
            for (i_625_ in 0..<faceCount) method643(i_625_, bool, bool_616_)
        }
    }

    private fun method654(matrix: Matrix?, i_632_: Int) {
        if (maxVertex >= 1) {
            aClass101_Sub1_5320 = matrix as Matrix_Sub1
            val class101_sub1 = toolkit.aClass101_Sub1_7492
            if (!aBoolean5323) method655()
            var bool = false
            if (aClass101_Sub1_5320!!.aFloat5672 == 16384.0f && aClass101_Sub1_5320!!.aFloat5673 == 0.0f && aClass101_Sub1_5320!!.aFloat5669 == 0.0f && aClass101_Sub1_5320!!.aFloat5655 == 0.0f && aClass101_Sub1_5320!!.aFloat5678 == 16384.0f && aClass101_Sub1_5320!!.aFloat5666 == 0.0f && aClass101_Sub1_5320!!.aFloat5662 == 0.0f && aClass101_Sub1_5320!!.aFloat5680 == 0.0f && (aClass101_Sub1_5320!!.aFloat5664 == 16384.0f)) bool = true
            val f = (class101_sub1!!.aFloat5681 + (class101_sub1.aFloat5662 * aClass101_Sub1_5320!!.aFloat5686) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320!!.aFloat5685) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320!!.aFloat5681))
            val f_633_ = (if (bool) class101_sub1.aFloat5680 else ((class101_sub1.aFloat5662 * aClass101_Sub1_5320!!.aFloat5673) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320!!.aFloat5678) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320!!.aFloat5680)))
            val i_634_ = (f + aShort5329.toFloat() * f_633_).toInt()
            val i_635_ = (f + aShort5365.toFloat() * f_633_).toInt()
            val i_636_: Int
            val i_637_: Int
            if (i_634_ > i_635_) {
                i_636_ = i_635_ - aShort5324
                i_637_ = i_634_ + aShort5324
            } else {
                i_636_ = i_634_ - aShort5324
                i_637_ = i_635_ + aShort5324
            }
            if (i_636_ < toolkit.anInt7494 && i_637_ > toolkit.anInt7482) {
                val f_638_ = (class101_sub1.aFloat5686 + (class101_sub1.aFloat5672 * aClass101_Sub1_5320!!.aFloat5686) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320!!.aFloat5685) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320!!.aFloat5681))
                val f_639_ = (if (bool) class101_sub1.aFloat5673 else ((class101_sub1.aFloat5672 * aClass101_Sub1_5320!!.aFloat5673) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320!!.aFloat5678)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320!!.aFloat5680))))
                val i_640_ = (f_638_ + aShort5329.toFloat() * f_639_).toInt()
                val i_641_ = (f_638_ + aShort5365.toFloat() * f_639_).toInt()
                val i_642_: Int
                val i_643_: Int
                if (i_640_ > i_641_) {
                    i_642_ = ((i_641_ - aShort5324) * toolkit.anInt7491)
                    i_643_ = ((i_640_ + aShort5324) * toolkit.anInt7491)
                } else {
                    i_642_ = ((i_640_ - aShort5324) * toolkit.anInt7491)
                    i_643_ = ((i_641_ + aShort5324) * toolkit.anInt7491)
                }
                if (i_642_ / i_637_ >= toolkit.anInt7508 || (i_643_ / i_637_ <= toolkit.anInt7509)) return
                val f_644_ = (class101_sub1.aFloat5685 + (class101_sub1.aFloat5655 * aClass101_Sub1_5320!!.aFloat5686) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320!!.aFloat5685) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320!!.aFloat5681))
                val f_645_ = (if (bool) class101_sub1.aFloat5678 else ((class101_sub1.aFloat5655 * aClass101_Sub1_5320!!.aFloat5673) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320!!.aFloat5678)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320!!.aFloat5680))))
                val i_646_ = (f_644_ + aShort5329.toFloat() * f_645_).toInt()
                val i_647_ = (f_644_ + aShort5365.toFloat() * f_645_).toInt()
                val i_648_: Int
                val i_649_: Int
                if (i_646_ > i_647_) {
                    i_648_ = ((i_647_ - aShort5324) * toolkit.anInt7497)
                    i_649_ = ((i_646_ + aShort5324) * toolkit.anInt7497)
                } else {
                    i_648_ = ((i_646_ - aShort5324) * toolkit.anInt7497)
                    i_649_ = ((i_647_ + aShort5324) * toolkit.anInt7497)
                }
                if (i_648_ / i_637_ >= toolkit.anInt7506 || (i_649_ / i_637_ <= toolkit.anInt7490)) return
                val f_650_: Float
                val f_651_: Float
                val f_652_: Float
                val f_653_: Float
                val f_654_: Float
                val f_655_: Float
                if (bool) {
                    f_650_ = class101_sub1.aFloat5672
                    f_651_ = class101_sub1.aFloat5655
                    f_652_ = class101_sub1.aFloat5662
                    f_653_ = class101_sub1.aFloat5669
                    f_654_ = class101_sub1.aFloat5666
                    f_655_ = class101_sub1.aFloat5664
                } else {
                    f_650_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320!!.aFloat5672) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320!!.aFloat5655)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320!!.aFloat5662)))
                    f_651_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320!!.aFloat5672) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320!!.aFloat5655)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320!!.aFloat5662)))
                    f_652_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320!!.aFloat5672) + (class101_sub1.aFloat5680 * (aClass101_Sub1_5320!!.aFloat5655)) + (class101_sub1.aFloat5664 * (aClass101_Sub1_5320!!.aFloat5662)))
                    f_653_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320!!.aFloat5669) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320!!.aFloat5666)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320!!.aFloat5664)))
                    f_654_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320!!.aFloat5669) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320!!.aFloat5666)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320!!.aFloat5664)))
                    f_655_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320!!.aFloat5669) + (class101_sub1.aFloat5680 * (aClass101_Sub1_5320!!.aFloat5666)) + (class101_sub1.aFloat5664 * (aClass101_Sub1_5320!!.aFloat5664)))
                }
                if (toolkit.anInt7485 > 1) {
                    synchronized(this) {
                        while (aBoolean5357) {
                            try {
                                (this as Object).wait()
                            } catch (interruptedexception: InterruptedException) {
                                /* empty */
                            }
                        }
                        aBoolean5357 = true
                    }
                }
                method632(Thread.currentThread())
                rasterizer!!.method1023((i_632_ and 0x2) != 0)
                var bool_656_ = false
                val bool_657_ = i_636_ <= toolkit.anInt7482
                val bool_658_ = (bool_657_ || emitters != null || effectors != null)
                aJavaThreadResource_5367!!.anInt2221 = rasterizer!!.width
                aJavaThreadResource_5367!!.anInt2229 = rasterizer!!.anInt1665
                aJavaThreadResource_5367!!.anInt2215 = rasterizer!!.anInt1668
                val i_659_ = toolkit.anInt7491
                val i_660_ = toolkit.anInt7497
                val i_661_ = toolkit.anInt7482
                for (i_662_ in 0..<vertexCount) {
                    val i_663_ = vertexX!![i_662_]
                    val i_664_ = vertexY!![i_662_]
                    val i_665_ = vertexZ!![i_662_]
                    val f_666_ = (f_638_ + f_650_ * i_663_.toFloat() + f_639_ * i_664_.toFloat() + f_653_ * i_665_.toFloat())
                    val f_667_ = (f_644_ + f_651_ * i_663_.toFloat() + f_645_ * i_664_.toFloat() + f_654_ * i_665_.toFloat())
                    val f_668_ = (f + f_652_ * i_663_.toFloat() + f_633_ * i_664_.toFloat() + f_655_ * i_665_.toFloat())
                    anIntArray5355!![i_662_] = f_668_.toInt()
                    if (f_668_ >= i_661_.toFloat()) {
                        anIntArray5321!![i_662_] = (aJavaThreadResource_5367!!.anInt2229 + (f_666_ * i_659_.toFloat() / f_668_).toInt())
                        anIntArray5343!![i_662_] = (aJavaThreadResource_5367!!.anInt2215 + (f_667_ * i_660_.toFloat() / f_668_).toInt())
                    } else {
                        anIntArray5321!![i_662_] = -5000
                        bool_656_ = true
                    }
                    if (bool_658_) {
                        anIntArray5399!![i_662_] = f_666_.toInt()
                        anIntArray5384!![i_662_] = f_667_.toInt()
                        anIntArray5392!![i_662_] = f_668_.toInt()
                    }
                    if (aJavaThreadResource_5367!!.aBoolean2195) anIntArray5362!![i_662_] = ((aClass101_Sub1_5320!!.aFloat5685) + ((aClass101_Sub1_5320!!.aFloat5655 * i_663_.toFloat()) + (aClass101_Sub1_5320!!.aFloat5678 * i_664_.toFloat()) + (aClass101_Sub1_5320!!.aFloat5666 * i_665_.toFloat()))).toInt()
                }
                if (billboardFaces != null) {
                    for (i_669_ in 0..<billboardCount) {
                        val javaBillboardFace = billboardFaces!![i_669_]
                        val javaBillboardAttributes = billboardAttributes!![i_669_]
                        val i_670_ = faceA!![javaBillboardFace!!.anInt144]
                        val i_671_ = faceB!![javaBillboardFace.anInt144]
                        val i_672_ = faceC!![javaBillboardFace.anInt144]
                        val i_673_ = ((vertexX!![i_670_.toInt()] + vertexX!![i_671_.toInt()] + vertexX!![i_672_.toInt()]) / 3)
                        val i_674_ = ((vertexY!![i_670_.toInt()] + vertexY!![i_671_.toInt()] + vertexY!![i_672_.toInt()]) / 3)
                        val i_675_ = ((vertexZ!![i_670_.toInt()] + vertexZ!![i_671_.toInt()] + vertexZ!![i_672_.toInt()]) / 3)
                        val f_676_ = (javaBillboardAttributes!!.anInt4316.toFloat() + (f_638_ + f_650_ * i_673_.toFloat() + f_639_ * i_674_.toFloat() + f_653_ * i_675_.toFloat()))
                        val f_677_ = (javaBillboardAttributes.anInt4317.toFloat() + (f_644_ + f_651_ * i_673_.toFloat() + f_645_ * i_674_.toFloat() + f_654_ * i_675_.toFloat()))
                        val f_678_ = (f + f_652_ * i_673_.toFloat() + f_633_ * i_674_.toFloat() + f_655_ * i_675_.toFloat())
                        if (f_678_ > (toolkit.anInt7482).toFloat()) {
                            javaBillboardAttributes.anInt4312 = (toolkit.anInt7510 + (f_676_ * i_659_.toFloat() / f_678_).toInt())
                            javaBillboardAttributes.anInt4310 = (toolkit.anInt7504 + (f_677_ * i_660_.toFloat() / f_678_).toInt())
                            javaBillboardAttributes.anInt4320 = (f_678_.toInt() - javaBillboardFace.anInt154)
                            javaBillboardAttributes.anInt4309 = (((javaBillboardAttributes.anInt4314) * (javaBillboardFace.aShort150) * i_659_).toFloat() / (f_678_ * 128.0f)).toInt()
                            javaBillboardAttributes.anInt4307 = (((javaBillboardAttributes.anInt4311) * (javaBillboardFace.aShort143) * i_660_).toFloat() / (f_678_ * 128.0f)).toInt()
                        } else {
                            javaBillboardAttributes.anInt4307 = 0
                            javaBillboardAttributes.anInt4309 = javaBillboardAttributes.anInt4307
                        }
                    }
                }
                method634(true)
                rasterizer!!.aBoolean1669 = (i_632_ and 0x1) == 0
                try {
                    method650(bool_656_, ((aJavaThreadResource_5367!!.aBoolean2201 && (i_637_ > aJavaThreadResource_5367!!.anInt2210)) || aJavaThreadResource_5367!!.aBoolean2195), i_636_, i_637_ - i_636_)
                } catch (exception: Exception) {
                    /* empty */
                }
                if (billboardFaces != null) {
                    for (i_721_ in 0..<faceCount) anIntArray5400!![i_721_] = -1
                }
                rasterizer = null
                if (toolkit.anInt7485 > 1) {
                    synchronized(this) {
                        aBoolean5357 = false
                        (this as Object).notifyAll()
                    }
                }
            }
        }
    }

    private fun method655() {
        if (!aBoolean5323) {
            var i = 0
            var i_722_ = 0
            var i_723_ = 32767
            var i_724_ = 32767
            var i_725_ = 32767
            var i_726_ = -32768
            var i_727_ = -32768
            var i_728_ = -32768
            for (i_729_ in 0..<maxVertex) {
                val i_730_ = vertexX!![i_729_]
                val i_731_ = vertexY!![i_729_]
                val i_732_ = vertexZ!![i_729_]
                if (i_730_ < i_723_) i_723_ = i_730_
                if (i_730_ > i_726_) i_726_ = i_730_
                if (i_731_ < i_724_) i_724_ = i_731_
                if (i_731_ > i_727_) i_727_ = i_731_
                if (i_732_ < i_725_) i_725_ = i_732_
                if (i_732_ > i_728_) i_728_ = i_732_
                var i_733_ = i_730_ * i_730_ + i_732_ * i_732_
                if (i_733_ > i) i = i_733_
                i_733_ += i_731_ * i_731_
                if (i_733_ > i_722_) i_722_ = i_733_
            }
            aShort5395 = i_723_.toShort()
            aShort5393 = i_726_.toShort()
            aShort5329 = i_724_.toShort()
            aShort5365 = i_727_.toShort()
            aShort5352 = i_725_.toShort()
            aShort5331 = i_728_.toShort()
            aShort5324 = (sqrt(i.toDouble()) + 0.99).toInt().toShort()
            aShort5348 = (sqrt(i_722_.toDouble()) + 0.99).toInt().toShort()
            aBoolean5323 = true
        }
    }

    private fun method656(i: Int): Int {
        var i = i
        if (i < 2) i = 2
        else if (i > 126) i = 126
        return i
    }

    private fun method657(bool: Boolean) {
        if (anInt5354 == 1) method647()
        else if (anInt5354 == 2) {
            if ((functionMask and 0x97098) == 0 && texCoordU == null) faceColour = null
            if (bool) shadingType = null
        } else {
            method636()
            val i = toolkit.anInt7484
            val i_734_ = toolkit.anInt7473
            val i_735_ = toolkit.anInt7479
            val i_736_ = toolkit.anInt7500 shr 8
            val i_737_ = toolkit.anInt7474 * 768 / contrast
            val i_738_ = toolkit.anInt7478 * 768 / contrast
            if (anIntArray5368 == null) {
                anIntArray5368 = IntArray(faceCount)
                anIntArray5337 = IntArray(faceCount)
                anIntArray5366 = IntArray(faceCount)
            }
            for (i_739_ in 0..<faceCount) {
                var i_740_: Byte
                if (shadingType == null) i_740_ = 0.toByte()
                else i_740_ = shadingType!![i_739_]
                val i_741_: Byte
                if (faceAlpha == null) i_741_ = 0.toByte()
                else i_741_ = faceAlpha!![i_739_]
                val i_742_: Short
                if (faceTextures == null) i_742_ = (-1).toShort()
                else i_742_ = faceTextures!![i_739_]
                if (i_741_.toInt() == -2) i_740_ = 3.toByte()
                if (i_741_.toInt() == -1) i_740_ = 2.toByte()
                if (i_742_.toInt() == -1) {
                    if (i_740_.toInt() == 0) {
                        val i_743_ = faceColour!![i_739_].toInt() and 0xffff
                        val i_744_ = (i_743_ and 0x7f) * ambient shr 7
                        val i_745_ = method303(i_743_ and 0x7f.inv() or i_744_)
                        var class360: Class360
                        if (aClass360Array5313 != null && (aClass360Array5313!![faceA!![i_739_].toInt()] != null)) class360 = aClass360Array5313!![faceA!![i_739_].toInt()]!!
                        else class360 = aClass360Array5360!![faceA!![i_739_].toInt()]!!
                        var i_746_ = (((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) shr 16)
                        var i_747_ = if (i_746_ > 256) i_737_ else i_738_
                        var i_748_ = (i_736_ shr 1) + (i_747_ * i_746_ shr 17)
                        anIntArray5368!![i_739_] = i_748_ shl 17 or method2198(i_748_, i_745_.toInt())
                        if (aClass360Array5313 != null && (aClass360Array5313!![faceB!![i_739_].toInt()] != null)) class360 = aClass360Array5313!![faceB!![i_739_].toInt()]!!
                        else class360 = aClass360Array5360!![faceB!![i_739_].toInt()]!!
                        i_746_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) shr 16
                        i_747_ = if (i_746_ > 256) i_737_ else i_738_
                        i_748_ = (i_736_ shr 1) + (i_747_ * i_746_ shr 17)
                        anIntArray5337!![i_739_] = i_748_ shl 17 or method2198(i_748_, i_745_.toInt())
                        if (aClass360Array5313 != null && (aClass360Array5313!![faceC!![i_739_].toInt()] != null)) class360 = aClass360Array5313!![faceC!![i_739_].toInt()]!!
                        else class360 = aClass360Array5360!![faceC!![i_739_].toInt()]!!
                        i_746_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) shr 16
                        i_747_ = if (i_746_ > 256) i_737_ else i_738_
                        i_748_ = (i_736_ shr 1) + (i_747_ * i_746_ shr 17)
                        anIntArray5366!![i_739_] = i_748_ shl 17 or method2198(i_748_, i_745_.toInt())
                    } else if (i_740_.toInt() == 1) {
                        val i_749_ = faceColour!![i_739_].toInt() and 0xffff
                        val i_750_ = (i_749_ and 0x7f) * ambient shr 7
                        val i_751_ = method303(i_749_ and 0x7f.inv() or i_750_)
                        val class41 = aClass41Array5385!![i_739_]
                        val i_752_ = ((i * class41!!.anInt561 + i_734_ * class41.anInt560 + i_735_ * class41.anInt559) shr 16)
                        val i_753_ = if (i_752_ > 256) i_737_ else i_738_
                        val i_754_ = (i_736_ shr 1) + (i_753_ * i_752_ shr 17)
                        anIntArray5368!![i_739_] = i_754_ shl 17 or method2198(i_754_, i_751_.toInt())
                        anIntArray5366!![i_739_] = -1
                    } else if (i_740_.toInt() == 3) {
                        anIntArray5368!![i_739_] = 128
                        anIntArray5366!![i_739_] = -1
                    } else anIntArray5366!![i_739_] = -2
                } else {
                    val i_755_ = faceColour!![i_739_].toInt() and 0xffff
                    if (i_740_.toInt() == 0) {
                        var class360: Class360
                        if (aClass360Array5313 != null && (aClass360Array5313!![faceA!![i_739_].toInt()] != null)) class360 = aClass360Array5313!![faceA!![i_739_].toInt()]!!
                        else class360 = aClass360Array5360!![faceA!![i_739_].toInt()]!!
                        var i_756_ = (((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) shr 16)
                        var i_757_ = if (i_756_ > 256) i_737_ else i_738_
                        var i_758_ = method656((i_736_ shr 2) + (i_757_ * i_756_ shr 18))
                        anIntArray5368!![i_739_] = i_758_ shl 24 or method642(i_755_, i_742_, i_758_)
                        if (aClass360Array5313 != null && (aClass360Array5313!![faceB!![i_739_].toInt()] != null)) class360 = aClass360Array5313!![faceB!![i_739_].toInt()]!!
                        else class360 = aClass360Array5360!![faceB!![i_739_].toInt()]!!
                        i_756_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) shr 16
                        i_757_ = if (i_756_ > 256) i_737_ else i_738_
                        i_758_ = method656((i_736_ shr 2) + (i_757_ * i_756_ shr 18))
                        anIntArray5337!![i_739_] = i_758_ shl 24 or method642(i_755_, i_742_, i_758_)
                        if (aClass360Array5313 != null && (aClass360Array5313!![faceC!![i_739_].toInt()] != null)) class360 = aClass360Array5313!![faceC!![i_739_].toInt()]!!
                        else class360 = aClass360Array5360!![faceC!![i_739_].toInt()]!!
                        i_756_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) shr 16
                        i_757_ = if (i_756_ > 256) i_737_ else i_738_
                        i_758_ = method656((i_736_ shr 2) + (i_757_ * i_756_ shr 18))
                        anIntArray5366!![i_739_] = i_758_ shl 24 or method642(i_755_, i_742_, i_758_)
                    } else if (i_740_.toInt() == 1) {
                        val class41 = aClass41Array5385!![i_739_]
                        val i_759_ = ((i * class41!!.anInt561 + i_734_ * class41.anInt560 + i_735_ * class41.anInt559) shr 16)
                        val i_760_ = if (i_759_ > 256) i_737_ else i_738_
                        val i_761_ = method656((i_736_ shr 2) + (i_760_ * i_759_ shr 18))
                        anIntArray5368!![i_739_] = i_761_ shl 24 or method642(i_755_, i_742_, i_761_)
                        anIntArray5366!![i_739_] = -1
                    } else anIntArray5366!![i_739_] = -2
                }
            }
            aClass360Array5360 = null
            aClass360Array5313 = null
            aClass41Array5385 = null
            if ((functionMask and 0x97098) == 0 && texCoordU == null) faceColour = null
            if (bool) shadingType = null
            anInt5354 = 2
        }
    }

    override fun O(i: Int, i_765_: Int, i_766_: Int) {
        check(!(i != 128 && (functionMask and 0x1) != 1))
        check(!(i_765_ != 128 && (functionMask and 0x2) != 2))
        check(!(i_766_ != 128 && (functionMask and 0x4) != 4))
        synchronized(this) {
            for (i_767_ in 0..<vertexCount) {
                vertexX!![i_767_] = vertexX!![i_767_] * i shr 7
                vertexY!![i_767_] = vertexY!![i_767_] * i_765_ shr 7
                vertexZ!![i_767_] = vertexZ!![i_767_] * i_766_ shr 7
            }
            aBoolean5323 = false
        }
    }

    override fun prepareAnimation(): Boolean {
        // Class64_Sub1.method633 lights and calculates normals for the bind pose before the animated copy is posed
        method634(false)
        method636()
        if (vertexLabels == null) return false
        originX = 0
        originY = 0
        originZ = 0
        return true
    }

    override fun transform(type: Int, labels: IntArray, x: Int, y: Int, z: Int) {
        var x = x
        var y = y
        var z = z
        val vertexLabels = vertexLabels!!
        val vertexX = vertexX!!
        val vertexY = vertexY!!
        val vertexZ = vertexZ!!
        when (type) {
            0 -> {
                x = x shl 4
                y = y shl 4
                z = z shl 4
                upscaleForAnimation()
                var count = 0
                originX = 0
                originY = 0
                originZ = 0
                for (label in labels) {
                    if (label >= vertexLabels.size) continue
                    for (vertex in vertexLabels[label]!!) {
                        originX += vertexX[vertex]
                        originY += vertexY[vertex]
                        originZ += vertexZ[vertex]
                        count++
                    }
                }
                if (count > 0) {
                    originX = originX / count + x
                    originY = originY / count + y
                    originZ = originZ / count + z
                } else {
                    originX = x
                    originY = y
                    originZ = z
                }
            }
            1 -> {
                x = x shl 4
                y = y shl 4
                z = z shl 4
                upscaleForAnimation()
                for (label in labels) {
                    if (label >= vertexLabels.size) continue
                    for (vertex in vertexLabels[label]!!) {
                        vertexX[vertex] += x
                        vertexY[vertex] += y
                        vertexZ[vertex] += z
                    }
                }
            }
            2 -> for (label in labels) {
                if (label >= vertexLabels.size) continue
                for (vertex in vertexLabels[label]!!) {
                    vertexX[vertex] -= originX
                    vertexY[vertex] -= originY
                    vertexZ[vertex] -= originZ
                    // Roll, pitch then yaw
                    if (z != 0) {
                        val sin = Mesh.anIntArray1207[z]
                        val cos = Mesh.anIntArray1204[z]
                        val rotated = (vertexY[vertex] * sin + vertexX[vertex] * cos + 16383) shr 14
                        vertexY[vertex] = (vertexY[vertex] * cos - vertexX[vertex] * sin + 16383) shr 14
                        vertexX[vertex] = rotated
                    }
                    if (x != 0) {
                        val sin = Mesh.anIntArray1207[x]
                        val cos = Mesh.anIntArray1204[x]
                        val rotated = (vertexY[vertex] * cos - vertexZ[vertex] * sin + 16383) shr 14
                        vertexZ[vertex] = (vertexY[vertex] * sin + vertexZ[vertex] * cos + 16383) shr 14
                        vertexY[vertex] = rotated
                    }
                    if (y != 0) {
                        val sin = Mesh.anIntArray1207[y]
                        val cos = Mesh.anIntArray1204[y]
                        val rotated = (vertexZ[vertex] * sin + vertexX[vertex] * cos + 16383) shr 14
                        vertexZ[vertex] = (vertexZ[vertex] * cos - vertexX[vertex] * sin + 16383) shr 14
                        vertexX[vertex] = rotated
                    }
                    vertexX[vertex] += originX
                    vertexY[vertex] += originY
                    vertexZ[vertex] += originZ
                }
            }
            3 -> for (label in labels) {
                if (label >= vertexLabels.size) continue
                for (vertex in vertexLabels[label]!!) {
                    vertexX[vertex] -= originX
                    vertexY[vertex] -= originY
                    vertexZ[vertex] -= originZ
                    vertexX[vertex] = vertexX[vertex] * x / 128
                    vertexY[vertex] = vertexY[vertex] * y / 128
                    vertexZ[vertex] = vertexZ[vertex] * z / 128
                    vertexX[vertex] += originX
                    vertexY[vertex] += originY
                    vertexZ[vertex] += originZ
                }
            }
            // Billboard updates are omitted from 5 and 7 as billboards aren't drawn
            5 -> {
                val faceLabels = faceLabels ?: return
                val faceAlpha = faceAlpha ?: return
                for (label in labels) {
                    if (label >= faceLabels.size) continue
                    for (face in faceLabels[label]!!) {
                        faceAlpha[face] = ((faceAlpha[face].toInt() and 0xff) + x * 8).coerceIn(0, 255).toByte()
                    }
                }
            }
            7 -> {
                val faceLabels = faceLabels ?: return
                val faceColour = faceColour ?: return
                for (label in labels) {
                    if (label >= faceLabels.size) continue
                    for (face in faceLabels[label]!!) {
                        val colour = faceColour[face].toInt() and 0xffff
                        val hue = (colour shr 10 and 0x3f) + x and 0x3f
                        val saturation = ((colour shr 7 and 0x7) + y).coerceIn(0, 7)
                        val lightness = ((colour and 0x7f) + z).coerceIn(0, 127)
                        faceColour[face] = (hue shl 10 or (saturation shl 7) or lightness).toShort()
                    }
                    coloursChanged = true
                }
            }
        }
    }

    override fun finishAnimation() {
        if (upscaled) {
            val vertexX = vertexX!!
            val vertexY = vertexY!!
            val vertexZ = vertexZ!!
            for (i in 0 until vertexCount) {
                vertexX[i] = vertexX[i] + 7 shr 4
                vertexY[i] = vertexY[i] + 7 shr 4
                vertexZ[i] = vertexZ[i] + 7 shr 4
            }
            upscaled = false
        }
        if (coloursChanged) {
            method647()
            coloursChanged = false
        }
        aBoolean5323 = false
    }

    /** Vertices are animated with 4 bits of extra precision, removed again in [finishAnimation]. */
    private fun upscaleForAnimation() {
        if (upscaled) return
        val vertexX = vertexX!!
        val vertexY = vertexY!!
        val vertexZ = vertexZ!!
        for (i in 0 until vertexCount) {
            vertexX[i] = vertexX[i] shl 4
            vertexY[i] = vertexY[i] shl 4
            vertexZ[i] = vertexZ[i] shl 4
        }
        upscaled = true
    }

    // dependency of method643, not in genuine list
    private fun method658(i: Int) {
        if (aJavaThreadResource_5367!!.aBoolean2195) {
            val i_777_ = faceA!![i]
            val i_778_ = faceB!![i]
            val i_779_ = faceC!![i]
            var i_780_ = 0
            var i_781_ = 0
            var i_782_ = 0
            if (anIntArray5362!![i_777_.toInt()] > aJavaThreadResource_5367!!.anInt2197) i_780_ = 255
            else if (anIntArray5362!![i_777_.toInt()] > aJavaThreadResource_5367!!.anInt2211) i_780_ = ((aJavaThreadResource_5367!!.anInt2211 - anIntArray5362!![i_777_.toInt()]) * 255 / (aJavaThreadResource_5367!!.anInt2211 - aJavaThreadResource_5367!!.anInt2197))
            if (anIntArray5362!![i_778_.toInt()] > aJavaThreadResource_5367!!.anInt2197) i_781_ = 255
            else if (anIntArray5362!![i_778_.toInt()] > aJavaThreadResource_5367!!.anInt2211) i_781_ = ((aJavaThreadResource_5367!!.anInt2211 - anIntArray5362!![i_778_.toInt()]) * 255 / (aJavaThreadResource_5367!!.anInt2211 - aJavaThreadResource_5367!!.anInt2197))
            if (anIntArray5362!![i_779_.toInt()] > aJavaThreadResource_5367!!.anInt2197) i_782_ = 255
            else if (anIntArray5362!![i_779_.toInt()] > aJavaThreadResource_5367!!.anInt2211) i_782_ = ((aJavaThreadResource_5367!!.anInt2211 - anIntArray5362!![i_779_.toInt()]) * 255 / (aJavaThreadResource_5367!!.anInt2211 - aJavaThreadResource_5367!!.anInt2197))
            if (faceAlpha == null) rasterizer!!.anInt1674 = 0
            else rasterizer!!.anInt1674 = faceAlpha!![i].toInt() and 0xff
            if (faceTextures == null || faceTextures!![i].toInt() == -1) {
                if (anIntArray5366!![i] == -1) rasterizer!!.method1027(
                    anIntArray5343!![i_777_.toInt()].toFloat(),
                    anIntArray5343!![i_778_.toInt()].toFloat(),
                    anIntArray5343!![i_779_.toInt()].toFloat(),
                    anIntArray5321!![i_777_.toInt()].toFloat(),
                    anIntArray5321!![i_778_.toInt()].toFloat(),
                    anIntArray5321!![i_779_.toInt()].toFloat(),
                    anIntArray5355!![i_777_.toInt()].toFloat(),
                    anIntArray5355!![i_778_.toInt()].toFloat(),
                    anIntArray5355!![i_779_.toInt()].toFloat(),
                    JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB!![(anIntArray5368!![i] and 0xffff)]), (i_780_ shl 24 or (aJavaThreadResource_5367!!.anInt2192)), 255),
                    JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB!![(anIntArray5368!![i] and 0xffff)]), (i_781_ shl 24 or (aJavaThreadResource_5367!!.anInt2192)), 255),
                    JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB!![(anIntArray5368!![i] and 0xffff)]), (i_782_ shl 24 or (aJavaThreadResource_5367!!.anInt2192)), 255)
                )
                else rasterizer!!.method1027(
                    anIntArray5343!![i_777_.toInt()].toFloat(),
                    anIntArray5343!![i_778_.toInt()].toFloat(),
                    anIntArray5343!![i_779_.toInt()].toFloat(),
                    anIntArray5321!![i_777_.toInt()].toFloat(),
                    anIntArray5321!![i_778_.toInt()].toFloat(),
                    anIntArray5321!![i_779_.toInt()].toFloat(),
                    anIntArray5355!![i_777_.toInt()].toFloat(),
                    anIntArray5355!![i_778_.toInt()].toFloat(),
                    anIntArray5355!![i_779_.toInt()].toFloat(),
                    JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB!![(anIntArray5368!![i] and 0xffff)]), (i_780_ shl 24 or (aJavaThreadResource_5367!!.anInt2192)), 255),
                    JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB!![(anIntArray5337!![i] and 0xffff)]), (i_781_ shl 24 or (aJavaThreadResource_5367!!.anInt2192)), 255),
                    JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB!![(anIntArray5366!![i] and 0xffff)]), (i_782_ shl 24 or (aJavaThreadResource_5367!!.anInt2192)), 255)
                )
            } else {
                var i_775_ = -16777216
                if (faceAlpha != null) i_775_ = 255 - (faceAlpha!![i].toInt() and 0xff) shl 24
                if (anIntArray5366!![i] == -1) {
                    val i_776_ = i_775_ or (anIntArray5368!![i] and 0xffffff)
                    rasterizer!!.method1024(
                        anIntArray5343!![i_777_.toInt()].toFloat(),
                        anIntArray5343!![i_778_.toInt()].toFloat(),
                        anIntArray5343!![i_779_.toInt()].toFloat(),
                        anIntArray5321!![i_777_.toInt()].toFloat(),
                        anIntArray5321!![i_778_.toInt()].toFloat(),
                        anIntArray5321!![i_779_.toInt()].toFloat(),
                        anIntArray5355!![i_777_.toInt()].toFloat(),
                        anIntArray5355!![i_778_.toInt()].toFloat(),
                        anIntArray5355!![i_779_.toInt()].toFloat(),
                        texCoordU!![i]!![0],
                        texCoordU!![i]!![1],
                        texCoordU!![i]!![2],
                        texCoordV!![i]!![0],
                        texCoordV!![i]!![1],
                        texCoordV!![i]!![2],
                        i_776_,
                        i_776_,
                        i_776_,
                        aJavaThreadResource_5367!!.anInt2192,
                        i_780_,
                        i_781_,
                        i_782_,
                        faceTextures!![i].toInt()
                    )
                } else rasterizer!!.method1024(
                    anIntArray5343!![i_777_.toInt()].toFloat(),
                    anIntArray5343!![i_778_.toInt()].toFloat(),
                    anIntArray5343!![i_779_.toInt()].toFloat(),
                    anIntArray5321!![i_777_.toInt()].toFloat(),
                    anIntArray5321!![i_778_.toInt()].toFloat(),
                    anIntArray5321!![i_779_.toInt()].toFloat(),
                    anIntArray5355!![i_777_.toInt()].toFloat(),
                    anIntArray5355!![i_778_.toInt()].toFloat(),
                    anIntArray5355!![i_779_.toInt()].toFloat(),
                    texCoordU!![i]!![0],
                    texCoordU!![i]!![1],
                    texCoordU!![i]!![2],
                    texCoordV!![i]!![0],
                    texCoordV!![i]!![1],
                    texCoordV!![i]!![2],
                    i_775_ or (anIntArray5368!![i] and 0xffffff),
                    i_775_ or (anIntArray5337!![i] and 0xffffff),
                    i_775_ or (anIntArray5366!![i] and 0xffffff),
                    aJavaThreadResource_5367!!.anInt2192,
                    i_780_,
                    i_781_,
                    i_782_,
                    faceTextures!![i].toInt()
                )
            }
        }
    }

    constructor(var_ha_Sub1: JavaToolkit) {
        anInt5354 = 0
        faceCount = 0
        movingTextures = false
        maxVertex = 0
        transparent = false
        toolkit = var_ha_Sub1
    }

    constructor(toolkit: JavaToolkit, mesh: Mesh, functionMask: Int, ambient: Int, contrast: Int, featureMask: Int) {
        anInt5354 = 0
        faceCount = 0
        movingTextures = false
        maxVertex = 0
        transparent = false
        this.toolkit = toolkit
        this.functionMask = functionMask
        this.ambient = ambient
        this.contrast = contrast
        val source = this.toolkit.textureSource
        vertexCount = mesh.vertexCount
        maxVertex = mesh.maxVertex
        vertexX = mesh.vertexX
        vertexY = mesh.vertexY
        vertexZ = mesh.vertexZ
        faceCount = mesh.faceCount
        faceA = mesh.faceA
        faceB = mesh.faceB!!
        faceC = mesh.faceC
        facePriority = mesh.facePriority
        faceColour = mesh.faceColour
        faceAlpha = mesh.faceAlpha
        shadingType = mesh.shadingType
        emitters = mesh.emitters
        effectors = mesh.effectors
        val faceIndex = IntArray(faceCount)
        for (i_788_ in 0..<faceCount) faceIndex[i_788_] = i_788_
        val faceIds = LongArray(faceCount)
        val bool = (this.functionMask and 0x100) != 0
        for (i in 0..<faceCount) {
            val index = faceIndex[i]
            var metrics: TextureMetrics? = null
            var i_791_ = 0
            var i_792_ = 0
            var i_793_ = 0
            var i_794_ = 0
            if (mesh.billboards != null) {
                var hideFace = false
                for (j in mesh.billboards!!.indices) {
                    val billboard = mesh.billboards!![j]!!
                    if (index == billboard.face) {
                        val type = Class73.list((billboard.id))
                        if (type.aBoolean2531) hideFace = true
                        if (type.texture != -1) {
                            val textureMetrics_797_ = source!!.getMetrics((type.texture))
                            if (textureMetrics_797_!!.alphaBlendMode == 2) transparent = true
                        }
                    }
                }
                if (hideFace) faceIds[i] = 9223372036854775807L
            }
            var texture = -1
            if (mesh.faceTexture != null) {
                texture = mesh.faceTexture!![index].toInt()
                if (texture != -1) {
                    metrics = source!!.getMetrics(texture and 0xffff)
                    if ((featureMask and 0x40) == 0 || !metrics!!.disableable) {
                        i_793_ = metrics!!.effectType.toInt()
                        i_794_ = metrics.effectParam1.toInt()
                    } else texture = -1
                }
            }
            val transparentFace = (faceAlpha != null && faceAlpha!![index].toInt() != 0 || metrics != null && metrics.alphaBlendMode == 2)
            if ((bool || transparentFace) && facePriority != null) i_791_ += facePriority!![index].toInt() shl 17
            if (transparentFace) i_791_ += 65536
            i_791_ += (i_793_ and 0xff) shl 8
            i_791_ += i_794_ and 0xff
            i_792_ += (texture and 0xffff) shl 16
            i_792_ += i and 0xffff
            faceIds[i] = (i_791_.toLong() shl 32) + i_792_.toLong()
            transparent = transparent or transparentFace
        }
        sort(faceIndex, faceIds)
        if (mesh.billboards != null) {
            billboardCount = mesh.billboards!!.size
            billboardFaces = arrayOfNulls<JavaBillboardFace>(billboardCount)
            billboardAttributes = arrayOfNulls<JavaBillboardAttributes>(billboardCount)
            for (i_800_ in mesh.billboards!!.indices) {
                val billboard = mesh.billboards!![i_800_]!!
                val type = Class73.list(billboard.id)
                var i_801_: Int = ((ItemSpriteCacheKey.HSV_TO_RGB!![(mesh.faceColour!![billboard.face]).toInt() and 0xffff]) and 0xffffff)
                i_801_ = (i_801_ or (255 - (if (mesh.faceAlpha != null) (mesh.faceAlpha!![billboard.face]).toInt() and 0xff else 0) shl 24))
                billboardFaces!![i_800_] = JavaBillboardFace(billboard.face, (mesh.faceA!![billboard.face]).toInt(), (mesh.faceB!![billboard.face]).toInt(), (mesh.faceC!![billboard.face]).toInt(), type.anInt2526, type.anInt2530, type.texture, type.anInt2533, type.anInt2534, type.aBoolean2531, billboard.anInt2158)
                billboardAttributes!![i_800_] = JavaBillboardAttributes(i_801_)
            }
        }
        texCoordU = arrayOfNulls<FloatArray>(faceCount)
        texCoordV = arrayOfNulls<FloatArray>(faceCount)
        val universe = Class59_Sub2_Sub1.fromMesh(faceCount, mesh, faceIndex)
        val javaThreadResource = this.toolkit.threadResource(Thread.currentThread())
        val fs = javaThreadResource!!.aFloatArray2226
        var bool_802_ = false
        for (i_803_ in 0..<faceCount) {
            val i_804_ = faceIndex[i_803_]
            var i_805_: Int
            if (mesh.faceTexSpace == null) i_805_ = -1
            else i_805_ = mesh.faceTexSpace!![i_804_].toInt()
            var i_806_ = (if (mesh.faceTexture == null) -1 else mesh.faceTexture!![i_804_]).toInt()
            if (i_806_ != -1 && (featureMask and 0x40) != 0) {
                val metrics = source!!.getMetrics(i_806_ and 0xffff)
                if (metrics!!.disableable) i_806_ = -1
            }
            if (i_806_ != -1) {
                bool_802_ = true
                texCoordU!![i_804_] = FloatArray(3)
                val fs_807_ = texCoordU!![i_804_]
                texCoordV!![i_804_] = FloatArray(3)
                val fs_808_ = texCoordV!![i_804_]
                if (i_805_ == -1) {
                    fs_807_!![0] = 0.0f
                    fs_808_!![0] = 1.0f
                    fs_807_[1] = 1.0f
                    fs_808_[1] = 1.0f
                    fs_807_[2] = 0.0f
                    fs_808_[2] = 0.0f
                } else {
                    i_805_ = i_805_ and 0xff
                    val mappingType = mesh.texMappingType!![i_805_]
                    if (mappingType.toInt() == 0) {
                        val i_811_ = faceA!![i_804_]
                        val i_812_ = faceB!![i_804_]
                        val i_813_ = faceC!![i_804_]
                        val i_814_ = mesh.texSpaceDefA!![i_805_]
                        val i_815_ = mesh.texSpaceDefB!![i_805_]
                        val i_816_ = mesh.texSpaceDefC!![i_805_]
                        val f = vertexX!![i_814_.toInt()].toFloat()
                        val f_817_ = vertexY!![i_814_.toInt()].toFloat()
                        val f_818_ = vertexZ!![i_814_.toInt()].toFloat()
                        val f_819_ = vertexX!![i_815_.toInt()].toFloat() - f
                        val f_820_ = vertexY!![i_815_.toInt()].toFloat() - f_817_
                        val f_821_ = vertexZ!![i_815_.toInt()].toFloat() - f_818_
                        val f_822_ = vertexX!![i_816_.toInt()].toFloat() - f
                        val f_823_ = vertexY!![i_816_.toInt()].toFloat() - f_817_
                        val f_824_ = vertexZ!![i_816_.toInt()].toFloat() - f_818_
                        val f_825_ = vertexX!![i_811_.toInt()].toFloat() - f
                        val f_826_ = vertexY!![i_811_.toInt()].toFloat() - f_817_
                        val f_827_ = vertexZ!![i_811_.toInt()].toFloat() - f_818_
                        val f_828_ = vertexX!![i_812_.toInt()].toFloat() - f
                        val f_829_ = vertexY!![i_812_.toInt()].toFloat() - f_817_
                        val f_830_ = vertexZ!![i_812_.toInt()].toFloat() - f_818_
                        val f_831_ = vertexX!![i_813_.toInt()].toFloat() - f
                        val f_832_ = vertexY!![i_813_.toInt()].toFloat() - f_817_
                        val f_833_ = vertexZ!![i_813_.toInt()].toFloat() - f_818_
                        val f_834_ = f_820_ * f_824_ - f_821_ * f_823_
                        val f_835_ = f_821_ * f_822_ - f_819_ * f_824_
                        val f_836_ = f_819_ * f_823_ - f_820_ * f_822_
                        var f_837_ = f_823_ * f_836_ - f_824_ * f_835_
                        var f_838_ = f_824_ * f_834_ - f_822_ * f_836_
                        var f_839_ = f_822_ * f_835_ - f_823_ * f_834_
                        var f_840_ = 1.0f / (f_837_ * f_819_ + f_838_ * f_820_ + f_839_ * f_821_)
                        fs_807_!![0] = (f_837_ * f_825_ + f_838_ * f_826_ + f_839_ * f_827_) * f_840_
                        fs_807_[1] = (f_837_ * f_828_ + f_838_ * f_829_ + f_839_ * f_830_) * f_840_
                        fs_807_[2] = (f_837_ * f_831_ + f_838_ * f_832_ + f_839_ * f_833_) * f_840_
                        f_837_ = f_820_ * f_836_ - f_821_ * f_835_
                        f_838_ = f_821_ * f_834_ - f_819_ * f_836_
                        f_839_ = f_819_ * f_835_ - f_820_ * f_834_
                        f_840_ = 1.0f / (f_837_ * f_822_ + f_838_ * f_823_ + f_839_ * f_824_)
                        fs_808_!![0] = (f_837_ * f_825_ + f_838_ * f_826_ + f_839_ * f_827_) * f_840_
                        fs_808_[1] = (f_837_ * f_828_ + f_838_ * f_829_ + f_839_ * f_830_) * f_840_
                        fs_808_[2] = (f_837_ * f_831_ + f_838_ * f_832_ + f_839_ * f_833_) * f_840_
                    } else {
                        val i_841_ = faceA!![i_804_]
                        val i_842_ = faceB!![i_804_]
                        val i_843_ = faceC!![i_804_]
                        val i_844_ = universe.originX!![i_805_]
                        val i_845_ = universe.originY!![i_805_]
                        val i_846_ = universe.originZ!![i_805_]
                        val fs_847_ = (universe.matrices!![i_805_])
                        val direction = mesh.texDirection!![i_805_]
                        val f = ((mesh.texOffsetX!![i_805_]).toFloat() / 256.0f)
                        if (mappingType.toInt() == 1) {
                            val f_849_ = ((mesh.texSpaceScaleZ!![i_805_]).toFloat() / 1024.0f)
                            Class246.cylinderMap(i_846_, vertexZ!![i_841_.toInt()], direction.toInt(), vertexX!![i_841_.toInt()], fs, vertexY!![i_841_.toInt()], f, i_845_, i_844_, f_849_, fs_847_)
                            fs_807_!![0] = fs[0]
                            fs_808_!![0] = fs[1]
                            Class246.cylinderMap(i_846_, vertexZ!![i_842_.toInt()], direction.toInt(), vertexX!![i_842_.toInt()], fs, vertexY!![i_842_.toInt()], f, i_845_, i_844_, f_849_, fs_847_)
                            fs_807_[1] = fs[0]
                            fs_808_[1] = fs[1]
                            Class246.cylinderMap(i_846_, vertexZ!![i_843_.toInt()], direction.toInt(), vertexX!![i_843_.toInt()], fs, vertexY!![i_843_.toInt()], f, i_845_, i_844_, f_849_, fs_847_)
                            fs_807_[2] = fs[0]
                            fs_808_[2] = fs[1]
                            val f_850_ = f_849_ / 2.0f
                            if ((direction.toInt() and 0x1) == 0) {
                                if (fs_807_[1] - fs_807_[0] > f_850_) fs_807_[1] -= f_849_
                                else if (fs_807_[0] - fs_807_[1] > f_850_) fs_807_[1] += f_849_
                                if (fs_807_[2] - fs_807_[0] > f_850_) fs_807_[2] -= f_849_
                                else if (fs_807_[0] - fs_807_[2] > f_850_) fs_807_[2] += f_849_
                            } else {
                                if (fs_808_[1] - fs_808_[0] > f_850_) fs_808_[1] -= f_849_
                                else if (fs_808_[0] - fs_808_[1] > f_850_) fs_808_[1] += f_849_
                                if (fs_808_[2] - fs_808_[0] > f_850_) fs_808_[2] -= f_849_
                                else if (fs_808_[0] - fs_808_[2] > f_850_) fs_808_[2] += f_849_
                            }
                        } else if (mappingType.toInt() == 2) {
                            val f_851_ = ((mesh.texOffsetY!![i_805_]).toFloat() / 256.0f)
                            val f_852_ = ((mesh.texOffsetZ!![i_805_]).toFloat() / 256.0f)
                            val i_853_ = (vertexX!![i_842_.toInt()] - vertexX!![i_841_.toInt()])
                            val i_854_ = (vertexY!![i_842_.toInt()] - vertexY!![i_841_.toInt()])
                            val i_855_ = (vertexZ!![i_842_.toInt()] - vertexZ!![i_841_.toInt()])
                            val i_856_ = (vertexX!![i_843_.toInt()] - vertexX!![i_841_.toInt()])
                            val i_857_ = (vertexY!![i_843_.toInt()] - vertexY!![i_841_.toInt()])
                            val i_858_ = (vertexZ!![i_843_.toInt()] - vertexZ!![i_841_.toInt()])
                            val i_859_ = i_854_ * i_858_ - i_857_ * i_855_
                            val i_860_ = i_855_ * i_856_ - i_858_ * i_853_
                            val i_861_ = i_853_ * i_857_ - i_856_ * i_854_
                            val f_862_ = 64.0f / (mesh.texSpaceScaleX!![i_805_]).toFloat()
                            val f_863_ = 64.0f / (mesh.texSpaceScaleY!![i_805_]).toFloat()
                            val f_864_ = 64.0f / (mesh.texSpaceScaleZ!![i_805_]).toFloat()
                            val f_865_ = ((i_859_.toFloat() * fs_847_!![0] + i_860_.toFloat() * fs_847_[1] + i_861_.toFloat() * fs_847_[2]) / f_862_)
                            val f_866_ = ((i_859_.toFloat() * fs_847_[3] + i_860_.toFloat() * fs_847_[4] + i_861_.toFloat() * fs_847_[5]) / f_863_)
                            val f_867_ = ((i_859_.toFloat() * fs_847_[6] + i_860_.toFloat() * fs_847_[7] + i_861_.toFloat() * fs_847_[8]) / f_864_)
                            val i_868_ = Class331.method2635(f_866_, f_867_, f_865_)
                            Class262.cubeMap(f_852_, f, fs_847_, vertexZ!![i_841_.toInt()], i_846_, direction.toInt(), i_844_, vertexX!![i_841_.toInt()], vertexY!![i_841_.toInt()], f_851_, fs, i_845_, i_868_)
                            fs_807_!![0] = fs[0]
                            fs_808_!![0] = fs[1]
                            Class262.cubeMap(f_852_, f, fs_847_, vertexZ!![i_842_.toInt()], i_846_, direction.toInt(), i_844_, vertexX!![i_842_.toInt()], vertexY!![i_842_.toInt()], f_851_, fs, i_845_, i_868_)
                            fs_807_[1] = fs[0]
                            fs_808_[1] = fs[1]
                            Class262.cubeMap(f_852_, f, fs_847_, vertexZ!![i_843_.toInt()], i_846_, direction.toInt(), i_844_, vertexX!![i_843_.toInt()], vertexY!![i_843_.toInt()], f_851_, fs, i_845_, i_868_)
                            fs_807_[2] = fs[0]
                            fs_808_[2] = fs[1]
                        } else if (mappingType.toInt() == 3) {
                            Class181.sphereMap(i_846_, direction.toInt(), f, vertexX!![i_841_.toInt()], fs, vertexZ!![i_841_.toInt()], i_844_, vertexY!![i_841_.toInt()], i_845_, fs_847_)
                            fs_807_!![0] = fs[0]
                            fs_808_!![0] = fs[1]
                            Class181.sphereMap(i_846_, direction.toInt(), f, vertexX!![i_842_.toInt()], fs, vertexZ!![i_842_.toInt()], i_844_, vertexY!![i_842_.toInt()], i_845_, fs_847_)
                            fs_807_[1] = fs[0]
                            fs_808_[1] = fs[1]
                            Class181.sphereMap(i_846_, direction.toInt(), f, vertexX!![i_843_.toInt()], fs, vertexZ!![i_843_.toInt()], i_844_, vertexY!![i_843_.toInt()], i_845_, fs_847_)
                            fs_807_[2] = fs[0]
                            fs_808_[2] = fs[1]
                            if ((direction.toInt() and 0x1) == 0) {
                                if (fs_807_[1] - fs_807_[0] > 0.5f) fs_807_[1]--
                                else if (fs_807_[0] - fs_807_[1] > 0.5f) fs_807_[1]++
                                if (fs_807_[2] - fs_807_[0] > 0.5f) fs_807_[2]--
                                else if (fs_807_[0] - fs_807_[2] > 0.5f) fs_807_[2]++
                            } else {
                                if (fs_808_[1] - fs_808_[0] > 0.5f) fs_808_[1]--
                                else if (fs_808_[0] - fs_808_[1] > 0.5f) fs_808_[1]++
                                if (fs_808_[2] - fs_808_[0] > 0.5f) fs_808_[2]--
                                else if (fs_808_[0] - fs_808_[2] > 0.5f) fs_808_[2]++
                            }
                        }
                    }
                }
            }
        }
        if (!bool_802_) {
            texCoordV = null
            texCoordU = texCoordV
        }
        if (mesh.vertexLabel != null && (this.functionMask and 0x20) != 0) vertexLabels = mesh.getVertexLabels()
        if (mesh.faceLabel != null && (this.functionMask and 0x180) != 0) faceLabels = mesh.getFaceLabels()
        if (mesh.billboards != null && (this.functionMask and 0x400) != 0) billboardLabels = mesh.getBillboardGroups()
        if (mesh.faceTexture != null) {
            faceTextures = ShortArray(faceCount)
            var hasTextures = false
            for (i_870_ in 0..<faceCount) {
                val i_871_ = mesh.faceTexture!![i_870_]
                if (i_871_.toInt() != -1) {
                    val metrics = this.toolkit.textureSource!!.getMetrics(i_871_.toInt())
                    if ((featureMask and 0x40) == 0 || !metrics!!.disableable) {
                        faceTextures!![i_870_] = i_871_
                        hasTextures = true
                        if (metrics!!.alphaBlendMode == 2) transparent = true
                        if (metrics.speedU.toInt() != 0 || metrics.speedV.toInt() != 0) movingTextures = true
                    } else faceTextures!![i_870_] = (-1).toShort()
                } else faceTextures!![i_870_] = (-1).toShort()
            }
            if (!hasTextures) faceTextures = null
        } else faceTextures = null
        if (transparent || billboardFaces != null) {
            faceIndices = ShortArray(faceCount)
            for (i_872_ in 0..<faceCount) faceIndices!![i_872_] = faceIndex[i_872_].toShort()
        }
    }

    companion object {

        fun method303(i: Int): Short {
            val i_4_ = (i and 0xfe66) shr 10
            var i_5_ = i shr 3 and 0x70
            val i_6_ = i and 0x7f
            i_5_ = (if (i_6_ <= 64) i_6_ * i_5_ shr 7 else i_5_ * (127 + -i_6_) shr 7)
            val i_7_ = i_5_ + i_6_
            val i_8_: Int
            if (i_7_ != 0) i_8_ = (i_5_ shl 8) / i_7_
            else i_8_ = i_5_ shl 1
            val i_9_ = i_7_
            return (i_9_ or (i_8_ shr 4 shl 7 or (i_4_ shl 10))).toShort()
        }


        fun sort(`is`: IntArray?, ls: LongArray?) {
            IOException_Sub1.method129(0, ls, ls!!.size - 1, `is`)
        }

        var anInt5346: Int = 4096
        var anInt5350: Int = 4096
        fun method2198(i_0_: Int, i_1_: Int): Int {
            var i_0_ = i_0_
            i_0_ = i_0_ * (i_1_ and 0x7f) shr 7
            if (i_0_ >= 2) {
                if (i_0_ > 126) i_0_ = 126
            } else i_0_ = 2
            return (0xff80 and i_1_) - -i_0_
        }
    }
}
