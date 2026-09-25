package world.gregs.voidps.tools.icon

import java.awt.Canvas
import kotlin.math.floor
import kotlin.math.sqrt

/* ha_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class JavaToolkit private constructor(var_textureSource: TextureSource?) : Toolkit(var_textureSource) {
    private var aIterableHashTable_7467: IterableHashTable?
    private var aCanvas7468: Canvas? = null
    private var aBoolean7470 = false
    private var aBoolean7471 = false
    var anInt7473: Int = 0
    var anInt7474: Int
    var anInt7476: Int
    var anInt7477: Int = 0
    var anInt7478: Int
    var anInt7479: Int = 0
    private var aJavaThreadResourceArray7480: Array<JavaThreadResource?>? = null
    var anInt7482: Int
    var anIntArray7483: IntArray? = null
    var anInt7484: Int = 0
    var anInt7485: Int = 0
    private var anInt7486 = 0
    private var anInt7488 = 0
    private val aBoolean7489: Boolean
    var anInt7490: Int = 0
    var anInt7491: Int
    var aClass101_Sub1_7492: Matrix_Sub1? = null
    var anInt7494: Int
    private var anInt7495 = 0
    var anInt7496: Int
    var anInt7497: Int
    private val aClass60_7498: Class60
    private val aClass60_7499: Class60
    var anInt7500: Int
    var anInt7501: Int
    var anInt7503: Int
    var anInt7504: Int = 0
    var anInt7506: Int = 0
    var anInt7507: Int
    var anInt7508: Int = 0
    var anInt7509: Int = 0
    var anInt7510: Int = 0
    var aFloatArray7511: FloatArray? = null
    private var anInt7512: Int
    private var aSprite_7513: Sprite? = null

    override fun na(i: Int, i_0_: Int, i_1_: Int, i_2_: Int): IntArray {
        val `is` = IntArray(i_1_ * i_2_)
        var i_3_ = 0
        for (i_4_ in 0..<i_2_) {
            val i_5_ = (i_0_ + i_4_) * this.anInt7477 + i
            for (i_6_ in 0..<i_1_) `is`[i_3_++] = this.anIntArray7483!![i_5_ + i_6_]
        }
        return `is`
    }

    // dependency of Class109's genuine drawing methods (method1018/method1024
    // call method3645 unconditionally); not itself in the genuine list.
    fun line(i: Int, i_41_: Int, i_42_: Int, i_44_: Int, i_45_: Int) {
        method3709(i_41_, i, i_42_, i_45_, i_44_, 1)
    }

    // dependency of method3645
    fun method3709(i: Int, i_241_: Int, i_242_: Int, i_243_: Int, i_244_: Int, i_245_: Int) {
        var i = i
        var i_241_ = i_241_
        var i_242_ = i_242_
        var i_243_ = i_243_
        var i_244_ = i_244_
        i_242_ -= i
        i_243_ -= i_241_
        if (i_243_ == 0) {
            if (i_242_ >= 0) U(i, i_241_, i_242_ + 1, i_244_, i_245_)
            else U(i + i_242_, i_241_, -i_242_ + 1, i_244_, i_245_)
        } else if (i_242_ == 0) {
            if (i_243_ >= 0) P(i, i_241_, i_243_ + 1, i_244_, i_245_)
            else P(i, i_241_ + i_243_, -i_243_ + 1, i_244_, i_245_)
        } else {
            if (i_242_ + i_243_ < 0) {
                i += i_242_
                i_242_ = -i_242_
                i_241_ += i_243_
                i_243_ = -i_243_
            }
            if (i_242_ > i_243_) {
                i_241_ = i_241_ shl 16
                i_241_ += 32768
                i_243_ = i_243_ shl 16
                val i_246_ = floor(i_243_.toDouble() / i_242_.toDouble() + 0.5).toInt()
                i_242_ += i
                if (i < this.anInt7496) {
                    i_241_ += i_246_ * (this.anInt7496 - i)
                    i = this.anInt7496
                }
                if (i_242_ >= this.anInt7507) i_242_ = this.anInt7507 - 1
                val i_247_ = i_244_ ushr 24
                if (i_245_ == 0 || i_245_ == 1 && i_247_ == 255) {
                    while ( /**/i <= i_242_) {
                        val i_248_ = i_241_ shr 16
                        if (i_248_ >= this.anInt7476 && i_248_ < this.anInt7503) this.anIntArray7483!![i + i_248_ * this.anInt7477] = i_244_
                        i_241_ += i_246_
                        i++
                    }
                    return
                }
                if (i_245_ == 1) {
                    i_244_ = (((i_244_ and 0xff00ff) * i_247_ shr 8 and 0xff00ff) + ((i_244_ and 0xff00) * i_247_ shr 8 and 0xff00) + (i_247_ shl 24))
                    val i_249_ = 256 - i_247_
                    while ( /**/i <= i_242_) {
                        val i_250_ = i_241_ shr 16
                        if (i_250_ >= this.anInt7476 && i_250_ < this.anInt7503) {
                            val i_251_ = i + i_250_ * this.anInt7477
                            var i_252_ = this.anIntArray7483!![i_251_]
                            i_252_ = (((i_252_ and 0xff00ff) * i_249_ shr 8 and 0xff00ff) + ((i_252_ and 0xff00) * i_249_ shr 8 and 0xff00))
                            this.anIntArray7483!![i_251_] = i_244_ + i_252_
                        }
                        i_241_ += i_246_
                        i++
                    }
                    return
                }
                if (i_245_ == 2) {
                    while ( /**/i <= i_242_) {
                        val i_253_ = i_241_ shr 16
                        if (i_253_ >= this.anInt7476 && i_253_ < this.anInt7503) {
                            val i_254_ = i + i_253_ * this.anInt7477
                            var i_255_ = this.anIntArray7483!![i_254_]
                            val i_256_ = i_244_ + i_255_
                            val i_257_ = (i_244_ and 0xff00ff) + (i_255_ and 0xff00ff)
                            i_255_ = (i_257_ and 0x1000100) + (i_256_ - i_257_ and 0x10000)
                            this.anIntArray7483!![i_254_] = i_256_ - i_255_ or i_255_ - (i_255_ ushr 8)
                        }
                        i_241_ += i_246_
                        i++
                    }
                    return
                }
                throw IllegalArgumentException()
            }
            i = i shl 16
            i += 32768
            i_242_ = i_242_ shl 16
            val i_258_ = floor(i_242_.toDouble() / i_243_.toDouble() + 0.5).toInt()
            i_243_ += i_241_
            if (i_241_ < this.anInt7476) {
                i += i_258_ * (this.anInt7476 - i_241_)
                i_241_ = this.anInt7476
            }
            if (i_243_ >= this.anInt7503) i_243_ = this.anInt7503 - 1
            val i_259_ = i_244_ ushr 24
            if (i_245_ == 0 || i_245_ == 1 && i_259_ == 255) {
                while ( /**/i_241_ <= i_243_) {
                    val i_260_ = i shr 16
                    if (i_260_ >= this.anInt7496 && i_260_ < this.anInt7507) this.anIntArray7483!![i_260_ + i_241_ * this.anInt7477] = i_244_
                    i += i_258_
                    i_241_++
                }
            } else if (i_245_ == 1) {
                i_244_ = (((i_244_ and 0xff00ff) * i_259_ shr 8 and 0xff00ff) + ((i_244_ and 0xff00) * i_259_ shr 8 and 0xff00) + (i_259_ shl 24))
                val i_261_ = 256 - i_259_
                while ( /**/i_241_ <= i_243_) {
                    val i_262_ = i shr 16
                    if (i_262_ >= this.anInt7496 && i_262_ < this.anInt7507) {
                        val i_263_ = i_262_ + i_241_ * this.anInt7477
                        var i_264_ = this.anIntArray7483!![i_263_]
                        i_264_ = (((i_264_ and 0xff00ff) * i_261_ shr 8 and 0xff00ff) + ((i_264_ and 0xff00) * i_261_ shr 8 and 0xff00))
                        this.anIntArray7483!![i_262_ + i_241_ * this.anInt7477] = i_244_ + i_264_
                    }
                    i += i_258_
                    i_241_++
                }
            } else if (i_245_ == 2) {
                while ( /**/i_241_ <= i_243_) {
                    val i_265_ = i shr 16
                    if (i_265_ >= this.anInt7496 && i_265_ < this.anInt7507) {
                        val i_266_ = i_265_ + i_241_ * this.anInt7477
                        var i_267_ = this.anIntArray7483!![i_266_]
                        val i_268_ = i_244_ + i_267_
                        val i_269_ = (i_244_ and 0xff00ff) + (i_267_ and 0xff00ff)
                        i_267_ = (i_269_ and 0x1000100) + (i_268_ - i_269_ and 0x10000)
                        this.anIntArray7483!![i_266_] = i_268_ - i_267_ or i_267_ - (i_267_ ushr 8)
                    }
                    i += i_258_
                    i_241_++
                }
            } else throw IllegalArgumentException()
        }
    }

    // dependency of method3709
    fun U(i: Int, i_186_: Int, i_187_: Int, i_188_: Int, i_189_: Int) {
        var i = i
        var i_187_ = i_187_
        var i_188_ = i_188_
        if (i_186_ >= this.anInt7476 && i_186_ < this.anInt7503) {
            if (i < this.anInt7496) {
                i_187_ -= this.anInt7496 - i
                i = this.anInt7496
            }
            if (i + i_187_ > this.anInt7507) i_187_ = this.anInt7507 - i
            val i_190_ = i + i_186_ * this.anInt7477
            val i_191_ = i_188_ ushr 24
            if (i_189_ == 0 || i_189_ == 1 && i_191_ == 255) {
                for (i_192_ in 0..<i_187_) this.anIntArray7483!![i_190_ + i_192_] = i_188_
            } else if (i_189_ == 1) {
                i_188_ = (((i_188_ and 0xff00ff) * i_191_ shr 8 and 0xff00ff) + ((i_188_ and 0xff00) * i_191_ shr 8 and 0xff00) + (i_191_ shl 24))
                val i_193_ = 256 - i_191_
                for (i_194_ in 0..<i_187_) {
                    var i_195_ = this.anIntArray7483!![i_190_ + i_194_]
                    i_195_ = (((i_195_ and 0xff00ff) * i_193_ shr 8 and 0xff00ff) + ((i_195_ and 0xff00) * i_193_ shr 8 and 0xff00))
                    this.anIntArray7483!![i_190_ + i_194_] = i_188_ + i_195_
                }
            } else if (i_189_ == 2) {
                for (i_196_ in 0..<i_187_) {
                    var i_197_ = this.anIntArray7483!![i_190_ + i_196_]
                    val i_198_ = i_188_ + i_197_
                    val i_199_ = (i_188_ and 0xff00ff) + (i_197_ and 0xff00ff)
                    i_197_ = (i_199_ and 0x1000100) + (i_198_ - i_199_ and 0x10000)
                    this.anIntArray7483!![i_190_ + i_196_] = i_198_ - i_197_ or i_197_ - (i_197_ ushr 8)
                }
            } else throw IllegalArgumentException()
        }
    }

    // dependency of method3709
    fun P(i: Int, i_71_: Int, i_72_: Int, i_73_: Int, i_74_: Int) {
        var i_71_ = i_71_
        var i_72_ = i_72_
        var i_73_ = i_73_
        if (i >= this.anInt7496 && i < this.anInt7507) {
            if (i_71_ < this.anInt7476) {
                i_72_ -= this.anInt7476 - i_71_
                i_71_ = this.anInt7476
            }
            if (i_71_ + i_72_ > this.anInt7503) i_72_ = this.anInt7503 - i_71_
            val i_75_ = i + i_71_ * this.anInt7477
            val i_76_ = i_73_ ushr 24
            if (i_74_ == 0 || i_74_ == 1 && i_76_ == 255) {
                for (i_77_ in 0..<i_72_) this.anIntArray7483!![i_75_ + i_77_ * this.anInt7477] = i_73_
            } else if (i_74_ == 1) {
                i_73_ = (((i_73_ and 0xff00ff) * i_76_ shr 8 and 0xff00ff) + ((i_73_ and 0xff00) * i_76_ shr 8 and 0xff00) + (i_76_ shl 24))
                val i_78_ = 256 - i_76_
                for (i_79_ in 0..<i_72_) {
                    val i_80_ = i_75_ + i_79_ * this.anInt7477
                    var i_81_ = this.anIntArray7483!![i_80_]
                    i_81_ = (((i_81_ and 0xff00ff) * i_78_ shr 8 and 0xff00ff) + ((i_81_ and 0xff00) * i_78_ shr 8 and 0xff00))
                    this.anIntArray7483!![i_80_] = i_73_ + i_81_
                }
            } else if (i_74_ == 2) {
                for (i_82_ in 0..<i_72_) {
                    val i_83_ = i_75_ + i_82_ * this.anInt7477
                    var i_84_ = this.anIntArray7483!![i_83_]
                    val i_85_ = i_73_ + i_84_
                    val i_86_ = (i_73_ and 0xff00ff) + (i_84_ and 0xff00ff)
                    i_84_ = (i_86_ and 0x1000100) + (i_85_ - i_86_ and 0x10000)
                    this.anIntArray7483!![i_83_] = i_85_ - i_84_ or i_84_ - (i_84_ ushr 8)
                }
            } else throw IllegalArgumentException()
        }
    }

    override fun xa(f: Float) {
        this.anInt7500 = (f * 65535.0f).toInt()
    }

    // explicitly listed as genuine
    private fun method3713() {
        this.anInt7509 = this.anInt7496 - this.anInt7510
        this.anInt7508 = this.anInt7507 - this.anInt7510
        this.anInt7490 = this.anInt7476 - this.anInt7504
        this.anInt7506 = this.anInt7503 - this.anInt7504
        for (i in 0..<this.anInt7485) {
            val rasterizer = aJavaThreadResourceArray7480!![i]!!.rasterizer
            rasterizer!!.anInt1665 = this.anInt7510 - this.anInt7496
            rasterizer.anInt1668 = this.anInt7504 - this.anInt7476
            rasterizer.width = this.anInt7507 - this.anInt7496
            rasterizer.height = this.anInt7503 - this.anInt7476
        }
        var i = (this.anInt7476 * this.anInt7477 + this.anInt7496)
        for (i_97_ in this.anInt7476..<this.anInt7503) {
            for (i_98_ in 0..<this.anInt7485) aJavaThreadResourceArray7480!![i_98_]!!.rasterizer!!.lineOffsets[i_97_ - this.anInt7476] = i
            i += this.anInt7477
        }
    }

    override fun i(): Int {
        return this.anInt7482
    }

    override fun method3705(): Matrix? {
        val javaThreadResource = threadResource(Thread.currentThread())
        return javaThreadResource!!.aClass101_Sub1_2209
    }

    fun method3714(i: Int): Boolean {
        return this.textureSource!!.getMetrics(i)!!.aBoolean217 || this.textureSource!!.getMetrics(i)!!.aBoolean215
    }

    override fun setCamera(matrix: Matrix?) {
        this.aClass101_Sub1_7492 = matrix as Matrix_Sub1
    }

    override fun f(i: Int, i_156_: Int) {
        val javaThreadResource = threadResource(Thread.currentThread())
        this.anInt7482 = i
        this.anInt7494 = i_156_
        javaThreadResource!!.anInt2210 = this.anInt7494 - 255
    }

    override fun ya() {
        if (this.anInt7496 == 0 && this.anInt7507 == this.anInt7477 && this.anInt7476 == 0 && this.anInt7503 == anInt7486) {
            val i = this.aFloatArray7511!!.size
            val i_176_ = i - (i and 0x7)
            var i_177_ = 0
            while (i_177_ < i_176_) {
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
                this.aFloatArray7511!![i_177_++] = 2.14748365E9f
            }
            while (i_177_ < i) this.aFloatArray7511!![i_177_++] = 2.14748365E9f
        } else {
            var i = this.anInt7507 - this.anInt7496
            val i_178_ = this.anInt7503 - this.anInt7476
            val i_179_ = this.anInt7477 - i
            val i_180_ = (this.anInt7496 + this.anInt7476 * this.anInt7477)
            val i_181_ = i shr 3
            val i_182_ = i and 0x7
            i = i_180_ - 1
            for (i_183_ in -i_178_..-1) {
                if (i_181_ > 0) {
                    var i_184_ = i_181_
                    do {
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                        this.aFloatArray7511!![++i] = 2.14748365E9f
                    } while (--i_184_ > 0)
                }
                if (i_182_ > 0) {
                    var i_185_ = i_182_
                    do this.aFloatArray7511!![++i] = 2.14748365E9f while (--i_185_ > 0)
                }
                i += i_179_
            }
        }
    }

    fun method3716(): Boolean {
        return aBoolean7470
    }

    private fun method3717() {
        for (i in 0..<this.anInt7485) aJavaThreadResourceArray7480!![i]!!.method1292()
        la()
    }

    override fun DA(i: Int, i_223_: Int, i_224_: Int, i_225_: Int) {
        this.anInt7510 = i
        this.anInt7504 = i_223_
        this.anInt7491 = i_224_
        this.anInt7497 = i_225_
        method3713()
    }

    fun method3643(canvas: Canvas?, i: Int, i_232_: Int) {
        var class348_sub31 = (aIterableHashTable_7467!!.method3480(canvas.hashCode().toLong()) as Class348_Sub31?)
        if (class348_sub31 == null) {
            class348_sub31 = method1035(i_232_, canvas, i)
            aIterableHashTable_7467!!.put(canvas.hashCode().toLong(), class348_sub31)
        } else if (class348_sub31.anInt6917 != i || class348_sub31.anInt6920 != i_232_) method3669(canvas, i, i_232_)
    }

    fun method3631(i: Int) {
        this.anInt7485 = i
        aJavaThreadResourceArray7480 = arrayOfNulls<JavaThreadResource>(this.anInt7485)
        for (i_240_ in 0..<this.anInt7485) aJavaThreadResourceArray7480!![i_240_] = JavaThreadResource(this)
    }

    // dependency of method3643 (not in genuine list, required to compile)
    fun method3669(canvas: Canvas?, i: Int, i_578_: Int) {
        var class348_sub31 = (aIterableHashTable_7467!!.method3480(canvas.hashCode().toLong()) as Class348_Sub31?)
        if (class348_sub31 != null) {
            class348_sub31.unlink()
            class348_sub31 = method1035(i_578_, canvas, i)
            aIterableHashTable_7467!!.put(canvas.hashCode().toLong(), class348_sub31)
            if (aCanvas7468 === canvas) {
                this.anIntArray7483 = class348_sub31!!.anIntArray6916
                this.anInt7477 = class348_sub31.anInt6917
                anInt7486 = class348_sub31.anInt6920
                if (this.anInt7477 != anInt7495 || anInt7486 != anInt7488) {
                    anInt7495 = this.anInt7477
                    anInt7488 = anInt7486
                    this.aFloatArray7511 = FloatArray(anInt7495 * anInt7488)
                }
                method3717()
            }
        }
    }

    override fun aa(i: Int, i_334_: Int, i_335_: Int, i_336_: Int, i_337_: Int, i_338_: Int) {
        var i = i
        var i_334_ = i_334_
        var i_335_ = i_335_
        var i_336_ = i_336_
        var i_337_ = i_337_
        if (i < this.anInt7496) {
            i_335_ -= this.anInt7496 - i
            i = this.anInt7496
        }
        if (i_334_ < this.anInt7476) {
            i_336_ -= this.anInt7476 - i_334_
            i_334_ = this.anInt7476
        }
        if (i + i_335_ > this.anInt7507) i_335_ = this.anInt7507 - i
        if (i_334_ + i_336_ > this.anInt7503) i_336_ = this.anInt7503 - i_334_
        if (i_335_ > 0 && i_336_ > 0 && i <= this.anInt7507 && i_334_ <= this.anInt7503) {
            val i_339_ = this.anInt7477 - i_335_
            var i_340_ = i + i_334_ * this.anInt7477
            val i_341_ = i_337_ ushr 24
            if (i_338_ == 0 || i_338_ == 1 && i_341_ == 255) {
                val i_342_ = i_335_ shr 3
                val i_343_ = i_335_ and 0x7
                i_335_ = i_340_ - 1
                for (i_344_ in -i_336_..-1) {
                    if (i_342_ > 0) {
                        i = i_342_
                        do {
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                            this.anIntArray7483!![++i_335_] = i_337_
                        } while (--i > 0)
                    }
                    if (i_343_ > 0) {
                        i = i_343_
                        do this.anIntArray7483!![++i_335_] = i_337_ while (--i > 0)
                    }
                    i_335_ += i_339_
                }
            } else if (i_338_ == 1) {
                i_337_ = (((i_337_ and 0xff00ff) * i_341_ shr 8 and 0xff00ff) + (((i_337_ and 0xff00ff.inv()) ushr 8) * i_341_ and 0xff00ff.inv()))
                val i_345_ = 256 - i_341_
                for (i_346_ in 0..<i_336_) {
                    for (i_347_ in -i_335_..-1) {
                        var i_348_ = this.anIntArray7483!![i_340_]
                        i_348_ = (((i_348_ and 0xff00ff) * i_345_ shr 8 and 0xff00ff) + (((i_348_ and 0xff00ff.inv()) ushr 8) * i_345_ and 0xff00ff.inv()))
                        this.anIntArray7483!![i_340_++] = i_337_ + i_348_
                    }
                    i_340_ += i_339_
                }
            } else if (i_338_ == 2) {
                for (i_349_ in 0..<i_336_) {
                    for (i_350_ in -i_335_..-1) {
                        var i_351_ = this.anIntArray7483!![i_340_]
                        val i_352_ = i_337_ + i_351_
                        val i_353_ = (i_337_ and 0xff00ff) + (i_351_ and 0xff00ff)
                        i_351_ = (i_353_ and 0x1000100) + (i_352_ - i_353_ and 0x10000)
                        this.anIntArray7483!![i_340_++] = i_352_ - i_351_ or i_351_ - (i_351_ ushr 8)
                    }
                    i_340_ += i_339_
                }
            } else throw IllegalArgumentException()
        }
    }

    // Only reachable from ha.method3635's dead exception-handler path (the
    // constructors that call it succeed without throwing in this renderer).
    override fun method3652() {
        if (aBoolean7471) {
            aBoolean7471 = false
        }
        aCanvas7468 = null
        aIterableHashTable_7467 = null
        aBoolean7470 = true
    }

    constructor(canvas: Canvas?, var_textureSource: TextureSource?, i: Int, i_355_: Int) : this(var_textureSource) {
        try {
            method3643(canvas, i, i_355_)
            method3677(canvas)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            this.method3635()
            throw RuntimeException("")
        }
    }

    fun method3719(i: Int): IntArray? {
        var class348_sub25: Class348_Sub25?
        synchronized(aClass60_7498) {
            class348_sub25 = (aClass60_7498.method583(i.toLong() or 0x7fffffffffffffffL.inv()) as Class348_Sub25?)
            if (class348_sub25 == null) {
                if (!this.textureSource!!.method4(i)) return null
                val textureMetrics = this.textureSource!!.getMetrics(i)
                val i_356_ = (if (textureMetrics!!.small || aBoolean7489) 64 else this.anInt7501)
                class348_sub25 = Class348_Sub25(i, i_356_, this.textureSource!!.method6(i_356_, 0.7f, i, i_356_)!!, textureMetrics.alphaBlendMode != 1)
                aClass60_7498.method582(class348_sub25, i.toLong() or 0x7fffffffffffffffL.inv())
            }
        }
        return class348_sub25!!.method2997()
    }

    override fun method3654(): Matrix {
        return Matrix_Sub1()
    }

    override fun la() {
        this.anInt7496 = 0
        this.anInt7476 = 0
        this.anInt7507 = this.anInt7477
        this.anInt7503 = anInt7486
        method3713()
    }

    override fun createModel(mesh: Mesh, functionMask: Int, featureMask: Int, ambient: Int, contrast: Int): Model {
        return JavaModel(this, mesh, functionMask, ambient, contrast, featureMask)
    }

    fun method3677(canvas: Canvas?) {
        if (canvas == null) {
            aCanvas7468 = null
            this.anIntArray7483 = null
            anInt7486 = 1
            this.anInt7477 = anInt7486
            anInt7488 = 1
            anInt7495 = anInt7488
            method3717()
        } else {
            val class348_sub31 = (aIterableHashTable_7467!!.method3480(canvas.hashCode().toLong()) as Class348_Sub31?)
            if (class348_sub31 != null) {
                aCanvas7468 = canvas
                this.anIntArray7483 = class348_sub31.anIntArray6916
                this.anInt7477 = class348_sub31.anInt6917
                anInt7486 = class348_sub31.anInt6920
                if (this.anInt7477 != anInt7495 || anInt7486 != anInt7488) {
                    anInt7495 = this.anInt7477
                    anInt7488 = anInt7486
                    this.aFloatArray7511 = FloatArray(anInt7495 * anInt7488)
                }
                method3717()
            }
        }
    }

    private fun method3723(i: Int, i_447_: Int, i_448_: Int, i_449_: Int, i_450_: Int, i_451_: Int) {
        var i_447_ = i_447_
        var i_449_ = i_449_
        var i_450_ = i_450_
        if (i_449_ < 0) i_449_ = -i_449_
        var i_452_ = i_447_ - i_449_
        if (i_452_ < this.anInt7476) i_452_ = this.anInt7476
        var i_453_ = i_447_ + i_449_ + 1
        if (i_453_ > this.anInt7503) i_453_ = this.anInt7503
        var i_454_ = i_452_
        val i_455_ = i_449_ * i_449_
        var i_456_ = 0
        var i_457_ = i_447_ - i_454_
        var i_458_ = i_457_ * i_457_
        var i_459_ = i_458_ - i_457_
        if (i_447_ > i_453_) i_447_ = i_453_
        val i_460_ = i_450_ ushr 24
        if (i_451_ == 0 || i_451_ == 1 && i_460_ == 255) {
            while (i_454_ < i_447_) {
                while ( /**/i_459_ <= i_455_ || i_458_ <= i_455_) {
                    i_458_ += i_456_ + i_456_
                    i_459_ += i_456_++ + i_456_
                }
                var i_461_ = i - i_456_ + 1
                if (i_461_ < this.anInt7496) i_461_ = this.anInt7496
                var i_462_ = i + i_456_
                if (i_462_ > this.anInt7507) i_462_ = this.anInt7507
                var i_463_ = i_461_ + i_454_ * this.anInt7477
                for (i_464_ in i_461_..<i_462_) {
                    if (i_448_.toFloat() < this.aFloatArray7511!![i_463_]) this.anIntArray7483!![i_463_] = i_450_
                    i_463_++
                }
                i_454_++
                i_458_ -= i_457_-- + i_457_
                i_459_ -= i_457_ + i_457_
            }
            i_456_ = i_449_
            i_457_ = i_454_ - i_447_
            i_459_ = i_457_ * i_457_ + i_455_
            i_458_ = i_459_ - i_456_
            i_459_ -= i_457_
            while (i_454_ < i_453_) {
                while ( /**/i_459_ > i_455_ && i_458_ > i_455_) {
                    i_459_ -= i_456_-- + i_456_
                    i_458_ -= i_456_ + i_456_
                }
                var i_465_ = i - i_456_
                if (i_465_ < this.anInt7496) i_465_ = this.anInt7496
                var i_466_ = i + i_456_
                if (i_466_ > this.anInt7507 - 1) i_466_ = this.anInt7507 - 1
                var i_467_ = i_465_ + i_454_ * this.anInt7477
                for (i_468_ in i_465_..i_466_) {
                    if (i_448_.toFloat() < this.aFloatArray7511!![i_467_]) this.anIntArray7483!![i_467_] = i_450_
                    i_467_++
                }
                i_454_++
                i_459_ += i_457_ + i_457_
                i_458_ += i_457_++ + i_457_
            }
        } else if (i_451_ == 1) {
            i_450_ = (((i_450_ and 0xff00ff) * i_460_ shr 8 and 0xff00ff) + ((i_450_ and 0xff00) * i_460_ shr 8 and 0xff00) + (i_460_ shl 24))
            val i_469_ = 256 - i_460_
            while (i_454_ < i_447_) {
                while ( /**/i_459_ <= i_455_ || i_458_ <= i_455_) {
                    i_458_ += i_456_ + i_456_
                    i_459_ += i_456_++ + i_456_
                }
                var i_470_ = i - i_456_ + 1
                if (i_470_ < this.anInt7496) i_470_ = this.anInt7496
                var i_471_ = i + i_456_
                if (i_471_ > this.anInt7507) i_471_ = this.anInt7507
                var i_472_ = i_470_ + i_454_ * this.anInt7477
                for (i_473_ in i_470_..<i_471_) {
                    if (i_448_.toFloat() < this.aFloatArray7511!![i_472_]) {
                        var i_474_ = this.anIntArray7483!![i_472_]
                        i_474_ = (((i_474_ and 0xff00ff) * i_469_ shr 8 and 0xff00ff) + ((i_474_ and 0xff00) * i_469_ shr 8 and 0xff00))
                        this.anIntArray7483!![i_472_] = i_450_ + i_474_
                    }
                    i_472_++
                }
                i_454_++
                i_458_ -= i_457_-- + i_457_
                i_459_ -= i_457_ + i_457_
            }
            i_456_ = i_449_
            i_457_ = -i_457_
            i_459_ = i_457_ * i_457_ + i_455_
            i_458_ = i_459_ - i_456_
            i_459_ -= i_457_
            while (i_454_ < i_453_) {
                while ( /**/i_459_ > i_455_ && i_458_ > i_455_) {
                    i_459_ -= i_456_-- + i_456_
                    i_458_ -= i_456_ + i_456_
                }
                var i_475_ = i - i_456_
                if (i_475_ < this.anInt7496) i_475_ = this.anInt7496
                var i_476_ = i + i_456_
                if (i_476_ > this.anInt7507 - 1) i_476_ = this.anInt7507 - 1
                var i_477_ = i_475_ + i_454_ * this.anInt7477
                for (i_478_ in i_475_..i_476_) {
                    if (i_448_.toFloat() < this.aFloatArray7511!![i_477_]) {
                        var i_479_ = this.anIntArray7483!![i_477_]
                        i_479_ = (((i_479_ and 0xff00ff) * i_469_ shr 8 and 0xff00ff) + ((i_479_ and 0xff00) * i_469_ shr 8 and 0xff00))
                        this.anIntArray7483!![i_477_] = i_450_ + i_479_
                    }
                    i_477_++
                }
                i_454_++
                i_459_ += i_457_ + i_457_
                i_458_ += i_457_++ + i_457_
            }
        } else if (i_451_ == 2) {
            while (i_454_ < i_447_) {
                while ( /**/i_459_ <= i_455_ || i_458_ <= i_455_) {
                    i_458_ += i_456_ + i_456_
                    i_459_ += i_456_++ + i_456_
                }
                var i_480_ = i - i_456_ + 1
                if (i_480_ < this.anInt7496) i_480_ = this.anInt7496
                var i_481_ = i + i_456_
                if (i_481_ > this.anInt7507) i_481_ = this.anInt7507
                var i_482_ = i_480_ + i_454_ * this.anInt7477
                for (i_483_ in i_480_..<i_481_) {
                    if (i_448_.toFloat() < this.aFloatArray7511!![i_482_]) {
                        var i_484_ = this.anIntArray7483!![i_482_]
                        val i_485_ = i_450_ + i_484_
                        val i_486_ = (i_450_ and 0xff00ff) + (i_484_ and 0xff00ff)
                        i_484_ = (i_486_ and 0x1000100) + (i_485_ - i_486_ and 0x10000)
                        this.anIntArray7483!![i_482_] = i_485_ - i_484_ or i_484_ - (i_484_ ushr 8)
                    }
                    i_482_++
                }
                i_454_++
                i_458_ -= i_457_-- + i_457_
                i_459_ -= i_457_ + i_457_
            }
            i_456_ = i_449_
            i_457_ = -i_457_
            i_459_ = i_457_ * i_457_ + i_455_
            i_458_ = i_459_ - i_456_
            i_459_ -= i_457_
            while (i_454_ < i_453_) {
                while ( /**/i_459_ > i_455_ && i_458_ > i_455_) {
                    i_459_ -= i_456_-- + i_456_
                    i_458_ -= i_456_ + i_456_
                }
                var i_487_ = i - i_456_
                if (i_487_ < this.anInt7496) i_487_ = this.anInt7496
                var i_488_ = i + i_456_
                if (i_488_ > this.anInt7507 - 1) i_488_ = this.anInt7507 - 1
                var i_489_ = i_487_ + i_454_ * this.anInt7477
                for (i_490_ in i_487_..i_488_) {
                    if (i_448_.toFloat() < this.aFloatArray7511!![i_489_]) {
                        var i_491_ = this.anIntArray7483!![i_489_]
                        val i_492_ = i_450_ + i_491_
                        val i_493_ = (i_450_ and 0xff00ff) + (i_491_ and 0xff00ff)
                        i_491_ = (i_493_ and 0x1000100) + (i_492_ - i_493_ and 0x10000)
                        this.anIntArray7483!![i_489_] = i_492_ - i_491_ or i_491_ - (i_491_ ushr 8)
                    }
                    i_489_++
                }
                i_454_++
                i_459_ += i_457_ + i_457_
                i_458_ += i_457_++ + i_457_
            }
        } else throw IllegalArgumentException()
    }

    // dependency of method3712 (used by method3720), not in genuine list
    fun method3720(i: Int, i_377_: Int, i_378_: Int, i_379_: Int, i_380_: Int, i_381_: Int, i_382_: Int, i_383_: Int, i_384_: Int, i_385_: Int) {
        if (i_379_ != 0 && i_380_ != 0) {
            if (i_382_ != 65535 && !(this.textureSource!!.getMetrics(i_382_)!!.disableable)) {
                if (anInt7512 != i_382_) {
                    var sprite = (aClass60_7499.method583(i_382_.toLong()) as Sprite?)
                    if (sprite == null) {
                        val `is` = method3719(i_382_)
                        if (`is` == null) return
                        val i_386_ = (if (method3727(i_382_)) 64 else this.anInt7501)
                        sprite = this.createSprite(i_386_, `is`, i_386_, i_386_)
                        aClass60_7499.method582(sprite, i_382_.toLong())
                    }
                    anInt7512 = i_382_
                    aSprite_7513 = sprite
                }
                (aSprite_7513 as Sprite_Sub3).method996(i - i_379_, i_377_ - i_380_, i_378_, i_379_ shl 1, i_380_ shl 1, i_384_, i_383_, i_385_, 1)
            } else method3723(i, i_377_, i_378_, i_379_, i_383_, i_385_)
        }
    }

    fun threadResource(runnable: Runnable?): JavaThreadResource? {
        for (i in 0..<this.anInt7485) {
            if (aJavaThreadResourceArray7480!![i]!!.aRunnable2198 === runnable) return aJavaThreadResourceArray7480!![i]
        }
        return null
    }

    override fun ZA(i: Int, f: Float, f_573_: Float, f_574_: Float, f_575_: Float, f_576_: Float) {
        this.anInt7474 = (f * 65535.0f).toInt()
        this.anInt7478 = (f_573_ * 65535.0f).toInt()
        val f_577_ = sqrt((f_574_ * f_574_ + f_575_ * f_575_ + f_576_ * f_576_).toDouble()).toFloat()
        this.anInt7484 = (f_574_ * 65535.0f / f_577_).toInt()
        this.anInt7473 = (f_575_ * 65535.0f / f_577_).toInt()
        this.anInt7479 = (f_576_ * 65535.0f / f_577_).toInt()
    }

    fun method3725(i: Int): Boolean {
        return this.textureSource!!.method4(i)
    }

    init {
        aIterableHashTable_7467 = IterableHashTable(4)
        this.anInt7474 = 45823
        aBoolean7489 = false
        this.anInt7501 = 128
        this.anInt7476 = 0
        this.anInt7482 = 50
        this.anInt7503 = 0
        this.anInt7496 = 0
        this.anInt7497 = 512
        this.anInt7500 = 75518
        this.anInt7491 = 512
        this.anInt7494 = 3500
        this.anInt7507 = 0
        this.anInt7478 = 78642
        aClass60_7499 = Class60(16)
        anInt7512 = -1
        try {
            aClass60_7498 = Class60(256)
            this.aClass101_Sub1_7492 = Matrix_Sub1()
            method3631(1)
            method3659(0)
            Class59_Sub2_Sub1.method566()
            aBoolean7471 = true
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            this.method3635()
            throw RuntimeException("")
        }
    }

    override fun XA(): Int {
        return this.anInt7494
    }

    override fun method3711(`is`: IntArray, i: Int, i_422_: Int, i_423_: Int, i_424_: Int): Sprite {
        var bool_425_ = false
        var i_426_ = i
        while_229_@ for (i_427_ in 0..<i_424_) {
            for (i_428_ in 0..<i_423_) {
                val i_429_ = `is`[i_426_++] ushr 24
                if (i_429_ != 0 && i_429_ != 255) {
                    bool_425_ = true
                    break@while_229_
                }
            }
        }
        if (bool_425_) return JavaArgbSprite(this, `is`, i, i_422_, i_423_, i_424_)
        return JavaRgbSprite(this, `is`, i, i_422_, i_423_, i_424_)
    }

    fun method3659(i: Int) {
        aJavaThreadResourceArray7480!![i]!!.method1291(Thread.currentThread())
    }

    fun method3726(i: Int): Int {
        return this.textureSource!!.getMetrics(i)!!.alphaBlendMode
    }

    fun method3727(i: Int): Boolean {
        return aBoolean7489 || this.textureSource!!.getMetrics(i)!!.small
    }

    companion object {

        fun method1035(i_16_: Int, canvas: Canvas?, i_17_: Int): Class348_Sub31? {
            try {
                val class348_sub31: Class348_Sub31 = Class348_Sub31_Sub1()
                class348_sub31.method3008(canvas!!, i_17_, i_16_)
                return class348_sub31
            } catch (throwable: Throwable) {
                val class348_sub31_sub2 = Class348_Sub31_Sub2()
                class348_sub31_sub2.method3008(canvas!!, i_17_, i_16_)
                return class348_sub31_sub2
            }
        }
    }
}
