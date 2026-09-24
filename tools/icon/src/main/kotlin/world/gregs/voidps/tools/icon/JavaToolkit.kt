package world.gregs.voidps.tools.icon;/* ha_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;

final class JavaToolkit extends Toolkit {
    private int anInt7465;
    private int anInt7466;
    private IterableHashTable aIterableHashTable_7467;
    private Canvas aCanvas7468;
    Class348_Sub31 aClass348_Sub31_7469;
    private boolean aBoolean7470 = false;
    private boolean aBoolean7471 = false;
    private int anInt7472;
    int anInt7473;
    int anInt7474;
    private Class49 aClass49_7475;
    int anInt7476;
    int anInt7477;
    int anInt7478;
    int anInt7479;
    private JavaThreadResource[] aJavaThreadResourceArray7480;
    private int anInt7481;
    int anInt7482;
    int[] anIntArray7483;
    int anInt7484;
    int anInt7485;
    private int anInt7486;
    private int anInt7487;
    private int anInt7488;
    private boolean aBoolean7489;
    int anInt7490;
    int anInt7491;
    Matrix_Sub1 aClass101_Sub1_7492;
    private int anInt7493;
    int anInt7494;
    private int anInt7495;
    int anInt7496;
    int anInt7497;
    private final Class60 aClass60_7498;
    private final Class60 aClass60_7499;
    int anInt7500;
    int anInt7501;
    float[] aFloatArray7502;
    int anInt7503;
    int anInt7504;
    private int anInt7505;
    int anInt7506;
    int anInt7507;
    int anInt7508;
    int anInt7509;
    int anInt7510;
    float[] aFloatArray7511;
    private int anInt7512;
    private Sprite aSprite_7513;

    final boolean method3695() {
        return true;
    }

    final int[] na(int i, int i_0_, int i_1_, int i_2_) {
        int[] is = new int[i_1_ * i_2_];
        int i_3_ = 0;
        for (int i_4_ = 0; i_4_ < i_2_; i_4_++) {
            int i_5_ = (i_0_ + i_4_) * this.anInt7477 + i;
            for (int i_6_ = 0; i_6_ < i_1_; i_6_++)
                is[i_3_++] = this.anIntArray7483[i_5_ + i_6_];
        }
        return is;
    }

    final aa method3661(int i, int i_63_, int[] is, int[] is_64_) {
        return new aa_Sub3(i, i_63_, is, is_64_);
    }

    final void method3642(int i, Class348_Sub1[] class348_sub1s) {
        /* empty */
    }

    final boolean method3693() {
        return false;
    }

    final void method3710() {
        /* empty */
    }

    // dependency of Class109's genuine drawing methods (method1018/method1024
    // call method3645 unconditionally); not itself in the genuine list.
    final void line(int i, int i_41_, int i_42_, int i_43_, int i_44_, int i_45_) {
        Toolkit.anInt4563++;
        method3709(i_41_, i, i_42_, i_45_, i_44_, 1);
        if (i_43_ != -8003) method3665(75, 67);
    }

    // dependency of method3645
    final void method3709(int i, int i_241_, int i_242_, int i_243_, int i_244_, int i_245_) {
        i_242_ -= i;
        i_243_ -= i_241_;
        if (i_243_ == 0) {
            if (i_242_ >= 0) U(i, i_241_, i_242_ + 1, i_244_, i_245_);
            else U(i + i_242_, i_241_, -i_242_ + 1, i_244_, i_245_);
        } else if (i_242_ == 0) {
            if (i_243_ >= 0) P(i, i_241_, i_243_ + 1, i_244_, i_245_);
            else P(i, i_241_ + i_243_, -i_243_ + 1, i_244_, i_245_);
        } else {
            if (i_242_ + i_243_ < 0) {
                i += i_242_;
                i_242_ = -i_242_;
                i_241_ += i_243_;
                i_243_ = -i_243_;
            }
            if (i_242_ > i_243_) {
                i_241_ <<= 16;
                i_241_ += 32768;
                i_243_ <<= 16;
                int i_246_ = (int) Math.floor((double) i_243_ / (double) i_242_ + 0.5);
                i_242_ += i;
                if (i < this.anInt7496) {
                    i_241_ += i_246_ * (this.anInt7496 - i);
                    i = this.anInt7496;
                }
                if (i_242_ >= this.anInt7507) i_242_ = this.anInt7507 - 1;
                int i_247_ = i_244_ >>> 24;
                if (i_245_ == 0 || i_245_ == 1 && i_247_ == 255) {
                    for (/**/; i <= i_242_; i++) {
                        int i_248_ = i_241_ >> 16;
                        if (i_248_ >= this.anInt7476 && i_248_ < this.anInt7503) this.anIntArray7483[i + i_248_ * this.anInt7477] = i_244_;
                        i_241_ += i_246_;
                    }
                    return;
                }
                if (i_245_ == 1) {
                    i_244_ = (((i_244_ & 0xff00ff) * i_247_ >> 8 & 0xff00ff) + ((i_244_ & 0xff00) * i_247_ >> 8 & 0xff00) + (i_247_ << 24));
                    int i_249_ = 256 - i_247_;
                    for (/**/; i <= i_242_; i++) {
                        int i_250_ = i_241_ >> 16;
                        if (i_250_ >= this.anInt7476 && i_250_ < this.anInt7503) {
                            int i_251_ = i + i_250_ * this.anInt7477;
                            int i_252_ = this.anIntArray7483[i_251_];
                            i_252_ = (((i_252_ & 0xff00ff) * i_249_ >> 8 & 0xff00ff) + ((i_252_ & 0xff00) * i_249_ >> 8 & 0xff00));
                            this.anIntArray7483[i_251_] = i_244_ + i_252_;
                        }
                        i_241_ += i_246_;
                    }
                    return;
                }
                if (i_245_ == 2) {
                    for (/**/; i <= i_242_; i++) {
                        int i_253_ = i_241_ >> 16;
                        if (i_253_ >= this.anInt7476 && i_253_ < this.anInt7503) {
                            int i_254_ = i + i_253_ * this.anInt7477;
                            int i_255_ = this.anIntArray7483[i_254_];
                            int i_256_ = i_244_ + i_255_;
                            int i_257_ = (i_244_ & 0xff00ff) + (i_255_ & 0xff00ff);
                            i_255_ = (i_257_ & 0x1000100) + (i_256_ - i_257_ & 0x10000);
                            this.anIntArray7483[i_254_] = i_256_ - i_255_ | i_255_ - (i_255_ >>> 8);
                        }
                        i_241_ += i_246_;
                    }
                    return;
                }
                throw new IllegalArgumentException();
            }
            i <<= 16;
            i += 32768;
            i_242_ <<= 16;
            int i_258_ = (int) Math.floor((double) i_242_ / (double) i_243_ + 0.5);
            i_243_ += i_241_;
            if (i_241_ < this.anInt7476) {
                i += i_258_ * (this.anInt7476 - i_241_);
                i_241_ = this.anInt7476;
            }
            if (i_243_ >= this.anInt7503) i_243_ = this.anInt7503 - 1;
            int i_259_ = i_244_ >>> 24;
            if (i_245_ == 0 || i_245_ == 1 && i_259_ == 255) {
                for (/**/; i_241_ <= i_243_; i_241_++) {
                    int i_260_ = i >> 16;
                    if (i_260_ >= this.anInt7496 && i_260_ < this.anInt7507) this.anIntArray7483[i_260_ + i_241_ * this.anInt7477] = i_244_;
                    i += i_258_;
                }
            } else if (i_245_ == 1) {
                i_244_ = (((i_244_ & 0xff00ff) * i_259_ >> 8 & 0xff00ff) + ((i_244_ & 0xff00) * i_259_ >> 8 & 0xff00) + (i_259_ << 24));
                int i_261_ = 256 - i_259_;
                for (/**/; i_241_ <= i_243_; i_241_++) {
                    int i_262_ = i >> 16;
                    if (i_262_ >= this.anInt7496 && i_262_ < this.anInt7507) {
                        int i_263_ = i_262_ + i_241_ * this.anInt7477;
                        int i_264_ = this.anIntArray7483[i_263_];
                        i_264_ = (((i_264_ & 0xff00ff) * i_261_ >> 8 & 0xff00ff) + ((i_264_ & 0xff00) * i_261_ >> 8 & 0xff00));
                        this.anIntArray7483[i_262_ + i_241_ * this.anInt7477] = i_244_ + i_264_;
                    }
                    i += i_258_;
                }
            } else if (i_245_ == 2) {
                for (/**/; i_241_ <= i_243_; i_241_++) {
                    int i_265_ = i >> 16;
                    if (i_265_ >= this.anInt7496 && i_265_ < this.anInt7507) {
                        int i_266_ = i_265_ + i_241_ * this.anInt7477;
                        int i_267_ = this.anIntArray7483[i_266_];
                        int i_268_ = i_244_ + i_267_;
                        int i_269_ = (i_244_ & 0xff00ff) + (i_267_ & 0xff00ff);
                        i_267_ = (i_269_ & 0x1000100) + (i_268_ - i_269_ & 0x10000);
                        this.anIntArray7483[i_266_] = i_268_ - i_267_ | i_267_ - (i_267_ >>> 8);
                    }
                    i += i_258_;
                }
            } else throw new IllegalArgumentException();
        }
    }

    // dependency of method3709
    final void U(int i, int i_186_, int i_187_, int i_188_, int i_189_) {
        if (i_186_ >= this.anInt7476 && i_186_ < this.anInt7503) {
            if (i < this.anInt7496) {
                i_187_ -= this.anInt7496 - i;
                i = this.anInt7496;
            }
            if (i + i_187_ > this.anInt7507) i_187_ = this.anInt7507 - i;
            int i_190_ = i + i_186_ * this.anInt7477;
            int i_191_ = i_188_ >>> 24;
            if (i_189_ == 0 || i_189_ == 1 && i_191_ == 255) {
                for (int i_192_ = 0; i_192_ < i_187_; i_192_++)
                    this.anIntArray7483[i_190_ + i_192_] = i_188_;
            } else if (i_189_ == 1) {
                i_188_ = (((i_188_ & 0xff00ff) * i_191_ >> 8 & 0xff00ff) + ((i_188_ & 0xff00) * i_191_ >> 8 & 0xff00) + (i_191_ << 24));
                int i_193_ = 256 - i_191_;
                for (int i_194_ = 0; i_194_ < i_187_; i_194_++) {
                    int i_195_ = this.anIntArray7483[i_190_ + i_194_];
                    i_195_ = (((i_195_ & 0xff00ff) * i_193_ >> 8 & 0xff00ff) + ((i_195_ & 0xff00) * i_193_ >> 8 & 0xff00));
                    this.anIntArray7483[i_190_ + i_194_] = i_188_ + i_195_;
                }
            } else if (i_189_ == 2) {
                for (int i_196_ = 0; i_196_ < i_187_; i_196_++) {
                    int i_197_ = this.anIntArray7483[i_190_ + i_196_];
                    int i_198_ = i_188_ + i_197_;
                    int i_199_ = (i_188_ & 0xff00ff) + (i_197_ & 0xff00ff);
                    i_197_ = (i_199_ & 0x1000100) + (i_198_ - i_199_ & 0x10000);
                    this.anIntArray7483[i_190_ + i_196_] = i_198_ - i_197_ | i_197_ - (i_197_ >>> 8);
                }
            } else throw new IllegalArgumentException();
        }
    }

    // dependency of method3709
    final void P(int i, int i_71_, int i_72_, int i_73_, int i_74_) {
        if (i >= this.anInt7496 && i < this.anInt7507) {
            if (i_71_ < this.anInt7476) {
                i_72_ -= this.anInt7476 - i_71_;
                i_71_ = this.anInt7476;
            }
            if (i_71_ + i_72_ > this.anInt7503) i_72_ = this.anInt7503 - i_71_;
            int i_75_ = i + i_71_ * this.anInt7477;
            int i_76_ = i_73_ >>> 24;
            if (i_74_ == 0 || i_74_ == 1 && i_76_ == 255) {
                for (int i_77_ = 0; i_77_ < i_72_; i_77_++)
                    this.anIntArray7483[i_75_ + i_77_ * this.anInt7477] = i_73_;
            } else if (i_74_ == 1) {
                i_73_ = (((i_73_ & 0xff00ff) * i_76_ >> 8 & 0xff00ff) + ((i_73_ & 0xff00) * i_76_ >> 8 & 0xff00) + (i_76_ << 24));
                int i_78_ = 256 - i_76_;
                for (int i_79_ = 0; i_79_ < i_72_; i_79_++) {
                    int i_80_ = i_75_ + i_79_ * this.anInt7477;
                    int i_81_ = this.anIntArray7483[i_80_];
                    i_81_ = (((i_81_ & 0xff00ff) * i_78_ >> 8 & 0xff00ff) + ((i_81_ & 0xff00) * i_78_ >> 8 & 0xff00));
                    this.anIntArray7483[i_80_] = i_73_ + i_81_;
                }
            } else if (i_74_ == 2) {
                for (int i_82_ = 0; i_82_ < i_72_; i_82_++) {
                    int i_83_ = i_75_ + i_82_ * this.anInt7477;
                    int i_84_ = this.anIntArray7483[i_83_];
                    int i_85_ = i_73_ + i_84_;
                    int i_86_ = (i_73_ & 0xff00ff) + (i_84_ & 0xff00ff);
                    i_84_ = (i_86_ & 0x1000100) + (i_85_ - i_86_ & 0x10000);
                    this.anIntArray7483[i_83_] = i_85_ - i_84_ | i_84_ - (i_84_ >>> 8);
                }
            } else throw new IllegalArgumentException();
        }
    }

    final void method3647(boolean bool) {
        /* empty */
    }

    final void xa(float f) {
        this.anInt7500 = (int) (f * 65535.0F);
    }

    final void method3651(za var_za) {
        /* empty */
    }

// explicitly listed as genuine
    private final void method3713() {
        this.anInt7509 = this.anInt7496 - this.anInt7510;
        this.anInt7508 = this.anInt7507 - this.anInt7510;
        this.anInt7490 = this.anInt7476 - this.anInt7504;
        this.anInt7506 = this.anInt7503 - this.anInt7504;
        for (int i = 0; i < this.anInt7485; i++) {
            Rasterizer rasterizer = aJavaThreadResourceArray7480[i].rasterizer;
            rasterizer.anInt1665 = this.anInt7510 - this.anInt7496;
            rasterizer.anInt1668 = this.anInt7504 - this.anInt7476;
            rasterizer.width = this.anInt7507 - this.anInt7496;
            rasterizer.height = this.anInt7503 - this.anInt7476;
        }
        int i = (this.anInt7476 * this.anInt7477 + this.anInt7496);
        for (int i_97_ = this.anInt7476; i_97_ < this.anInt7503; i_97_++) {
            for (int i_98_ = 0; i_98_ < this.anInt7485; i_98_++)
                aJavaThreadResourceArray7480[i_98_].rasterizer.lineOffsets[i_97_ - this.anInt7476] = i;
            i += this.anInt7477;
        }
    }

    final int i() {
        return this.anInt7482;
    }

    final Matrix method3705() {
        JavaThreadResource javaThreadResource = threadResource(Thread.currentThread());
        return javaThreadResource.aClass101_Sub1_2209;
    }

    final boolean method3714(int i) {
        return this.textureSource.getMetrics(i, -6662).aBoolean217 || this.textureSource.getMetrics(i, -6662).aBoolean215;
    }

    final boolean method3655() {
        return false;
    }

    final Class299 method3697(int i, int i_137_, int i_138_, int i_139_, int i_140_, int i_141_) {
        return null;
    }

    final void method3698() {
        /* empty */
    }

    final void setCamera(Matrix matrix) {
        this.aClass101_Sub1_7492 = (Matrix_Sub1) matrix;
    }

    final void f(int i, int i_156_) {
        JavaThreadResource javaThreadResource = threadResource(Thread.currentThread());
        this.anInt7482 = i;
        this.anInt7494 = i_156_;
        javaThreadResource.anInt2210 = this.anInt7494 - 255;
    }

    final void ya() {
        if (this.anInt7496 == 0 && this.anInt7507 == this.anInt7477 && this.anInt7476 == 0 && this.anInt7503 == anInt7486) {
            int i = this.aFloatArray7511.length;
            int i_176_ = i - (i & 0x7);
            int i_177_ = 0;
            while (i_177_ < i_176_) {
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
                this.aFloatArray7511[i_177_++] = 2.14748365E9F;
            }
            while (i_177_ < i) this.aFloatArray7511[i_177_++] = 2.14748365E9F;
        } else {
            int i = this.anInt7507 - this.anInt7496;
            int i_178_ = this.anInt7503 - this.anInt7476;
            int i_179_ = this.anInt7477 - i;
            int i_180_ = (this.anInt7496 + this.anInt7476 * this.anInt7477);
            int i_181_ = i >> 3;
            int i_182_ = i & 0x7;
            i = i_180_ - 1;
            for (int i_183_ = -i_178_; i_183_ < 0; i_183_++) {
                if (i_181_ > 0) {
                    int i_184_ = i_181_;
                    do {
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                        this.aFloatArray7511[++i] = 2.14748365E9F;
                    } while (--i_184_ > 0);
                }
                if (i_182_ > 0) {
                    int i_185_ = i_182_;
                    do this.aFloatArray7511[++i] = 2.14748365E9F; while (--i_185_ > 0);
                }
                i += i_179_;
            }
        }
    }

    final boolean method3639() {
        return true;
    }

    final boolean method3716() {
        return aBoolean7470;
    }

    final void method3658(int i, int i_205_, int i_206_, int i_207_) {
        /* empty */
    }

    final void method3700(float f, float f_213_, float f_214_) {
        /* empty */
    }

    private final void method3717() {
        for (int i = 0; i < this.anInt7485; i++)
            aJavaThreadResourceArray7480[i].method1292(64);
        la();
    }

    final boolean method3682() {
        return false;
    }

    final boolean method3670() {
        return false;
    }

    final void DA(int i, int i_223_, int i_224_, int i_225_) {
        this.anInt7510 = i;
        this.anInt7504 = i_223_;
        this.anInt7491 = i_224_;
        this.anInt7497 = i_225_;
        method3713();
    }

    final boolean method3627() {
        return false;
    }

    final void method3643(Canvas canvas, int i, int i_232_) {
        Class348_Sub31 class348_sub31 = ((Class348_Sub31) aIterableHashTable_7467.method3480(canvas.hashCode(), -6008));
        if (class348_sub31 == null) {
            class348_sub31 = Class110.method1035(9029, i_232_, canvas, i);
            aIterableHashTable_7467.put((byte) 21, canvas.hashCode(), class348_sub31);
        } else if (class348_sub31.anInt6917 != i || class348_sub31.anInt6920 != i_232_) method3669(canvas, i, i_232_);
    }

    final void method3633() {
        /* empty */
    }

    final void method3631(int i) {
        this.anInt7485 = i;
        aJavaThreadResourceArray7480 = new JavaThreadResource[this.anInt7485];
        for (int i_240_ = 0; i_240_ < this.anInt7485; i_240_++)
            aJavaThreadResourceArray7480[i_240_] = new JavaThreadResource(this);
    }

    final void method3653(Class299 class299) {
        /* empty */
    }

    // dependency of method3643 (not in genuine list, required to compile)
    final void method3669(Canvas canvas, int i, int i_578_) {
        Class348_Sub31 class348_sub31 = ((Class348_Sub31) aIterableHashTable_7467.method3480(canvas.hashCode(), -6008));
        if (class348_sub31 != null) {
            class348_sub31.unlink((byte) 95);
            class348_sub31 = Class110.method1035(9029, i_578_, canvas, i);
            aIterableHashTable_7467.put((byte) 112, canvas.hashCode(), class348_sub31);
            if (aCanvas7468 == canvas && aClass49_7475 == null) {
                Dimension dimension = canvas.getSize();
                anInt7465 = dimension.width;
                anInt7472 = dimension.height;
                this.aClass348_Sub31_7469 = class348_sub31;
                this.anIntArray7483 = class348_sub31.anIntArray6916;
                this.anInt7477 = class348_sub31.anInt6917;
                anInt7486 = class348_sub31.anInt6920;
                if (this.anInt7477 != anInt7495 || anInt7486 != anInt7488) {
                    anInt7481 = anInt7495 = this.anInt7477;
                    anInt7493 = anInt7488 = anInt7486;
                    this.aFloatArray7502 = this.aFloatArray7511 = new float[anInt7495 * anInt7488];
                }
                method3717();
            }
        }
    }

    final void aa(int i, int i_334_, int i_335_, int i_336_, int i_337_, int i_338_) {
        if (i < this.anInt7496) {
            i_335_ -= this.anInt7496 - i;
            i = this.anInt7496;
        }
        if (i_334_ < this.anInt7476) {
            i_336_ -= this.anInt7476 - i_334_;
            i_334_ = this.anInt7476;
        }
        if (i + i_335_ > this.anInt7507) i_335_ = this.anInt7507 - i;
        if (i_334_ + i_336_ > this.anInt7503) i_336_ = this.anInt7503 - i_334_;
        if (i_335_ > 0 && i_336_ > 0 && i <= this.anInt7507 && i_334_ <= this.anInt7503) {
            int i_339_ = this.anInt7477 - i_335_;
            int i_340_ = i + i_334_ * this.anInt7477;
            int i_341_ = i_337_ >>> 24;
            if (i_338_ == 0 || i_338_ == 1 && i_341_ == 255) {
                int i_342_ = i_335_ >> 3;
                int i_343_ = i_335_ & 0x7;
                i_335_ = i_340_ - 1;
                for (int i_344_ = -i_336_; i_344_ < 0; i_344_++) {
                    if (i_342_ > 0) {
                        i = i_342_;
                        do {
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                            this.anIntArray7483[++i_335_] = i_337_;
                        } while (--i > 0);
                    }
                    if (i_343_ > 0) {
                        i = i_343_;
                        do this.anIntArray7483[++i_335_] = i_337_; while (--i > 0);
                    }
                    i_335_ += i_339_;
                }
            } else if (i_338_ == 1) {
                i_337_ = (((i_337_ & 0xff00ff) * i_341_ >> 8 & 0xff00ff) + (((i_337_ & ~0xff00ff) >>> 8) * i_341_ & ~0xff00ff));
                int i_345_ = 256 - i_341_;
                for (int i_346_ = 0; i_346_ < i_336_; i_346_++) {
                    for (int i_347_ = -i_335_; i_347_ < 0; i_347_++) {
                        int i_348_ = this.anIntArray7483[i_340_];
                        i_348_ = (((i_348_ & 0xff00ff) * i_345_ >> 8 & 0xff00ff) + (((i_348_ & ~0xff00ff) >>> 8) * i_345_ & ~0xff00ff));
                        this.anIntArray7483[i_340_++] = i_337_ + i_348_;
                    }
                    i_340_ += i_339_;
                }
            } else if (i_338_ == 2) {
                for (int i_349_ = 0; i_349_ < i_336_; i_349_++) {
                    for (int i_350_ = -i_335_; i_350_ < 0; i_350_++) {
                        int i_351_ = this.anIntArray7483[i_340_];
                        int i_352_ = i_337_ + i_351_;
                        int i_353_ = (i_337_ & 0xff00ff) + (i_351_ & 0xff00ff);
                        i_351_ = (i_353_ & 0x1000100) + (i_352_ - i_353_ & 0x10000);
                        this.anIntArray7483[i_340_++] = i_352_ - i_351_ | i_351_ - (i_351_ >>> 8);
                    }
                    i_340_ += i_339_;
                }
            } else throw new IllegalArgumentException();
        }
    }

    // Only reachable from ha.method3635's dead exception-handler path (the
    // constructors that call it succeed without throwing in this renderer).
    final void method3652() {
        if (aBoolean7471) {
            aBoolean7471 = false;
        }
        this.aClass348_Sub31_7469 = null;
        aCanvas7468 = null;
        anInt7465 = 0;
        anInt7472 = 0;
        aIterableHashTable_7467 = null;
        aBoolean7470 = true;
    }

    final Matrix method3640() {
        return this.aClass101_Sub1_7492;
    }

    JavaToolkit(Canvas canvas, TextureSource var_textureSource, int i, int i_355_) {
        this(var_textureSource);
        try {
            method3643(canvas, i, i_355_);
            method3677(canvas);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            this.method3635((byte) -115);
            throw new RuntimeException("");
        }
    }

    final int[] method3719(int i) {
        Class348_Sub25 class348_sub25;
        synchronized (aClass60_7498) {
            class348_sub25 = ((Class348_Sub25) aClass60_7498.method583((long) i | ~0x7fffffffffffffffL, 107));
            if (class348_sub25 == null) {
                if (!this.textureSource.method4(-7953, i)) return null;
                TextureMetrics textureMetrics = this.textureSource.getMetrics(i, -6662);
                int i_356_ = (textureMetrics.small || aBoolean7489 ? 64 : this.anInt7501);
                class348_sub25 = new Class348_Sub25(i, i_356_, this.textureSource.method6(-21540, i_356_, 0.7F, i, true, i_356_), textureMetrics.alphaBlendMode != 1);
                aClass60_7498.method582(class348_sub25, (long) i | ~0x7fffffffffffffffL, (byte) -126);
            }
        }
        class348_sub25.aBoolean6882 = true;
        return class348_sub25.method2997();
    }

    final Matrix method3654() {
        return new Matrix_Sub1();
    }

    final void la() {
        this.anInt7496 = 0;
        this.anInt7476 = 0;
        this.anInt7507 = this.anInt7477;
        this.anInt7503 = anInt7486;
        method3713();
    }

    final Model createModel(Mesh mesh, int functionMask, int featureMask, int ambient, int contrast) {
        return new JavaModel(this, mesh, functionMask, ambient, contrast, featureMask);
    }

    final Interface13 method3624(int i, int i_369_) {
        return new Class216(i, i_369_);
    }

    final void method3677(Canvas canvas) {
        if (canvas == null) {
            aCanvas7468 = null;
            this.aClass348_Sub31_7469 = null;
            if (aClass49_7475 == null) {
                this.anIntArray7483 = null;
                this.anInt7477 = anInt7486 = 1;
                anInt7495 = anInt7488 = 1;
                method3717();
            }
        } else {
            Class348_Sub31 class348_sub31 = ((Class348_Sub31) aIterableHashTable_7467.method3480(canvas.hashCode(), -6008));
            if (class348_sub31 != null) {
                aCanvas7468 = canvas;
                Dimension dimension = canvas.getSize();
                anInt7465 = dimension.width;
                anInt7472 = dimension.height;
                this.aClass348_Sub31_7469 = class348_sub31;
                if (aClass49_7475 == null) {
                    this.anIntArray7483 = class348_sub31.anIntArray6916;
                    this.anInt7477 = class348_sub31.anInt6917;
                    anInt7486 = class348_sub31.anInt6920;
                    if (this.anInt7477 != anInt7495 || anInt7486 != anInt7488) {
                        anInt7481 = anInt7495 = this.anInt7477;
                        anInt7493 = anInt7488 = anInt7486;
                        this.aFloatArray7502 = this.aFloatArray7511 = new float[anInt7495 * anInt7488];
                    }
                    method3717();
                }
            }
        }
    }

    private final void method3723(int i, int i_447_, int i_448_, int i_449_, int i_450_, int i_451_) {
        if (i_449_ < 0) i_449_ = -i_449_;
        int i_452_ = i_447_ - i_449_;
        if (i_452_ < this.anInt7476) i_452_ = this.anInt7476;
        int i_453_ = i_447_ + i_449_ + 1;
        if (i_453_ > this.anInt7503) i_453_ = this.anInt7503;
        int i_454_ = i_452_;
        int i_455_ = i_449_ * i_449_;
        int i_456_ = 0;
        int i_457_ = i_447_ - i_454_;
        int i_458_ = i_457_ * i_457_;
        int i_459_ = i_458_ - i_457_;
        if (i_447_ > i_453_) i_447_ = i_453_;
        int i_460_ = i_450_ >>> 24;
        if (i_451_ == 0 || i_451_ == 1 && i_460_ == 255) {
            while (i_454_ < i_447_) {
                for (/**/; i_459_ <= i_455_ || i_458_ <= i_455_; i_459_ += i_456_++ + i_456_)
                    i_458_ += i_456_ + i_456_;
                int i_461_ = i - i_456_ + 1;
                if (i_461_ < this.anInt7496) i_461_ = this.anInt7496;
                int i_462_ = i + i_456_;
                if (i_462_ > this.anInt7507) i_462_ = this.anInt7507;
                int i_463_ = i_461_ + i_454_ * this.anInt7477;
                for (int i_464_ = i_461_; i_464_ < i_462_; i_464_++) {
                    if ((float) i_448_ < this.aFloatArray7511[i_463_]) this.anIntArray7483[i_463_] = i_450_;
                    i_463_++;
                }
                i_454_++;
                i_458_ -= i_457_-- + i_457_;
                i_459_ -= i_457_ + i_457_;
            }
            i_456_ = i_449_;
            i_457_ = i_454_ - i_447_;
            i_459_ = i_457_ * i_457_ + i_455_;
            i_458_ = i_459_ - i_456_;
            i_459_ -= i_457_;
            while (i_454_ < i_453_) {
                for (/**/; i_459_ > i_455_ && i_458_ > i_455_; i_458_ -= i_456_ + i_456_)
                    i_459_ -= i_456_-- + i_456_;
                int i_465_ = i - i_456_;
                if (i_465_ < this.anInt7496) i_465_ = this.anInt7496;
                int i_466_ = i + i_456_;
                if (i_466_ > this.anInt7507 - 1) i_466_ = this.anInt7507 - 1;
                int i_467_ = i_465_ + i_454_ * this.anInt7477;
                for (int i_468_ = i_465_; i_468_ <= i_466_; i_468_++) {
                    if ((float) i_448_ < this.aFloatArray7511[i_467_]) this.anIntArray7483[i_467_] = i_450_;
                    i_467_++;
                }
                i_454_++;
                i_459_ += i_457_ + i_457_;
                i_458_ += i_457_++ + i_457_;
            }
        } else if (i_451_ == 1) {
            i_450_ = (((i_450_ & 0xff00ff) * i_460_ >> 8 & 0xff00ff) + ((i_450_ & 0xff00) * i_460_ >> 8 & 0xff00) + (i_460_ << 24));
            int i_469_ = 256 - i_460_;
            while (i_454_ < i_447_) {
                for (/**/; i_459_ <= i_455_ || i_458_ <= i_455_; i_459_ += i_456_++ + i_456_)
                    i_458_ += i_456_ + i_456_;
                int i_470_ = i - i_456_ + 1;
                if (i_470_ < this.anInt7496) i_470_ = this.anInt7496;
                int i_471_ = i + i_456_;
                if (i_471_ > this.anInt7507) i_471_ = this.anInt7507;
                int i_472_ = i_470_ + i_454_ * this.anInt7477;
                for (int i_473_ = i_470_; i_473_ < i_471_; i_473_++) {
                    if ((float) i_448_ < this.aFloatArray7511[i_472_]) {
                        int i_474_ = this.anIntArray7483[i_472_];
                        i_474_ = (((i_474_ & 0xff00ff) * i_469_ >> 8 & 0xff00ff) + ((i_474_ & 0xff00) * i_469_ >> 8 & 0xff00));
                        this.anIntArray7483[i_472_] = i_450_ + i_474_;
                    }
                    i_472_++;
                }
                i_454_++;
                i_458_ -= i_457_-- + i_457_;
                i_459_ -= i_457_ + i_457_;
            }
            i_456_ = i_449_;
            i_457_ = -i_457_;
            i_459_ = i_457_ * i_457_ + i_455_;
            i_458_ = i_459_ - i_456_;
            i_459_ -= i_457_;
            while (i_454_ < i_453_) {
                for (/**/; i_459_ > i_455_ && i_458_ > i_455_; i_458_ -= i_456_ + i_456_)
                    i_459_ -= i_456_-- + i_456_;
                int i_475_ = i - i_456_;
                if (i_475_ < this.anInt7496) i_475_ = this.anInt7496;
                int i_476_ = i + i_456_;
                if (i_476_ > this.anInt7507 - 1) i_476_ = this.anInt7507 - 1;
                int i_477_ = i_475_ + i_454_ * this.anInt7477;
                for (int i_478_ = i_475_; i_478_ <= i_476_; i_478_++) {
                    if ((float) i_448_ < this.aFloatArray7511[i_477_]) {
                        int i_479_ = this.anIntArray7483[i_477_];
                        i_479_ = (((i_479_ & 0xff00ff) * i_469_ >> 8 & 0xff00ff) + ((i_479_ & 0xff00) * i_469_ >> 8 & 0xff00));
                        this.anIntArray7483[i_477_] = i_450_ + i_479_;
                    }
                    i_477_++;
                }
                i_454_++;
                i_459_ += i_457_ + i_457_;
                i_458_ += i_457_++ + i_457_;
            }
        } else if (i_451_ == 2) {
            while (i_454_ < i_447_) {
                for (/**/; i_459_ <= i_455_ || i_458_ <= i_455_; i_459_ += i_456_++ + i_456_)
                    i_458_ += i_456_ + i_456_;
                int i_480_ = i - i_456_ + 1;
                if (i_480_ < this.anInt7496) i_480_ = this.anInt7496;
                int i_481_ = i + i_456_;
                if (i_481_ > this.anInt7507) i_481_ = this.anInt7507;
                int i_482_ = i_480_ + i_454_ * this.anInt7477;
                for (int i_483_ = i_480_; i_483_ < i_481_; i_483_++) {
                    if ((float) i_448_ < this.aFloatArray7511[i_482_]) {
                        int i_484_ = this.anIntArray7483[i_482_];
                        int i_485_ = i_450_ + i_484_;
                        int i_486_ = (i_450_ & 0xff00ff) + (i_484_ & 0xff00ff);
                        i_484_ = (i_486_ & 0x1000100) + (i_485_ - i_486_ & 0x10000);
                        this.anIntArray7483[i_482_] = i_485_ - i_484_ | i_484_ - (i_484_ >>> 8);
                    }
                    i_482_++;
                }
                i_454_++;
                i_458_ -= i_457_-- + i_457_;
                i_459_ -= i_457_ + i_457_;
            }
            i_456_ = i_449_;
            i_457_ = -i_457_;
            i_459_ = i_457_ * i_457_ + i_455_;
            i_458_ = i_459_ - i_456_;
            i_459_ -= i_457_;
            while (i_454_ < i_453_) {
                for (/**/; i_459_ > i_455_ && i_458_ > i_455_; i_458_ -= i_456_ + i_456_)
                    i_459_ -= i_456_-- + i_456_;
                int i_487_ = i - i_456_;
                if (i_487_ < this.anInt7496) i_487_ = this.anInt7496;
                int i_488_ = i + i_456_;
                if (i_488_ > this.anInt7507 - 1) i_488_ = this.anInt7507 - 1;
                int i_489_ = i_487_ + i_454_ * this.anInt7477;
                for (int i_490_ = i_487_; i_490_ <= i_488_; i_490_++) {
                    if ((float) i_448_ < this.aFloatArray7511[i_489_]) {
                        int i_491_ = this.anIntArray7483[i_489_];
                        int i_492_ = i_450_ + i_491_;
                        int i_493_ = (i_450_ & 0xff00ff) + (i_491_ & 0xff00ff);
                        i_491_ = (i_493_ & 0x1000100) + (i_492_ - i_493_ & 0x10000);
                        this.anIntArray7483[i_489_] = i_492_ - i_491_ | i_491_ - (i_491_ >>> 8);
                    }
                    i_489_++;
                }
                i_454_++;
                i_459_ += i_457_ + i_457_;
                i_458_ += i_457_++ + i_457_;
            }
        } else throw new IllegalArgumentException();
    }

    // dependency of method3712 (used by method3720), not in genuine list
    final void method3720(int i, int i_377_, int i_378_, int i_379_, int i_380_, int i_381_, int i_382_, int i_383_, int i_384_, int i_385_) {
        if (i_379_ != 0 && i_380_ != 0) {
            if (i_382_ != 65535 && !(this.textureSource.getMetrics(i_382_, -6662).disableable)) {
                if (anInt7512 != i_382_) {
                    Sprite sprite = ((Sprite) aClass60_7499.method583(i_382_, 97));
                    if (sprite == null) {
                        int[] is = method3719(i_382_);
                        if (is == null) return;
                        int i_386_ = (method3727(i_382_) ? 64 : this.anInt7501);
                        sprite = this.createSprite(i_386_, is, (byte) 94, 0, i_386_, i_386_);
                        aClass60_7499.method582(sprite, i_382_, (byte) -100);
                    }
                    anInt7512 = i_382_;
                    aSprite_7513 = sprite;
                }
                ((Sprite_Sub3) aSprite_7513).method996(i - i_379_, i_377_ - i_380_, i_378_, i_379_ << 1, i_380_ << 1, i_384_, i_383_, i_385_, 1);
            } else method3723(i, i_377_, i_378_, i_379_, i_383_, i_385_);
        }
    }

    final JavaThreadResource threadResource(Runnable runnable) {
        for (int i = 0; i < this.anInt7485; i++) {
            if (aJavaThreadResourceArray7480[i].aRunnable2198 == runnable) return aJavaThreadResourceArray7480[i];
        }
        return null;
    }

    final int E() {
        return 0;
    }

    final void ZA(int i, float f, float f_573_, float f_574_, float f_575_, float f_576_) {
        this.anInt7474 = (int) (f * 65535.0F);
        this.anInt7478 = (int) (f_573_ * 65535.0F);
        float f_577_ = (float) Math.sqrt(f_574_ * f_574_ + f_575_ * f_575_ + f_576_ * f_576_);
        this.anInt7484 = (int) (f_574_ * 65535.0F / f_577_);
        this.anInt7473 = (int) (f_575_ * 65535.0F / f_577_);
        this.anInt7479 = (int) (f_576_ * 65535.0F / f_577_);
    }

    final boolean method3725(int i) {
        return this.textureSource.method4(-7953, i);
    }

    private JavaToolkit(TextureSource var_textureSource) {
        super(var_textureSource);
        aIterableHashTable_7467 = new IterableHashTable(4);
        this.anInt7474 = 45823;
        aBoolean7489 = false;
        anInt7487 = 0;
        this.anInt7501 = 128;
        this.anInt7476 = 0;
        this.anInt7482 = 50;
        this.anInt7503 = 0;
        this.anInt7496 = 0;
        this.anInt7497 = 512;
        this.anInt7500 = 75518;
        this.anInt7491 = 512;
        anInt7505 = 0;
        this.anInt7494 = 3500;
        this.anInt7507 = 0;
        this.anInt7478 = 78642;
        aClass60_7499 = new Class60(16);
        anInt7512 = -1;
        try {
            aClass60_7498 = new Class60(256);
            this.aClass101_Sub1_7492 = new Matrix_Sub1();
            method3631(1);
            method3659(0);
            Class59_Sub2_Sub1.method566(true, true, (byte) -126);
            aBoolean7471 = true;
            anInt7466 = (int) Class62.method599(-70);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            this.method3635((byte) -98);
            throw new RuntimeException("");
        }
    }

    final boolean method3708() {
        return true;
    }

    final za method3702(int i) {
        return null;
    }

    // dependency of method3665, not in genuine list
    final Sprite method3629(int i, int i_519_, boolean bool) {
        if (bool) return new JavaArgbSprite(this, i, i_519_);
        return new JavaRgbSprite(this, i, i_519_);
    }

    final Class348_Sub1 method3690(int i, int i_520_, int i_521_, int i_522_, int i_523_, float f) {
        return null;
    }

    final boolean method3666() {
        return false;
    }

    final Class299 method3706(Class299 class299, Class299 class299_524_, float f, Class299 class299_525_) {
        return null;
    }

    final int XA() {
        return this.anInt7494;
    }

    final boolean method3699() {
        return false;
    }

    final boolean method3671() {
        return false;
    }

    final Sprite method3711(int[] is, int i, int i_422_, int i_423_, int i_424_, boolean bool) {
        boolean bool_425_ = false;
        int i_426_ = i;
        while_229_:
        for (int i_427_ = 0; i_427_ < i_424_; i_427_++) {
            for (int i_428_ = 0; i_428_ < i_423_; i_428_++) {
                int i_429_ = is[i_426_++] >>> 24;
                if (i_429_ != 0 && i_429_ != 255) {
                    bool_425_ = true;
                    break while_229_;
                }
            }
        }
        if (bool_425_) return new JavaArgbSprite(this, is, i, i_422_, i_423_, i_424_, bool);
        return new JavaRgbSprite(this, is, i, i_422_, i_423_, i_424_, bool);
    }

    // dependency of method3665, not in genuine list
    final Interface3 method3665(int i, int i_591_) {
        return method3629(i, i_591_, false);
    }

    final void X(int i) {
        /* empty */
    }

    final void method3659(int i) {
        aJavaThreadResourceArray7480[i].method1291(10000, Thread.currentThread());
    }

    final int method3726(int i) {
        return this.textureSource.getMetrics(i, -6662).alphaBlendMode;
    }

    final int method3704() {
        return 0;
    }

    final boolean method3694() {
        return true;
    }

    final void da(int i, int i_636_, int i_637_, int[] is) {
        float f = ((this.aClass101_Sub1_7492.aFloat5681) + ((this.aClass101_Sub1_7492.aFloat5662) * (float) i + (this.aClass101_Sub1_7492.aFloat5680) * (float) i_636_ + (this.aClass101_Sub1_7492.aFloat5664) * (float) i_637_));
        if (f >= (float) this.anInt7482 && f <= (float) this.anInt7494) {
            int i_638_ = (int) ((float) this.anInt7491 * (this.aClass101_Sub1_7492.aFloat5686 + ((this.aClass101_Sub1_7492.aFloat5672) * (float) i + (this.aClass101_Sub1_7492.aFloat5673) * (float) i_636_ + (this.aClass101_Sub1_7492.aFloat5669) * (float) i_637_)) / f);
            int i_639_ = (int) ((float) this.anInt7497 * (this.aClass101_Sub1_7492.aFloat5685 + ((this.aClass101_Sub1_7492.aFloat5655) * (float) i + (this.aClass101_Sub1_7492.aFloat5678) * (float) i_636_ + (this.aClass101_Sub1_7492.aFloat5666) * (float) i_637_)) / f);
            if (i_638_ >= this.anInt7509 && i_638_ <= this.anInt7508 && i_639_ >= this.anInt7490 && i_639_ <= this.anInt7506) {
                is[0] = i_638_ - this.anInt7509;
                is[1] = i_639_ - this.anInt7490;
                is[2] = (int) f;
            } else is[0] = is[1] = is[2] = -1;
        } else is[0] = is[1] = is[2] = -1;
    }

    final void method3673() {
        /* empty */
    }

    final boolean method3644() {
        return false;
    }

    final int method3679(int i, int i_640_) {
        return i | i_640_;
    }

    final boolean method3727(int i) {
        return aBoolean7489 || this.textureSource.getMetrics(i, -6662).small;
    }
}
