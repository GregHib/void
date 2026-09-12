package world.gregs.voidps.tools.inv.item;/* Class64_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class JavaModel extends Model {
    static int anInt5346 = 4096;
    static int anInt5350 = 4096;
    private short[] faceColour;
    private int[] vertexZ;
    private Class360[] aClass360Array5313;
    private float[][] texCoordU;
    private int[] anIntArray5315;
    private int functionMask;
    private short[] faceA;
    private int[] anIntArray5318;
    private Matrix_Sub1 aClass101_Sub1_5320;
    private int[] anIntArray5321;
    private ModelParticleEmitter[] emitters;
    private boolean aBoolean5323 = false;
    private short aShort5324;
    private byte[] faceAlpha;
    private int[] anIntArray5326;
    private short[] faceIndices;
    private short aShort5329;
    private int[][] faceLabels;
    private short aShort5331;
    private int[] vertexY;
    private short[] aShortArray5333;
    private int[][] vertexLabels;
    private ModelParticleEffector[] effectors;
    private int[] anIntArray5337;
    private int anInt5338;
    private int vertexCount = 0;
    private int anInt5342;
    private int anInt5375;
    private int[] anIntArray5343;
    private int ambient;
    private float[][] texCoordV;
    private short aShort5348;
    private int contrast;
    private int faceCount;
    private short aShort5352;
    private final JavaToolkit toolkit;
    private int anInt5354;
    private int[] anIntArray5355;
    private int[] vertexX;
    private boolean aBoolean5357 = false;
    private byte[] facePriority;
    private int[] anIntArray5359;
    private Class360[] aClass360Array5360;
    private JavaBillboardFace[] billboardFaces;
    private int[] anIntArray5362;
    private JavaBillboardAttributes[] billboardAttributes;
    private short[] faceC;
    private short aShort5365;
    private int[] anIntArray5366;
    private JavaThreadResource aJavaThreadResource_5367;
    private int[] anIntArray5368;
    private boolean aBoolean5369;
    private short[] aShortArray5370;
    private int[] anIntArray5371;
    private boolean aBoolean5372;
    private int[] anIntArray5373;
    private int[] anIntArray5377;
    private int[][] billboardLabels;
    private int[] anIntArray5381;
    private boolean transparent;
    private Rasterizer rasterizer;
    private int[] anIntArray5384;
    private Class41[] aClass41Array5385;
    private byte[] shadingType;
    private int maxVertex;
    private short[] faceTextures;
    private int billboardCount;
    private boolean movingTextures;
    private int[] anIntArray5392;
    private short aShort5393;
    private short[] faceB;
    private short aShort5395;
    private int[] anIntArray5398;
    private int[] anIntArray5399;
    private int[] anIntArray5400;

    private final boolean method629(int i) {
        if (faceAlpha == null) return false;
        return faceAlpha[i] != 0;
    }

    // dependency of method645, not in genuine list
    private final boolean method630(int i, int i_0_, int i_1_, int i_2_, int i_3_, int i_4_, int i_5_, int i_6_) {
        if (i_0_ < i_1_ && i_0_ < i_2_ && i_0_ < i_3_) return false;
        if (i_0_ > i_1_ && i_0_ > i_2_ && i_0_ > i_3_) return false;
        if (i < i_4_ && i < i_5_ && i < i_6_) return false;
        return i <= i_4_ || i <= i_5_ || i <= i_6_;
    }

    private final void method631() {
        aClass360Array5360 = null;
        aClass360Array5313 = null;
        aClass41Array5385 = null;
        aBoolean5323 = false;
    }

    final int EA() {
        if (!aBoolean5323) method655();
        return aShort5365;
    }

    private final void method632(Thread thread) {
        JavaThreadResource javaThreadResource = toolkit.threadResource(thread);
        rasterizer = javaThreadResource.rasterizer;
        if (javaThreadResource != aJavaThreadResource_5367) {
            aJavaThreadResource_5367 = javaThreadResource;
            anIntArray5362 = aJavaThreadResource_5367.anIntArray2222;
            anIntArray5399 = aJavaThreadResource_5367.anIntArray2244;
            anIntArray5384 = aJavaThreadResource_5367.anIntArray2214;
            anIntArray5392 = aJavaThreadResource_5367.anIntArray2237;
            anIntArray5321 = aJavaThreadResource_5367.anIntArray2234;
            anIntArray5343 = aJavaThreadResource_5367.anIntArray2230;
            anIntArray5355 = aJavaThreadResource_5367.anIntArray2213;
            anIntArray5359 = aJavaThreadResource_5367.anIntArray2218;
            anIntArray5373 = aJavaThreadResource_5367.anIntArray2241;
            anIntArray5398 = aJavaThreadResource_5367.anIntArray2245;
            anIntArray5315 = aJavaThreadResource_5367.anIntArray2238;
            anIntArray5371 = aJavaThreadResource_5367.anIntArray2247;
            anIntArray5381 = aJavaThreadResource_5367.anIntArray2235;
            anIntArray5377 = aJavaThreadResource_5367.anIntArray2240;
            anIntArray5326 = aJavaThreadResource_5367.anIntArray2236;
            anIntArray5318 = aJavaThreadResource_5367.anIntArray2216;
            anIntArray5400 = aJavaThreadResource_5367.anIntArray2242;
        }
    }

    private final void method634(boolean bool) {
        if (toolkit.anInt7485 > 1) {
            synchronized (this) {
                method657(bool);
            }
        } else method657(bool);
    }

    final ModelParticleEmitter[] method619() {
        return emitters;
    }

    final int V() {
        if (!aBoolean5323) method655();
        return aShort5395;
    }

    private final void method635(int i) {
        short i_39_ = faceA[i];
        short i_40_ = faceB[i];
        short i_41_ = faceC[i];
        if (faceTextures == null || faceTextures[i] == -1) {
            if (faceAlpha == null) rasterizer.anInt1674 = 0;
            else rasterizer.anInt1674 = faceAlpha[i] & 0xff;
            if (anIntArray5366[i] == -1)
                rasterizer.method1018((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], (ItemSpriteCacheKey.HSV_TO_RGB[anIntArray5368[i] & 0xffff]));
            else
                rasterizer.method1022((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], (float) (anIntArray5368[i] & 0xffff), (float) (anIntArray5337[i] & 0xffff), (float) (anIntArray5366[i] & 0xffff));
        } else {
            int i_42_ = -16777216;
            if (faceAlpha != null) i_42_ = 255 - (faceAlpha[i] & 0xff) << 24;
            if (anIntArray5366[i] == -1) {
                int i_43_ = i_42_ | anIntArray5368[i] & 0xffffff;
                rasterizer.method1024((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_43_, i_43_, i_43_, aJavaThreadResource_5367.anInt2192, 0, 0, 0, faceTextures[i]);
            } else
                rasterizer.method1024((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_42_ | anIntArray5368[i] & 0xffffff, i_42_ | anIntArray5337[i] & 0xffffff, i_42_ | anIntArray5366[i] & 0xffffff, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
        }
    }

    final void render(Matrix matrix, Class318_Sub3 class318_sub3, int i) {
        method654(matrix, class318_sub3, -1, i);
    }

    final int HA() {
        if (!aBoolean5323) method655();
        return aShort5352;
    }

    private final void method636() {
        if (anInt5354 == 0 && aClass360Array5360 == null) {
            if (toolkit.anInt7485 > 1) {
                synchronized (this) {
                    method649();
                }
            } else method649();
        }
    }

    final int fa() {
        if (!aBoolean5323) method655();
        return aShort5329;
    }

    private final int method637(int i, int i_283_) {
        i_283_ = i_283_ * (i & 0x7f) >> 7;
        if (i_283_ < 2) i_283_ = 2;
        else if (i_283_ > 126) i_283_ = 126;
        return (i & 0xff80) + i_283_;
    }

    private final boolean method638(int i) {
        if (anIntArray5400 == null) return false;
        return anIntArray5400[i] != -1;
    }

    final int da() {
        return contrast;
    }

    final boolean loadedTextures() {
        if (faceTextures == null) return true;
        for (int i = 0; i < faceTextures.length; i++) {
            if (faceTextures[i] != -1 && !toolkit.method3725(faceTextures[i])) return false;
        }
        return true;
    }

    final void method612() {
        /* empty */
    }

    private final int method642(int i, short i_305_, int i_306_) {
        int i_307_ = Class10.anIntArray179[method637(i, i_306_)];
        TextureMetrics textureMetrics = toolkit.textureSource.getMetrics(i_305_ & 0xffff, -6662);
        int i_308_ = textureMetrics.alpha & 0xff;
        if (i_308_ != 0) {
            int i_309_ = 131586 * i_306_;
            if (i_308_ == 256) i_307_ = i_309_;
            else {
                int i_310_ = i_308_;
                int i_311_ = 256 - i_308_;
                i_307_ = ((((i_309_ & 0xff00ff) * i_310_ + (i_307_ & 0xff00ff) * i_311_) & ~0xff00ff) + (((i_309_ & 0xff00) * i_310_ + (i_307_ & 0xff00) * i_311_) & 0xff0000)) >> 8;
            }
        }
        int i_312_ = textureMetrics.aByte216 & 0xff;
        if (i_312_ != 0) {
            i_312_ += 256;
            int i_313_ = ((i_307_ & 0xff0000) >> 16) * i_312_;
            if (i_313_ > 65535) i_313_ = 65535;
            int i_314_ = ((i_307_ & 0xff00) >> 8) * i_312_;
            if (i_314_ > 65535) i_314_ = 65535;
            int i_315_ = (i_307_ & 0xff) * i_312_;
            if (i_315_ > 65535) i_315_ = 65535;
            i_307_ = (i_313_ << 8 & 0xff0000) + (i_314_ & 0xff00) + (i_315_ >> 8);
        }
        return i_307_;
    }

    final void method608(Matrix matrix, Class318_Sub3 class318_sub3, int i, int i_316_) {
        method654(matrix, class318_sub3, i, i_316_);
    }

    final boolean NA() {
        if (vertexLabels == null) return false;
        anInt5338 = 0;
        anInt5375 = 0;
        anInt5342 = 0;
        return true;
    }

    private final void method643(int i, boolean bool, boolean bool_317_) {
        if (anIntArray5366[i] != -2) {
            short i_318_ = faceA[i];
            short i_319_ = faceB[i];
            short i_320_ = faceC[i];
            int i_321_ = anIntArray5321[i_318_];
            int i_322_ = anIntArray5321[i_319_];
            int i_323_ = anIntArray5321[i_320_];
            if (bool && (i_321_ == -5000 || i_322_ == -5000 || i_323_ == -5000)) {
                int i_324_ = anIntArray5399[i_318_];
                int i_325_ = anIntArray5399[i_319_];
                int i_326_ = anIntArray5399[i_320_];
                int i_327_ = anIntArray5384[i_318_];
                int i_328_ = anIntArray5384[i_319_];
                int i_329_ = anIntArray5384[i_320_];
                int i_330_ = anIntArray5392[i_318_];
                int i_331_ = anIntArray5392[i_319_];
                int i_332_ = anIntArray5392[i_320_];
                i_324_ -= i_325_;
                i_326_ -= i_325_;
                i_327_ -= i_328_;
                i_329_ -= i_328_;
                i_330_ -= i_331_;
                i_332_ -= i_331_;
                int i_333_ = i_327_ * i_332_ - i_330_ * i_329_;
                int i_334_ = i_330_ * i_326_ - i_324_ * i_332_;
                int i_335_ = i_324_ * i_329_ - i_327_ * i_326_;
                if (i_325_ * i_333_ + i_328_ * i_334_ + i_331_ * i_335_ > 0) method646(i);
            } else if (anIntArray5400[i] != -1 || ((i_321_ - i_322_) * (anIntArray5343[i_320_] - anIntArray5343[i_319_]) - ((anIntArray5343[i_318_] - anIntArray5343[i_319_]) * (i_323_ - i_322_))) > 0) {
                rasterizer.clamp = i_321_ < 0 || i_322_ < 0 || i_323_ < 0 || i_321_ > aJavaThreadResource_5367.anInt2221 || i_322_ > aJavaThreadResource_5367.anInt2221 || i_323_ > aJavaThreadResource_5367.anInt2221;
                if (bool_317_) {
                    int i_336_ = anIntArray5400[i];
                    if (i_336_ == -1 || !billboardFaces[i_336_].aBoolean145) method658(i);
                } else {
                    int i_337_ = anIntArray5400[i];
                    if (i_337_ != -1) {
                        JavaBillboardFace javaBillboardFace = billboardFaces[i_337_];
                        JavaBillboardAttributes javaBillboardAttributes = billboardAttributes[i_337_];
                        if (!javaBillboardFace.aBoolean145) method635(i);
                        toolkit.method3720(javaBillboardAttributes.anInt4312, javaBillboardAttributes.anInt4310, javaBillboardAttributes.anInt4320, javaBillboardAttributes.anInt4309, javaBillboardAttributes.anInt4307, javaBillboardAttributes.anInt4308, javaBillboardFace.aShort146 & 0xffff, javaBillboardAttributes.anInt4313, javaBillboardFace.aByte148, javaBillboardFace.aByte156);
                    } else method635(i);
                }
            }
        }
    }

    final boolean F() {
        return transparent;
    }

    final int na() {
        if (!aBoolean5323) method655();
        return aShort5324;
    }

    final r ba(r var_r) {
        return null;
    }

    // dependency of method643, not in genuine list
    private final void method646(int i) {
        int i_540_ = 0;
        int i_541_ = toolkit.anInt7482;
        short i_542_ = faceA[i];
        short i_543_ = faceB[i];
        short i_544_ = faceC[i];
        int i_545_ = anIntArray5392[i_542_];
        int i_546_ = anIntArray5392[i_543_];
        int i_547_ = anIntArray5392[i_544_];
        if (faceAlpha == null) rasterizer.anInt1674 = 0;
        else rasterizer.anInt1674 = faceAlpha[i] & 0xff;
        if (i_545_ >= i_541_) {
            anIntArray5315[i_540_] = anIntArray5321[i_542_];
            anIntArray5371[i_540_] = anIntArray5343[i_542_];
            anIntArray5381[i_540_] = anIntArray5355[i_542_];
            anIntArray5377[i_540_++] = anIntArray5368[i] & 0xffff;
        } else {
            int i_548_ = anIntArray5399[i_542_];
            int i_549_ = anIntArray5384[i_542_];
            int i_550_ = anIntArray5368[i] & 0xffff;
            if (i_547_ >= i_541_) {
                int i_551_ = (i_541_ - i_545_) * (65536 / (i_547_ - i_545_));
                anIntArray5315[i_540_] = (aJavaThreadResource_5367.anInt2229 + ((i_548_ + ((anIntArray5399[i_544_] - i_548_) * i_551_ >> 16)) * toolkit.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aJavaThreadResource_5367.anInt2215 + ((i_549_ + ((anIntArray5384[i_544_] - i_549_) * i_551_ >> 16)) * toolkit.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_550_ + (((anIntArray5366[i] & 0xffff) - i_550_) * i_551_ >> 16));
            }
            if (i_546_ >= i_541_) {
                int i_552_ = (i_541_ - i_545_) * (65536 / (i_546_ - i_545_));
                anIntArray5315[i_540_] = (aJavaThreadResource_5367.anInt2229 + ((i_548_ + ((anIntArray5399[i_543_] - i_548_) * i_552_ >> 16)) * toolkit.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aJavaThreadResource_5367.anInt2215 + ((i_549_ + ((anIntArray5384[i_543_] - i_549_) * i_552_ >> 16)) * toolkit.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_550_ + (((anIntArray5337[i] & 0xffff) - i_550_) * i_552_ >> 16));
            }
        }
        if (i_546_ >= i_541_) {
            anIntArray5315[i_540_] = anIntArray5321[i_543_];
            anIntArray5371[i_540_] = anIntArray5343[i_543_];
            anIntArray5381[i_540_] = anIntArray5355[i_543_];
            anIntArray5377[i_540_++] = anIntArray5337[i] & 0xffff;
        } else {
            int i_553_ = anIntArray5399[i_543_];
            int i_554_ = anIntArray5384[i_543_];
            int i_555_ = anIntArray5337[i] & 0xffff;
            if (i_545_ >= i_541_) {
                int i_556_ = (i_541_ - i_546_) * (65536 / (i_545_ - i_546_));
                anIntArray5315[i_540_] = (aJavaThreadResource_5367.anInt2229 + ((i_553_ + ((anIntArray5399[i_542_] - i_553_) * i_556_ >> 16)) * toolkit.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aJavaThreadResource_5367.anInt2215 + ((i_554_ + ((anIntArray5384[i_542_] - i_554_) * i_556_ >> 16)) * toolkit.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_555_ + (((anIntArray5368[i] & 0xffff) - i_555_) * i_556_ >> 16));
            }
            if (i_547_ >= i_541_) {
                int i_557_ = (i_541_ - i_546_) * (65536 / (i_547_ - i_546_));
                anIntArray5315[i_540_] = (aJavaThreadResource_5367.anInt2229 + ((i_553_ + ((anIntArray5399[i_544_] - i_553_) * i_557_ >> 16)) * toolkit.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aJavaThreadResource_5367.anInt2215 + ((i_554_ + ((anIntArray5384[i_544_] - i_554_) * i_557_ >> 16)) * toolkit.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_555_ + (((anIntArray5366[i] & 0xffff) - i_555_) * i_557_ >> 16));
            }
        }
        if (i_547_ >= i_541_) {
            anIntArray5315[i_540_] = anIntArray5321[i_544_];
            anIntArray5371[i_540_] = anIntArray5343[i_544_];
            anIntArray5381[i_540_] = anIntArray5355[i_544_];
            anIntArray5377[i_540_++] = anIntArray5366[i] & 0xffff;
        } else {
            int i_558_ = anIntArray5399[i_544_];
            int i_559_ = anIntArray5384[i_544_];
            int i_560_ = anIntArray5366[i] & 0xffff;
            if (i_546_ >= i_541_) {
                int i_561_ = (i_541_ - i_547_) * (65536 / (i_546_ - i_547_));
                anIntArray5315[i_540_] = (aJavaThreadResource_5367.anInt2229 + ((i_558_ + ((anIntArray5399[i_543_] - i_558_) * i_561_ >> 16)) * toolkit.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aJavaThreadResource_5367.anInt2215 + ((i_559_ + ((anIntArray5384[i_543_] - i_559_) * i_561_ >> 16)) * toolkit.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_560_ + (((anIntArray5337[i] & 0xffff) - i_560_) * i_561_ >> 16));
            }
            if (i_545_ >= i_541_) {
                int i_562_ = (i_541_ - i_547_) * (65536 / (i_545_ - i_547_));
                anIntArray5315[i_540_] = (aJavaThreadResource_5367.anInt2229 + ((i_558_ + ((anIntArray5399[i_542_] - i_558_) * i_562_ >> 16)) * toolkit.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aJavaThreadResource_5367.anInt2215 + ((i_559_ + ((anIntArray5384[i_542_] - i_559_) * i_562_ >> 16)) * toolkit.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_560_ + (((anIntArray5368[i] & 0xffff) - i_560_) * i_562_ >> 16));
            }
        }
        int i_563_ = anIntArray5315[0];
        int i_564_ = anIntArray5315[1];
        int i_565_ = anIntArray5315[2];
        int i_566_ = anIntArray5371[0];
        int i_567_ = anIntArray5371[1];
        int i_568_ = anIntArray5371[2];
        i_545_ = anIntArray5381[0];
        i_546_ = anIntArray5381[1];
        i_547_ = anIntArray5381[2];
        rasterizer.clamp = false;
        if (i_540_ == 3) {
            if (i_563_ < 0 || i_564_ < 0 || i_565_ < 0 || i_563_ > aJavaThreadResource_5367.anInt2221 || i_564_ > aJavaThreadResource_5367.anInt2221 || i_565_ > aJavaThreadResource_5367.anInt2221) rasterizer.clamp = true;
            if (faceTextures == null || faceTextures[i] == -1) {
                if (anIntArray5366[i] == -1) rasterizer.method1018((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, (ItemSpriteCacheKey.HSV_TO_RGB[anIntArray5368[i] & 0xffff]));
                else rasterizer.method1022((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, (float) anIntArray5377[0], (float) anIntArray5377[1], (float) anIntArray5377[2]);
            } else {
                int i_569_ = -16777216;
                if (faceAlpha != null) i_569_ = 255 - (faceAlpha[i] & 0xff) << 24;
                int i_570_ = i_569_ | anIntArray5368[i] & 0xffffff;
                if (anIntArray5366[i] == -1)
                    rasterizer.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_570_, i_570_, i_570_, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
                else
                    rasterizer.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_570_, i_570_, i_570_, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
            }
        }
        if (i_540_ == 4) {
            if (i_563_ < 0 || i_564_ < 0 || i_565_ < 0 || i_563_ > aJavaThreadResource_5367.anInt2221 || i_564_ > aJavaThreadResource_5367.anInt2221 || i_565_ > aJavaThreadResource_5367.anInt2221 || anIntArray5315[3] < 0 || anIntArray5315[3] > aJavaThreadResource_5367.anInt2221) rasterizer.clamp = true;
            if (faceTextures == null || faceTextures[i] == -1) {
                if (anIntArray5366[i] == -1) {
                    int i_571_ = ItemSpriteCacheKey.HSV_TO_RGB[anIntArray5368[i] & 0xffff];
                    rasterizer.method1018((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, i_571_);
                    rasterizer.method1018((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_546_, (float) anIntArray5381[3], i_571_);
                } else {
                    rasterizer.method1022((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, (float) anIntArray5377[0], (float) anIntArray5377[1], (float) anIntArray5377[2]);
                    rasterizer.method1022((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_546_, (float) anIntArray5381[3], (float) anIntArray5377[0], (float) anIntArray5377[2], (float) anIntArray5377[3]);
                }
            } else {
                int i_572_ = -16777216;
                if (faceAlpha != null) i_572_ = 255 - (faceAlpha[i] & 0xff) << 24;
                int i_573_ = i_572_ | anIntArray5368[i] & 0xffffff;
                if (anIntArray5366[i] == -1) {
                    rasterizer.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_573_, i_573_, i_573_, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
                    rasterizer.method1024((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_547_, (float) anIntArray5381[3], texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_573_, i_573_, i_573_, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
                } else {
                    rasterizer.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_573_, i_573_, i_573_, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
                    rasterizer.method1024((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_547_, (float) anIntArray5381[3], texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_573_, i_573_, i_573_, (aJavaThreadResource_5367.anInt2192), 0, 0, 0, faceTextures[i]);
                }
            }
        }
    }

    // dependency of method657, not in genuine list
    private final void method647() {
        if (anInt5354 == 0) method634(false);
        else if (toolkit.anInt7485 > 1) {
            synchronized (this) {
                method640();
            }
        } else method640();
    }

    // dependency of method647, not in genuine list
    private final void method640() {
        for (int i = 0; i < faceCount; i++) {
            short i_287_ = faceTextures != null ? faceTextures[i] : (short) -1;
            if (i_287_ == -1) {
                int i_288_ = faceColour[i] & 0xffff;
                int i_289_ = (i_288_ & 0x7f) * ambient >> 7;
                short i_290_ = Class25.method303(i_288_ & ~0x7f | i_289_, 30);
                if (anIntArray5366[i] == -1) {
                    int i_291_ = anIntArray5368[i] & ~0x1ffff;
                    anIntArray5368[i] = i_291_ | method2198(0, i_291_ >> 17, i_290_);
                } else if (anIntArray5366[i] != -2) {
                    int i_292_ = anIntArray5368[i] & ~0x1ffff;
                    anIntArray5368[i] = i_292_ | method2198(0, i_292_ >> 17, i_290_);
                    i_292_ = anIntArray5337[i] & ~0x1ffff;
                    anIntArray5337[i] = i_292_ | method2198(0, i_292_ >> 17, i_290_);
                    i_292_ = anIntArray5366[i] & ~0x1ffff;
                    anIntArray5366[i] = i_292_ | method2198(0, i_292_ >> 17, i_290_);
                }
            }
        }
        anInt5354 = 2;
    }

    static final int method2198(int i, int i_0_, int i_1_) {
        i_0_ = i_0_ * (i_1_ & 0x7f) >> 7;
        if (i != 0) method2198(52, -11, 108);
//        anInt3741++;
        if (i_0_ >= 2) {
            if (i_0_ > 126) i_0_ = 126;
        } else i_0_ = 2;
        return (0xff80 & i_1_) - -i_0_;
    }

    final void C(int i) {
        if ((functionMask & 0x1000) != 4096) throw new IllegalStateException();
        ambient = i;
        anInt5354 = 0;
    }

    final int RA() {
        if (!aBoolean5323) method655();
        return aShort5393;
    }

    final boolean method623(int i, int i_474_, Matrix matrix, boolean bool, int i_475_, int i_476_) {
        return method645(i, i_474_, matrix, bool, i_475_, i_476_);
    }

    final ModelParticleEffector[] method604() {
        return effectors;
    }

    final int WA() {
        return ambient;
    }

    final void method621() {
        if (toolkit.anInt7485 > 1) {
            synchronized (this) {
                this.aBoolean1124 = false;
                this.notifyAll();
            }
        }
    }

    final boolean method628(int i, int i_486_, Matrix matrix, boolean bool, int i_487_) {
        return method645(i, i_486_, matrix, bool, i_487_, -1);
    }

    final int ma() {
        if (!aBoolean5323) method655();
        return aShort5348;
    }

    // dependency of method623 and method628, not in genuine list
    private final boolean method645(int i, int i_488_, Matrix matrix, boolean bool, int i_489_, int i_490_) {
        aClass101_Sub1_5320 = (Matrix_Sub1) matrix;
        Matrix_Sub1 class101_sub1 = toolkit.aClass101_Sub1_7492;
        float f = (class101_sub1.aFloat5686 + ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320.aFloat5681)));
        float f_491_ = (class101_sub1.aFloat5685 + ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320.aFloat5681)));
        float f_492_ = (class101_sub1.aFloat5681 + ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320.aFloat5681)));
        float f_493_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5672) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320.aFloat5655) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320.aFloat5662));
        float f_494_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320.aFloat5678) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320.aFloat5680));
        float f_495_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5669) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320.aFloat5666) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320.aFloat5664));
        float f_496_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5672) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320.aFloat5655) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320.aFloat5662));
        float f_497_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320.aFloat5678) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320.aFloat5680));
        float f_498_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5669) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320.aFloat5666) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320.aFloat5664));
        float f_499_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5672) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320.aFloat5655) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320.aFloat5662));
        float f_500_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320.aFloat5678) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320.aFloat5680));
        float f_501_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5669) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320.aFloat5666) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320.aFloat5664));
        boolean bool_502_ = false;
        int i_503_ = toolkit.anInt7510;
        int i_504_ = toolkit.anInt7504;
        int i_505_ = toolkit.anInt7491;
        int i_506_ = toolkit.anInt7497;
        int i_507_ = 2147483647;
        int i_508_ = -2147483648;
        int i_509_ = 2147483647;
        int i_510_ = -2147483648;
        method632(Thread.currentThread());
        if (!aBoolean5323) method655();
        int i_511_ = aShort5393 - aShort5395 >> 1;
        int i_512_ = aShort5365 - aShort5329 >> 1;
        int i_513_ = aShort5331 - aShort5352 >> 1;
        int i_514_ = aShort5395 + i_511_;
        int i_515_ = aShort5329 + i_512_;
        int i_516_ = aShort5352 + i_513_;
        int i_517_ = i_514_ - (i_511_ << i_489_);
        int i_518_ = i_515_ - (i_512_ << i_489_);
        int i_519_ = i_516_ - (i_513_ << i_489_);
        int i_520_ = i_514_ + (i_511_ << i_489_);
        int i_521_ = i_515_ + (i_512_ << i_489_);
        int i_522_ = i_516_ + (i_513_ << i_489_);
        anIntArray5359[0] = i_517_;
        anIntArray5373[0] = i_518_;
        anIntArray5398[0] = i_519_;
        anIntArray5359[1] = i_520_;
        anIntArray5373[1] = i_518_;
        anIntArray5398[1] = i_519_;
        anIntArray5359[2] = i_517_;
        anIntArray5373[2] = i_521_;
        anIntArray5398[2] = i_519_;
        anIntArray5359[3] = i_520_;
        anIntArray5373[3] = i_521_;
        anIntArray5398[3] = i_519_;
        anIntArray5359[4] = i_517_;
        anIntArray5373[4] = i_518_;
        anIntArray5398[4] = i_522_;
        anIntArray5359[5] = i_520_;
        anIntArray5373[5] = i_518_;
        anIntArray5398[5] = i_522_;
        anIntArray5359[6] = i_517_;
        anIntArray5373[6] = i_521_;
        anIntArray5398[6] = i_522_;
        anIntArray5359[7] = i_520_;
        anIntArray5373[7] = i_521_;
        anIntArray5398[7] = i_522_;
        for (int i_523_ = 0; i_523_ < 8; i_523_++) {
            int i_524_ = anIntArray5359[i_523_];
            int i_525_ = anIntArray5373[i_523_];
            int i_526_ = anIntArray5398[i_523_];
            float f_527_ = f + (f_493_ * (float) i_524_ + f_494_ * (float) i_525_ + f_495_ * (float) i_526_);
            float f_528_ = f_491_ + (f_496_ * (float) i_524_ + f_497_ * (float) i_525_ + f_498_ * (float) i_526_);
            float f_529_ = f_492_ + (f_499_ * (float) i_524_ + f_500_ * (float) i_525_ + f_501_ * (float) i_526_);
            if (f_529_ >= (float) toolkit.anInt7482) {
                if (i_490_ > 0) f_529_ = (float) i_490_;
                int i_530_ = i_503_ + (int) (f_527_ * (float) i_505_ / f_529_);
                int i_531_ = i_504_ + (int) (f_528_ * (float) i_506_ / f_529_);
                if (i_530_ < i_507_) i_507_ = i_530_;
                if (i_530_ > i_508_) i_508_ = i_530_;
                if (i_531_ < i_509_) i_509_ = i_531_;
                if (i_531_ > i_510_) i_510_ = i_531_;
                bool_502_ = true;
            }
        }
        if (bool_502_ && i > i_507_ && i < i_508_ && i_488_ > i_509_ && i_488_ < i_510_) {
            if (bool) return true;
            for (int i_532_ = 0; i_532_ < vertexCount; i_532_++) {
                int i_533_ = vertexX[i_532_];
                int i_534_ = vertexY[i_532_];
                int i_535_ = vertexZ[i_532_];
                float f_536_ = f + (f_493_ * (float) i_533_ + f_494_ * (float) i_534_ + f_495_ * (float) i_535_);
                float f_537_ = f_491_ + (f_496_ * (float) i_533_ + f_497_ * (float) i_534_ + f_498_ * (float) i_535_);
                float f_538_ = f_492_ + (f_499_ * (float) i_533_ + f_500_ * (float) i_534_ + f_501_ * (float) i_535_);
                if (f_538_ >= (float) toolkit.anInt7482) {
                    if (i_490_ > 0) f_538_ = (float) i_490_;
                    anIntArray5321[i_532_] = i_503_ + (int) (f_536_ * (float) i_505_ / f_538_);
                    anIntArray5343[i_532_] = i_504_ + (int) (f_537_ * (float) i_506_ / f_538_);
                } else anIntArray5321[i_532_] = -999999;
            }
            for (int i_539_ = 0; i_539_ < faceCount; i_539_++) {
                if (anIntArray5321[faceA[i_539_]] != -999999 && anIntArray5321[faceB[i_539_]] != -999999 && anIntArray5321[faceC[i_539_]] != -999999 && method630(i, i_488_, anIntArray5343[faceA[i_539_]], anIntArray5343[faceB[i_539_]], anIntArray5343[faceC[i_539_]], anIntArray5321[faceA[i_539_]], anIntArray5321[faceB[i_539_]], anIntArray5321[faceC[i_539_]]))
                    return true;
            }
        }
        return false;
    }

    final void LA(int i) {
        if ((functionMask & 0x2000) != 8192) throw new IllegalStateException();
        contrast = i;
        anInt5354 = 0;
    }

    private final void method649() {
        aClass360Array5360 = new Class360[maxVertex];
        for (int i = 0; i < maxVertex; i++)
            aClass360Array5360[i] = new Class360();
        for (int i = 0; i < faceCount; i++) {
            short i_599_ = faceA[i];
            short i_600_ = faceB[i];
            short i_601_ = faceC[i];
            int i_602_ = vertexX[i_600_] - vertexX[i_599_];
            int i_603_ = vertexY[i_600_] - vertexY[i_599_];
            int i_604_ = vertexZ[i_600_] - vertexZ[i_599_];
            int i_605_ = vertexX[i_601_] - vertexX[i_599_];
            int i_606_ = vertexY[i_601_] - vertexY[i_599_];
            int i_607_ = vertexZ[i_601_] - vertexZ[i_599_];
            int i_608_ = i_603_ * i_607_ - i_606_ * i_604_;
            int i_609_ = i_604_ * i_605_ - i_607_ * i_602_;
            int i_610_;
            for (i_610_ = i_602_ * i_606_ - i_605_ * i_603_; (i_608_ > 8192 || i_609_ > 8192 || i_610_ > 8192 || i_608_ < -8192 || i_609_ < -8192 || i_610_ < -8192); i_610_ >>= 1) {
                i_608_ >>= 1;
                i_609_ >>= 1;
            }
            int i_611_ = (int) Math.sqrt(i_608_ * i_608_ + i_609_ * i_609_ + i_610_ * i_610_);
            if (i_611_ <= 0) i_611_ = 1;
            i_608_ = i_608_ * 256 / i_611_;
            i_609_ = i_609_ * 256 / i_611_;
            i_610_ = i_610_ * 256 / i_611_;
            byte i_612_;
            if (shadingType == null) i_612_ = (byte) 0;
            else i_612_ = shadingType[i];
            if (i_612_ == 0) {
                Class360 class360 = aClass360Array5360[i_599_];
                class360.anInt4430 += i_608_;
                class360.anInt4428 += i_609_;
                class360.anInt4427 += i_610_;
                class360.anInt4429++;
                class360 = aClass360Array5360[i_600_];
                class360.anInt4430 += i_608_;
                class360.anInt4428 += i_609_;
                class360.anInt4427 += i_610_;
                class360.anInt4429++;
                class360 = aClass360Array5360[i_601_];
                class360.anInt4430 += i_608_;
                class360.anInt4428 += i_609_;
                class360.anInt4427 += i_610_;
                class360.anInt4429++;
            } else if (i_612_ == 1) {
                if (aClass41Array5385 == null) aClass41Array5385 = new Class41[faceCount];
                Class41 class41 = aClass41Array5385[i] = new Class41();
                class41.anInt561 = i_608_;
                class41.anInt560 = i_609_;
                class41.anInt559 = i_610_;
            }
        }
    }

    private final void method650(boolean bool, boolean bool_616_, int i, int i_617_) {
        if (billboardFaces != null) {
            for (int i_618_ = 0; i_618_ < billboardCount; i_618_++) {
                JavaBillboardFace javaBillboardFace = billboardFaces[i_618_];
                anIntArray5400[javaBillboardFace.anInt144] = i_618_;
            }
        }
        if (transparent || billboardFaces != null) {
            if ((functionMask & 0x100) == 0 && faceIndices != null) {
                for (int i_619_ = 0; i_619_ < faceCount; i_619_++) {
                    short i_620_ = faceIndices[i_619_];
                    method643(i_620_, bool, bool_616_);
                }
            } else {
                for (int i_621_ = 0; i_621_ < faceCount; i_621_++) {
                    if (!method629(i_621_) && !method638(i_621_)) method643(i_621_, bool, bool_616_);
                }
                if (facePriority == null) {
                    for (int i_622_ = 0; i_622_ < faceCount; i_622_++) {
                        if (method629(i_622_) || method638(i_622_)) method643(i_622_, bool, bool_616_);
                    }
                } else {
                    for (int i_623_ = 0; i_623_ < 12; i_623_++) {
                        for (int i_624_ = 0; i_624_ < faceCount; i_624_++) {
                            if (facePriority[i_624_] == i_623_ && (method629(i_624_) || method638(i_624_))) method643(i_624_, bool, bool_616_);
                        }
                    }
                }
            }
        } else {
            for (int i_625_ = 0; i_625_ < faceCount; i_625_++)
                method643(i_625_, bool, bool_616_);
        }
    }

    private final void method654(Matrix matrix, Class318_Sub3 class318_sub3, int i, int i_632_) {
        if (maxVertex >= 1) {
            aClass101_Sub1_5320 = (Matrix_Sub1) matrix;
            Matrix_Sub1 class101_sub1 = toolkit.aClass101_Sub1_7492;
            if (!aBoolean5323) method655();
            boolean bool = false;
            if (aClass101_Sub1_5320.aFloat5672 == 16384.0F && aClass101_Sub1_5320.aFloat5673 == 0.0F && aClass101_Sub1_5320.aFloat5669 == 0.0F && aClass101_Sub1_5320.aFloat5655 == 0.0F && aClass101_Sub1_5320.aFloat5678 == 16384.0F && aClass101_Sub1_5320.aFloat5666 == 0.0F && aClass101_Sub1_5320.aFloat5662 == 0.0F && aClass101_Sub1_5320.aFloat5680 == 0.0F && (aClass101_Sub1_5320.aFloat5664 == 16384.0F))
                bool = true;
            float f = (class101_sub1.aFloat5681 + (class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320.aFloat5681));
            float f_633_ = (bool ? class101_sub1.aFloat5680 : ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5680 * aClass101_Sub1_5320.aFloat5678) + (class101_sub1.aFloat5664 * aClass101_Sub1_5320.aFloat5680)));
            int i_634_ = (int) (f + (float) aShort5329 * f_633_);
            int i_635_ = (int) (f + (float) aShort5365 * f_633_);
            int i_636_;
            int i_637_;
            if (i_634_ > i_635_) {
                i_636_ = i_635_ - aShort5324;
                i_637_ = i_634_ + aShort5324;
            } else {
                i_636_ = i_634_ - aShort5324;
                i_637_ = i_635_ + aShort5324;
            }
            if (i_636_ < toolkit.anInt7494 && i_637_ > toolkit.anInt7482) {
                float f_638_ = (class101_sub1.aFloat5686 + (class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320.aFloat5681));
                float f_639_ = (bool ? class101_sub1.aFloat5673 : ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320.aFloat5678)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320.aFloat5680))));
                int i_640_ = (int) (f_638_ + (float) aShort5329 * f_639_);
                int i_641_ = (int) (f_638_ + (float) aShort5365 * f_639_);
                int i_642_;
                int i_643_;
                if (i_640_ > i_641_) {
                    i_642_ = ((i_641_ - aShort5324) * toolkit.anInt7491);
                    i_643_ = ((i_640_ + aShort5324) * toolkit.anInt7491);
                } else {
                    i_642_ = ((i_640_ - aShort5324) * toolkit.anInt7491);
                    i_643_ = ((i_641_ + aShort5324) * toolkit.anInt7491);
                }
                if (i == -1) {
                    if (i_642_ / i_637_ >= toolkit.anInt7508 || (i_643_ / i_637_ <= toolkit.anInt7509)) return;
                } else if (i_642_ / i >= toolkit.anInt7508 || (i_643_ / i <= toolkit.anInt7509)) return;
                float f_644_ = (class101_sub1.aFloat5685 + (class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320.aFloat5681));
                float f_645_ = (bool ? class101_sub1.aFloat5678 : ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320.aFloat5678)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320.aFloat5680))));
                int i_646_ = (int) (f_644_ + (float) aShort5329 * f_645_);
                int i_647_ = (int) (f_644_ + (float) aShort5365 * f_645_);
                int i_648_;
                int i_649_;
                if (i_646_ > i_647_) {
                    i_648_ = ((i_647_ - aShort5324) * toolkit.anInt7497);
                    i_649_ = ((i_646_ + aShort5324) * toolkit.anInt7497);
                } else {
                    i_648_ = ((i_646_ - aShort5324) * toolkit.anInt7497);
                    i_649_ = ((i_647_ + aShort5324) * toolkit.anInt7497);
                }
                if (i == -1) {
                    if (i_648_ / i_637_ >= toolkit.anInt7506 || (i_649_ / i_637_ <= toolkit.anInt7490)) return;
                } else if (i_648_ / i >= toolkit.anInt7506 || (i_649_ / i <= toolkit.anInt7490)) return;
                float f_650_;
                float f_651_;
                float f_652_;
                float f_653_;
                float f_654_;
                float f_655_;
                if (bool) {
                    f_650_ = class101_sub1.aFloat5672;
                    f_651_ = class101_sub1.aFloat5655;
                    f_652_ = class101_sub1.aFloat5662;
                    f_653_ = class101_sub1.aFloat5669;
                    f_654_ = class101_sub1.aFloat5666;
                    f_655_ = class101_sub1.aFloat5664;
                } else {
                    f_650_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5672) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320.aFloat5655)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320.aFloat5662)));
                    f_651_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5672) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320.aFloat5655)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320.aFloat5662)));
                    f_652_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5672) + (class101_sub1.aFloat5680 * (aClass101_Sub1_5320.aFloat5655)) + (class101_sub1.aFloat5664 * (aClass101_Sub1_5320.aFloat5662)));
                    f_653_ = ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5669) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320.aFloat5666)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320.aFloat5664)));
                    f_654_ = ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5669) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320.aFloat5666)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320.aFloat5664)));
                    f_655_ = ((class101_sub1.aFloat5662 * aClass101_Sub1_5320.aFloat5669) + (class101_sub1.aFloat5680 * (aClass101_Sub1_5320.aFloat5666)) + (class101_sub1.aFloat5664 * (aClass101_Sub1_5320.aFloat5664)));
                }
                if (toolkit.anInt7485 > 1) {
                    synchronized (this) {
                        while (aBoolean5357) {
                            try {
                                this.wait();
                            } catch (InterruptedException interruptedexception) {
                                /* empty */
                            }
                        }
                        aBoolean5357 = true;
                    }
                }
                method632(Thread.currentThread());
                rasterizer.method1023((i_632_ & 0x2) != 0);
                boolean bool_656_ = false;
                boolean bool_657_ = i_636_ <= toolkit.anInt7482;
                boolean bool_658_ = (bool_657_ || emitters != null || effectors != null);
                aJavaThreadResource_5367.anInt2221 = rasterizer.width;
                aJavaThreadResource_5367.anInt2229 = rasterizer.anInt1665;
                aJavaThreadResource_5367.anInt2215 = rasterizer.anInt1668;
                int i_659_ = toolkit.anInt7491;
                int i_660_ = toolkit.anInt7497;
                int i_661_ = toolkit.anInt7482;
                if (i == -1) {
                    for (int i_662_ = 0; i_662_ < vertexCount; i_662_++) {
                        int i_663_ = vertexX[i_662_];
                        int i_664_ = vertexY[i_662_];
                        int i_665_ = vertexZ[i_662_];
                        float f_666_ = (f_638_ + f_650_ * (float) i_663_ + f_639_ * (float) i_664_ + f_653_ * (float) i_665_);
                        float f_667_ = (f_644_ + f_651_ * (float) i_663_ + f_645_ * (float) i_664_ + f_654_ * (float) i_665_);
                        float f_668_ = (f + f_652_ * (float) i_663_ + f_633_ * (float) i_664_ + f_655_ * (float) i_665_);
                        anIntArray5355[i_662_] = (int) f_668_;
                        if (f_668_ >= (float) i_661_) {
                            anIntArray5321[i_662_] = (aJavaThreadResource_5367.anInt2229 + (int) (f_666_ * (float) i_659_ / f_668_));
                            anIntArray5343[i_662_] = (aJavaThreadResource_5367.anInt2215 + (int) (f_667_ * (float) i_660_ / f_668_));
                        } else {
                            anIntArray5321[i_662_] = -5000;
                            bool_656_ = true;
                        }
                        if (bool_658_) {
                            anIntArray5399[i_662_] = (int) f_666_;
                            anIntArray5384[i_662_] = (int) f_667_;
                            anIntArray5392[i_662_] = (int) f_668_;
                        }
                        if (aJavaThreadResource_5367.aBoolean2195) anIntArray5362[i_662_] = (int) ((aClass101_Sub1_5320.aFloat5685) + ((aClass101_Sub1_5320.aFloat5655 * (float) i_663_) + (aClass101_Sub1_5320.aFloat5678 * (float) i_664_) + (aClass101_Sub1_5320.aFloat5666 * (float) i_665_)));
                    }
                    if (billboardFaces != null) {
                        for (int i_669_ = 0; i_669_ < billboardCount; i_669_++) {
                            JavaBillboardFace javaBillboardFace = billboardFaces[i_669_];
                            JavaBillboardAttributes javaBillboardAttributes = billboardAttributes[i_669_];
                            short i_670_ = faceA[javaBillboardFace.anInt144];
                            short i_671_ = faceB[javaBillboardFace.anInt144];
                            short i_672_ = faceC[javaBillboardFace.anInt144];
                            int i_673_ = ((vertexX[i_670_] + vertexX[i_671_] + vertexX[i_672_]) / 3);
                            int i_674_ = ((vertexY[i_670_] + vertexY[i_671_] + vertexY[i_672_]) / 3);
                            int i_675_ = ((vertexZ[i_670_] + vertexZ[i_671_] + vertexZ[i_672_]) / 3);
                            float f_676_ = ((float) javaBillboardAttributes.anInt4316 + (f_638_ + f_650_ * (float) i_673_ + f_639_ * (float) i_674_ + f_653_ * (float) i_675_));
                            float f_677_ = ((float) javaBillboardAttributes.anInt4317 + (f_644_ + f_651_ * (float) i_673_ + f_645_ * (float) i_674_ + f_654_ * (float) i_675_));
                            float f_678_ = (f + f_652_ * (float) i_673_ + f_633_ * (float) i_674_ + f_655_ * (float) i_675_);
                            if (f_678_ > (float) (toolkit.anInt7482)) {
                                javaBillboardAttributes.anInt4312 = (toolkit.anInt7510 + (int) (f_676_ * (float) i_659_ / f_678_));
                                javaBillboardAttributes.anInt4310 = (toolkit.anInt7504 + (int) (f_677_ * (float) i_660_ / f_678_));
                                javaBillboardAttributes.anInt4320 = ((int) f_678_ - javaBillboardFace.anInt154);
                                javaBillboardAttributes.anInt4309 = (int) ((float) ((javaBillboardAttributes.anInt4314) * (javaBillboardFace.aShort150) * i_659_) / (f_678_ * 128.0F));
                                javaBillboardAttributes.anInt4307 = (int) ((float) ((javaBillboardAttributes.anInt4311) * (javaBillboardFace.aShort143) * i_660_) / (f_678_ * 128.0F));
                            } else javaBillboardAttributes.anInt4309 = javaBillboardAttributes.anInt4307 = 0;
                        }
                    }
                } else {
                    for (int i_679_ = 0; i_679_ < vertexCount; i_679_++) {
                        int i_680_ = vertexX[i_679_];
                        int i_681_ = vertexY[i_679_];
                        int i_682_ = vertexZ[i_679_];
                        float f_683_ = (f_638_ + f_650_ * (float) i_680_ + f_639_ * (float) i_681_ + f_653_ * (float) i_682_);
                        float f_684_ = (f_644_ + f_651_ * (float) i_680_ + f_645_ * (float) i_681_ + f_654_ * (float) i_682_);
                        float f_685_ = (f + f_652_ * (float) i_680_ + f_633_ * (float) i_681_ + f_655_ * (float) i_682_);
                        anIntArray5355[i_679_] = (int) f_685_;
                        anIntArray5321[i_679_] = (aJavaThreadResource_5367.anInt2229 + (int) (f_683_ * (float) i_659_ / (float) i));
                        anIntArray5343[i_679_] = (aJavaThreadResource_5367.anInt2215 + (int) (f_684_ * (float) i_660_ / (float) i));
                        if (bool_658_) {
                            anIntArray5399[i_679_] = (int) f_683_;
                            anIntArray5384[i_679_] = (int) f_684_;
                            anIntArray5392[i_679_] = i;
                        }
                        if (aJavaThreadResource_5367.aBoolean2195) anIntArray5362[i_679_] = (int) ((aClass101_Sub1_5320.aFloat5685) + ((aClass101_Sub1_5320.aFloat5655 * (float) i_680_) + (aClass101_Sub1_5320.aFloat5678 * (float) i_681_) + (aClass101_Sub1_5320.aFloat5666 * (float) i_682_)));
                    }
                    if (billboardFaces != null) {
                        for (int i_686_ = 0; i_686_ < billboardCount; i_686_++) {
                            JavaBillboardFace javaBillboardFace = billboardFaces[i_686_];
                            JavaBillboardAttributes javaBillboardAttributes = billboardAttributes[i_686_];
                            short i_687_ = faceA[javaBillboardFace.anInt144];
                            short i_688_ = faceB[javaBillboardFace.anInt144];
                            short i_689_ = faceC[javaBillboardFace.anInt144];
                            int i_690_ = ((vertexX[i_687_] + vertexX[i_688_] + vertexX[i_689_]) / 3);
                            int i_691_ = ((vertexY[i_687_] + vertexY[i_688_] + vertexY[i_689_]) / 3);
                            int i_692_ = ((vertexZ[i_687_] + vertexZ[i_688_] + vertexZ[i_689_]) / 3);
                            float f_693_ = (f_638_ + f_650_ * (float) i_690_ + f_639_ * (float) i_691_ + f_653_ * (float) i_692_);
                            float f_694_ = (f_644_ + f_651_ * (float) i_690_ + f_645_ * (float) i_691_ + f_654_ * (float) i_692_);
                            float f_695_ = (f + f_652_ * (float) i_690_ + f_633_ * (float) i_691_ + f_655_ * (float) i_692_);
                            javaBillboardAttributes.anInt4312 = (toolkit.anInt7510 + (int) (f_693_ * (float) i_659_ / (float) i));
                            javaBillboardAttributes.anInt4310 = (toolkit.anInt7504 + (int) (f_694_ * (float) i_660_ / (float) i));
                            javaBillboardAttributes.anInt4320 = i - javaBillboardFace.anInt154;
                            javaBillboardAttributes.anInt4309 = (javaBillboardAttributes.anInt4314 * javaBillboardFace.aShort150 * i_659_ / (i << 7));
                            javaBillboardAttributes.anInt4307 = (javaBillboardAttributes.anInt4311 * javaBillboardFace.aShort143 * i_660_ / (i << 7));
                        }
                    }
                }
                if (class318_sub3 != null) {
                    boolean bool_696_ = false;
                    boolean bool_697_ = true;
                    int i_698_ = aShort5395 + aShort5393 >> 1;
                    int i_699_ = aShort5352 + aShort5331 >> 1;
                    int i_700_ = i_698_;
                    short i_701_ = aShort5329;
                    int i_702_ = i_699_;
                    float f_703_ = (f_638_ + f_650_ * (float) i_700_ + f_639_ * (float) i_701_ + f_653_ * (float) i_702_);
                    float f_704_ = (f_644_ + f_651_ * (float) i_700_ + f_645_ * (float) i_701_ + f_654_ * (float) i_702_);
                    float f_705_ = (f + f_652_ * (float) i_700_ + f_633_ * (float) i_701_ + f_655_ * (float) i_702_);
                    if (f_705_ >= (float) i_661_) {
                        int i_706_ = (int) f_705_;
                        if (i != -1) i_706_ = i;
                        class318_sub3.anInt6405 = (toolkit.anInt7510 + (int) (f_703_ * (float) i_659_ / (float) i_706_));
                        class318_sub3.anInt6402 = (toolkit.anInt7504 + (int) (f_704_ * (float) i_660_ / (float) i_706_));
                    } else bool_696_ = true;
                    i_700_ = i_698_;
                    i_701_ = aShort5365;
                    i_702_ = i_699_;
                    float f_707_ = (f_638_ + f_650_ * (float) i_700_ + f_639_ * (float) i_701_ + f_653_ * (float) i_702_);
                    float f_708_ = (f_644_ + f_651_ * (float) i_700_ + f_645_ * (float) i_701_ + f_654_ * (float) i_702_);
                    float f_709_ = (f + f_652_ * (float) i_700_ + f_633_ * (float) i_701_ + f_655_ * (float) i_702_);
                    if (f_709_ >= (float) i_661_) {
                        int i_710_ = (int) f_709_;
                        if (i != -1) i_710_ = i;
                        class318_sub3.anInt6406 = (toolkit.anInt7510 + (int) (f_707_ * (float) i_659_ / (float) i_710_));
                        class318_sub3.anInt6404 = (toolkit.anInt7504 + (int) (f_708_ * (float) i_660_ / (float) i_710_));
                    } else bool_696_ = true;
                    if (bool_696_) {
                        if (f_705_ < (float) i_661_ && f_709_ < (float) i_661_) bool_697_ = false;
                        else if (f_705_ < (float) i_661_) {
                            float f_711_ = ((f_709_ - (float) (toolkit.anInt7482)) / (f_709_ - f_705_));
                            int i_712_ = (int) (f_707_ + (f_707_ - f_703_) * f_711_);
                            int i_713_ = (int) (f_708_ + (f_708_ - f_704_) * f_711_);
                            int i_714_ = i_661_;
                            if (i != -1) i_714_ = i;
                            class318_sub3.anInt6405 = (toolkit.anInt7510 + i_712_ * i_659_ / i_714_);
                            class318_sub3.anInt6402 = (toolkit.anInt7504 + i_713_ * i_660_ / i_714_);
                        } else if (f_709_ < (float) i_661_) {
                            float f_715_ = ((f_705_ - (float) i_661_) / (f_705_ - f_709_));
                            int i_716_ = (int) (f_703_ + (f_703_ - f_707_) * f_715_);
                            int i_717_ = (int) (f_704_ + (f_704_ - f_708_) * f_715_);
                            int i_718_ = i_661_;
                            if (i != -1) i_718_ = i;
                            class318_sub3.anInt6405 = (toolkit.anInt7510 + i_716_ * i_659_ / i_718_);
                            class318_sub3.anInt6402 = (toolkit.anInt7504 + i_717_ * i_660_ / i_718_);
                        }
                    }
                    if (bool_697_) {
                        if (f_705_ > f_709_) {
                            int i_719_ = (int) f_705_;
                            if (i != -1) i_719_ = i;
                            class318_sub3.anInt6403 = (toolkit.anInt7510 + (int) ((f_703_ + (float) aShort5324) * (float) i_659_ / (float) i_719_) - (class318_sub3.anInt6405));
                        } else {
                            int i_720_ = (int) f_709_;
                            if (i != -1) i_720_ = i;
                            class318_sub3.anInt6403 = (toolkit.anInt7510 + (int) ((f_707_ + (float) aShort5324) * (float) i_659_ / (float) i_720_) - (class318_sub3.anInt6406));
                        }
                        class318_sub3.aBoolean6401 = true;
                    }
                }
                method634(true);
                rasterizer.aBoolean1669 = (i_632_ & 0x1) == 0;
                rasterizer.aBoolean1667 = false;
                try {
                    method650(bool_656_, ((aJavaThreadResource_5367.aBoolean2201 && (i_637_ > aJavaThreadResource_5367.anInt2210)) || aJavaThreadResource_5367.aBoolean2195), i_636_, i_637_ - i_636_);
                } catch (Exception exception) {
                    /* empty */
                }
                if (billboardFaces != null) {
                    for (int i_721_ = 0; i_721_ < faceCount; i_721_++)
                        anIntArray5400[i_721_] = -1;
                }
                rasterizer = null;
                if (toolkit.anInt7485 > 1) {
                    synchronized (this) {
                        aBoolean5357 = false;
                        this.notifyAll();
                    }
                }
            }
        }
    }

    final boolean r() {
        return movingTextures;
    }

    private final void method655() {
        if (!aBoolean5323) {
            int i = 0;
            int i_722_ = 0;
            int i_723_ = 32767;
            int i_724_ = 32767;
            int i_725_ = 32767;
            int i_726_ = -32768;
            int i_727_ = -32768;
            int i_728_ = -32768;
            for (int i_729_ = 0; i_729_ < maxVertex; i_729_++) {
                int i_730_ = vertexX[i_729_];
                int i_731_ = vertexY[i_729_];
                int i_732_ = vertexZ[i_729_];
                if (i_730_ < i_723_) i_723_ = i_730_;
                if (i_730_ > i_726_) i_726_ = i_730_;
                if (i_731_ < i_724_) i_724_ = i_731_;
                if (i_731_ > i_727_) i_727_ = i_731_;
                if (i_732_ < i_725_) i_725_ = i_732_;
                if (i_732_ > i_728_) i_728_ = i_732_;
                int i_733_ = i_730_ * i_730_ + i_732_ * i_732_;
                if (i_733_ > i) i = i_733_;
                i_733_ += i_731_ * i_731_;
                if (i_733_ > i_722_) i_722_ = i_733_;
            }
            aShort5395 = (short) i_723_;
            aShort5393 = (short) i_726_;
            aShort5329 = (short) i_724_;
            aShort5365 = (short) i_727_;
            aShort5352 = (short) i_725_;
            aShort5331 = (short) i_728_;
            aShort5324 = (short) (int) (Math.sqrt(i) + 0.99);
            aShort5348 = (short) (int) (Math.sqrt(i_722_) + 0.99);
            aBoolean5323 = true;
        }
    }

    private final int method656(int i) {
        if (i < 2) i = 2;
        else if (i > 126) i = 126;
        return i;
    }

    private final void method657(boolean bool) {
        if (anInt5354 == 1) method647();
        else if (anInt5354 == 2) {
            if ((functionMask & 0x97098) == 0 && texCoordU == null) faceColour = null;
            if (bool) shadingType = null;
        } else {
            method636();
            int i = toolkit.anInt7484;
            int i_734_ = toolkit.anInt7473;
            int i_735_ = toolkit.anInt7479;
            int i_736_ = toolkit.anInt7500 >> 8;
            int i_737_ = toolkit.anInt7474 * 768 / contrast;
            int i_738_ = toolkit.anInt7478 * 768 / contrast;
            if (anIntArray5368 == null) {
                anIntArray5368 = new int[faceCount];
                anIntArray5337 = new int[faceCount];
                anIntArray5366 = new int[faceCount];
            }
            for (int i_739_ = 0; i_739_ < faceCount; i_739_++) {
                byte i_740_;
                if (shadingType == null) i_740_ = (byte) 0;
                else i_740_ = shadingType[i_739_];
                byte i_741_;
                if (faceAlpha == null) i_741_ = (byte) 0;
                else i_741_ = faceAlpha[i_739_];
                short i_742_;
                if (faceTextures == null) i_742_ = (short) -1;
                else i_742_ = faceTextures[i_739_];
                if (i_741_ == -2) i_740_ = (byte) 3;
                if (i_741_ == -1) i_740_ = (byte) 2;
                if (i_742_ == -1) {
                    if (i_740_ == 0) {
                        int i_743_ = faceColour[i_739_] & 0xffff;
                        int i_744_ = (i_743_ & 0x7f) * ambient >> 7;
                        short i_745_ = Class25.method303(i_743_ & ~0x7f | i_744_, 30);
                        Class360 class360;
                        if (aClass360Array5313 != null && (aClass360Array5313[faceA[i_739_]] != null)) class360 = aClass360Array5313[faceA[i_739_]];
                        else class360 = aClass360Array5360[faceA[i_739_]];
                        int i_746_ = (((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16);
                        int i_747_ = i_746_ > 256 ? i_737_ : i_738_;
                        int i_748_ = (i_736_ >> 1) + (i_747_ * i_746_ >> 17);
                        anIntArray5368[i_739_] = i_748_ << 17 | method2198(0, i_748_, i_745_);
                        if (aClass360Array5313 != null && (aClass360Array5313[faceB[i_739_]] != null)) class360 = aClass360Array5313[faceB[i_739_]];
                        else class360 = aClass360Array5360[faceB[i_739_]];
                        i_746_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_747_ = i_746_ > 256 ? i_737_ : i_738_;
                        i_748_ = (i_736_ >> 1) + (i_747_ * i_746_ >> 17);
                        anIntArray5337[i_739_] = i_748_ << 17 | method2198(0, i_748_, i_745_);
                        if (aClass360Array5313 != null && (aClass360Array5313[faceC[i_739_]] != null)) class360 = aClass360Array5313[faceC[i_739_]];
                        else class360 = aClass360Array5360[faceC[i_739_]];
                        i_746_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_747_ = i_746_ > 256 ? i_737_ : i_738_;
                        i_748_ = (i_736_ >> 1) + (i_747_ * i_746_ >> 17);
                        anIntArray5366[i_739_] = i_748_ << 17 | method2198(0, i_748_, i_745_);
                    } else if (i_740_ == 1) {
                        int i_749_ = faceColour[i_739_] & 0xffff;
                        int i_750_ = (i_749_ & 0x7f) * ambient >> 7;
                        short i_751_ = Class25.method303(i_749_ & ~0x7f | i_750_, 30);
                        Class41 class41 = aClass41Array5385[i_739_];
                        int i_752_ = ((i * class41.anInt561 + i_734_ * class41.anInt560 + i_735_ * class41.anInt559) >> 16);
                        int i_753_ = i_752_ > 256 ? i_737_ : i_738_;
                        int i_754_ = (i_736_ >> 1) + (i_753_ * i_752_ >> 17);
                        anIntArray5368[i_739_] = i_754_ << 17 | method2198(0, i_754_, i_751_);
                        anIntArray5366[i_739_] = -1;
                    } else if (i_740_ == 3) {
                        anIntArray5368[i_739_] = 128;
                        anIntArray5366[i_739_] = -1;
                    } else anIntArray5366[i_739_] = -2;
                } else {
                    int i_755_ = faceColour[i_739_] & 0xffff;
                    if (i_740_ == 0) {
                        Class360 class360;
                        if (aClass360Array5313 != null && (aClass360Array5313[faceA[i_739_]] != null)) class360 = aClass360Array5313[faceA[i_739_]];
                        else class360 = aClass360Array5360[faceA[i_739_]];
                        int i_756_ = (((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16);
                        int i_757_ = i_756_ > 256 ? i_737_ : i_738_;
                        int i_758_ = method656((i_736_ >> 2) + (i_757_ * i_756_ >> 18));
                        anIntArray5368[i_739_] = i_758_ << 24 | method642(i_755_, i_742_, i_758_);
                        if (aClass360Array5313 != null && (aClass360Array5313[faceB[i_739_]] != null)) class360 = aClass360Array5313[faceB[i_739_]];
                        else class360 = aClass360Array5360[faceB[i_739_]];
                        i_756_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_757_ = i_756_ > 256 ? i_737_ : i_738_;
                        i_758_ = method656((i_736_ >> 2) + (i_757_ * i_756_ >> 18));
                        anIntArray5337[i_739_] = i_758_ << 24 | method642(i_755_, i_742_, i_758_);
                        if (aClass360Array5313 != null && (aClass360Array5313[faceC[i_739_]] != null)) class360 = aClass360Array5313[faceC[i_739_]];
                        else class360 = aClass360Array5360[faceC[i_739_]];
                        i_756_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_757_ = i_756_ > 256 ? i_737_ : i_738_;
                        i_758_ = method656((i_736_ >> 2) + (i_757_ * i_756_ >> 18));
                        anIntArray5366[i_739_] = i_758_ << 24 | method642(i_755_, i_742_, i_758_);
                    } else if (i_740_ == 1) {
                        Class41 class41 = aClass41Array5385[i_739_];
                        int i_759_ = ((i * class41.anInt561 + i_734_ * class41.anInt560 + i_735_ * class41.anInt559) >> 16);
                        int i_760_ = i_759_ > 256 ? i_737_ : i_738_;
                        int i_761_ = method656((i_736_ >> 2) + (i_760_ * i_759_ >> 18));
                        anIntArray5368[i_739_] = i_761_ << 24 | method642(i_755_, i_742_, i_761_);
                        anIntArray5366[i_739_] = -1;
                    } else anIntArray5366[i_739_] = -2;
                }
            }
            aClass360Array5360 = null;
            aClass360Array5313 = null;
            aClass41Array5385 = null;
            if ((functionMask & 0x97098) == 0 && texCoordU == null) faceColour = null;
            if (bool) shadingType = null;
            anInt5354 = 2;
        }
    }

    final void O(int i, int i_765_, int i_766_) {
        if (i != 128 && (functionMask & 0x1) != 1) throw new IllegalStateException();
        if (i_765_ != 128 && (functionMask & 0x2) != 2) throw new IllegalStateException();
        if (i_766_ != 128 && (functionMask & 0x4) != 4) throw new IllegalStateException();
        synchronized (this) {
            for (int i_767_ = 0; i_767_ < vertexCount; i_767_++) {
                vertexX[i_767_] = vertexX[i_767_] * i >> 7;
                vertexY[i_767_] = vertexY[i_767_] * i_765_ >> 7;
                vertexZ[i_767_] = vertexZ[i_767_] * i_766_ >> 7;
            }
            aBoolean5323 = false;
        }
    }

    final int ua() {
        return functionMask;
    }

    // dependency of method643, not in genuine list
    private final void method658(int i) {
        if (aJavaThreadResource_5367.aBoolean2195) {
            short i_777_ = faceA[i];
            short i_778_ = faceB[i];
            short i_779_ = faceC[i];
            int i_780_ = 0;
            int i_781_ = 0;
            int i_782_ = 0;
            if (anIntArray5362[i_777_] > aJavaThreadResource_5367.anInt2197) i_780_ = 255;
            else if (anIntArray5362[i_777_] > aJavaThreadResource_5367.anInt2211) i_780_ = ((aJavaThreadResource_5367.anInt2211 - anIntArray5362[i_777_]) * 255 / (aJavaThreadResource_5367.anInt2211 - aJavaThreadResource_5367.anInt2197));
            if (anIntArray5362[i_778_] > aJavaThreadResource_5367.anInt2197) i_781_ = 255;
            else if (anIntArray5362[i_778_] > aJavaThreadResource_5367.anInt2211) i_781_ = ((aJavaThreadResource_5367.anInt2211 - anIntArray5362[i_778_]) * 255 / (aJavaThreadResource_5367.anInt2211 - aJavaThreadResource_5367.anInt2197));
            if (anIntArray5362[i_779_] > aJavaThreadResource_5367.anInt2197) i_782_ = 255;
            else if (anIntArray5362[i_779_] > aJavaThreadResource_5367.anInt2211) i_782_ = ((aJavaThreadResource_5367.anInt2211 - anIntArray5362[i_779_]) * 255 / (aJavaThreadResource_5367.anInt2211 - aJavaThreadResource_5367.anInt2197));
            if (faceAlpha == null) rasterizer.anInt1674 = 0;
            else rasterizer.anInt1674 = faceAlpha[i] & 0xff;
            if (faceTextures == null || faceTextures[i] == -1) {
                if (anIntArray5366[i] == -1)
                    rasterizer.method1027((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB[(anIntArray5368[i] & 0xffff)]), (i_780_ << 24 | (aJavaThreadResource_5367.anInt2192)), 255), JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB[(anIntArray5368[i] & 0xffff)]), (i_781_ << 24 | (aJavaThreadResource_5367.anInt2192)), 255), JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB[(anIntArray5368[i] & 0xffff)]), (i_782_ << 24 | (aJavaThreadResource_5367.anInt2192)), 255));
                else
                    rasterizer.method1027((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB[(anIntArray5368[i] & 0xffff)]), (i_780_ << 24 | (aJavaThreadResource_5367.anInt2192)), 255), JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB[(anIntArray5337[i] & 0xffff)]), (i_781_ << 24 | (aJavaThreadResource_5367.anInt2192)), 255), JavaBillboardFace.method206((ItemSpriteCacheKey.HSV_TO_RGB[(anIntArray5366[i] & 0xffff)]), (i_782_ << 24 | (aJavaThreadResource_5367.anInt2192)), 255));
            } else {
                int i_775_ = -16777216;
                if (faceAlpha != null) i_775_ = 255 - (faceAlpha[i] & 0xff) << 24;
                if (anIntArray5366[i] == -1) {
                    int i_776_ = i_775_ | anIntArray5368[i] & 0xffffff;
                    rasterizer.method1024((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_776_, i_776_, i_776_, aJavaThreadResource_5367.anInt2192, i_780_, i_781_, i_782_, faceTextures[i]);
                } else
                    rasterizer.method1024((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], texCoordU[i][0], texCoordU[i][1], texCoordU[i][2], texCoordV[i][0], texCoordV[i][1], texCoordV[i][2], i_775_ | anIntArray5368[i] & 0xffffff, i_775_ | anIntArray5337[i] & 0xffffff, i_775_ | anIntArray5366[i] & 0xffffff, aJavaThreadResource_5367.anInt2192, i_780_, i_781_, i_782_, faceTextures[i]);
            }
        }
    }

    JavaModel(JavaToolkit var_ha_Sub1) {
        anInt5354 = 0;
        aBoolean5369 = false;
        faceCount = 0;
        aBoolean5372 = false;
        movingTextures = false;
        maxVertex = 0;
        transparent = false;
        toolkit = var_ha_Sub1;
    }

    JavaModel(JavaToolkit toolkit, Mesh mesh, int functionMask, int ambient, int contrast, int featureMask) {
        anInt5354 = 0;
        aBoolean5369 = false;
        faceCount = 0;
        aBoolean5372 = false;
        movingTextures = false;
        maxVertex = 0;
        transparent = false;
        this.toolkit = toolkit;
        this.functionMask = functionMask;
        this.ambient = ambient;
        this.contrast = contrast;
        TextureSource source = this.toolkit.textureSource;
        vertexCount = mesh.vertexCount;
        maxVertex = mesh.maxVertex;
        vertexX = mesh.vertexX;
        vertexY = mesh.vertexY;
        vertexZ = mesh.vertexZ;
        faceCount = mesh.faceCount;
        faceA = mesh.faceA;
        faceB = mesh.faceB;
        faceC = mesh.faceC;
        facePriority = mesh.facePriority;
        faceColour = mesh.faceColour;
        faceAlpha = mesh.faceAlpha;
        aShortArray5370 = mesh.aShortArray1856;
        shadingType = mesh.shadingType;
        emitters = mesh.emitters;
        effectors = mesh.effectors;
        aShortArray5333 = mesh.aShortArray1842;
        int[] faceIndex = new int[faceCount];
        for (int i_788_ = 0; i_788_ < faceCount; i_788_++)
            faceIndex[i_788_] = i_788_;
        long[] faceIds = new long[faceCount];
        boolean bool = (this.functionMask & 0x100) != 0;
        for (int i = 0; i < faceCount; i++) {
            int index = faceIndex[i];
            TextureMetrics metrics = null;
            int i_791_ = 0;
            int i_792_ = 0;
            int i_793_ = 0;
            int i_794_ = 0;
            if (mesh.billboards != null) {
                boolean hideFace = false;
                for (int j = 0; j < mesh.billboards.length; j++) {
                    MeshBillboard billboard = mesh.billboards[j];
                    if (index == billboard.face) {
                        BillboardType type = Class73.list(104, (billboard.id));
                        if (type.aBoolean2531) hideFace = true;
                        if (type.texture != -1) {
                            TextureMetrics textureMetrics_797_ = source.getMetrics((type.texture), -6662);
                            if (textureMetrics_797_.alphaBlendMode == 2) transparent = true;
                        }
                    }
                }
                if (hideFace) faceIds[i] = 9223372036854775807L;
            }
            int texture = -1;
            if (mesh.faceTexture != null) {
                texture = mesh.faceTexture[index];
                if (texture != -1) {
                    metrics = source.getMetrics(texture & 0xffff, -6662);
                    if ((featureMask & 0x40) == 0 || !metrics.disableable) {
                        i_793_ = metrics.effectType;
                        i_794_ = metrics.effectParam1;
                    } else texture = -1;
                }
            }
            boolean transparentFace = (faceAlpha != null && faceAlpha[index] != 0 || metrics != null && metrics.alphaBlendMode == 2);
            if ((bool || transparentFace) && facePriority != null) i_791_ += facePriority[index] << 17;
            if (transparentFace) i_791_ += 65536;
            i_791_ += (i_793_ & 0xff) << 8;
            i_791_ += i_794_ & 0xff;
            i_792_ += (texture & 0xffff) << 16;
            i_792_ += i & 0xffff;
            faceIds[i] = ((long) i_791_ << 32) + (long) i_792_;
            transparent |= transparentFace;
        }
        Class348_Sub16_Sub2.sort(faceIndex, faceIds, 0);
        if (mesh.billboards != null) {
            billboardCount = mesh.billboards.length;
            billboardFaces = new JavaBillboardFace[billboardCount];
            billboardAttributes = new JavaBillboardAttributes[billboardCount];
            for (int i_800_ = 0; i_800_ < mesh.billboards.length; i_800_++) {
                MeshBillboard billboard = mesh.billboards[i_800_];
                BillboardType type = Class73.list(104, billboard.id);
                int i_801_ = ((ItemSpriteCacheKey.HSV_TO_RGB[(mesh.faceColour[billboard.face]) & 0xffff]) & 0xffffff);
                i_801_ = (i_801_ | 255 - (mesh.faceAlpha != null ? (mesh.faceAlpha[billboard.face]) & 0xff : 0) << 24);
                billboardFaces[i_800_] = new JavaBillboardFace(billboard.face, (mesh.faceA[billboard.face]), (mesh.faceB[billboard.face]), (mesh.faceC[billboard.face]), type.anInt2526, type.anInt2530, type.texture, type.anInt2533, type.anInt2534, type.aBoolean2531, billboard.anInt2158);
                billboardAttributes[i_800_] = new JavaBillboardAttributes(i_801_);
            }
        }
        texCoordU = new float[faceCount][];
        texCoordV = new float[faceCount][];
        TextureUniverse universe = Class59_Sub2_Sub1.fromMesh(255, faceCount, mesh, faceIndex);
        JavaThreadResource javaThreadResource = this.toolkit.threadResource(Thread.currentThread());
        float[] fs = javaThreadResource.aFloatArray2226;
        boolean bool_802_ = false;
        for (int i_803_ = 0; i_803_ < faceCount; i_803_++) {
            int i_804_ = faceIndex[i_803_];
            int i_805_;
            if (mesh.faceTexSpace == null) i_805_ = -1;
            else i_805_ = mesh.faceTexSpace[i_804_];
            int i_806_ = (mesh.faceTexture == null ? -1 : mesh.faceTexture[i_804_]);
            if (i_806_ != -1 && (featureMask & 0x40) != 0) {
                TextureMetrics metrics = source.getMetrics(i_806_ & 0xffff, -6662);
                if (metrics.disableable) i_806_ = -1;
            }
            if (i_806_ != -1) {
                bool_802_ = true;
                float[] fs_807_ = texCoordU[i_804_] = new float[3];
                float[] fs_808_ = texCoordV[i_804_] = new float[3];
                boolean bool_809_ = false;
                if (i_805_ == -1) {
                    fs_807_[0] = 0.0F;
                    fs_808_[0] = 1.0F;
                    fs_807_[1] = 1.0F;
                    fs_808_[1] = 1.0F;
                    fs_807_[2] = 0.0F;
                    fs_808_[2] = 0.0F;
                } else {
                    i_805_ &= 0xff;
                    byte mappingType = mesh.texMappingType[i_805_];
                    if (mappingType == 0) {
                        short i_811_ = faceA[i_804_];
                        short i_812_ = faceB[i_804_];
                        short i_813_ = faceC[i_804_];
                        short i_814_ = mesh.texSpaceDefA[i_805_];
                        short i_815_ = mesh.texSpaceDefB[i_805_];
                        short i_816_ = mesh.texSpaceDefC[i_805_];
                        float f = (float) vertexX[i_814_];
                        float f_817_ = (float) vertexY[i_814_];
                        float f_818_ = (float) vertexZ[i_814_];
                        float f_819_ = (float) vertexX[i_815_] - f;
                        float f_820_ = (float) vertexY[i_815_] - f_817_;
                        float f_821_ = (float) vertexZ[i_815_] - f_818_;
                        float f_822_ = (float) vertexX[i_816_] - f;
                        float f_823_ = (float) vertexY[i_816_] - f_817_;
                        float f_824_ = (float) vertexZ[i_816_] - f_818_;
                        float f_825_ = (float) vertexX[i_811_] - f;
                        float f_826_ = (float) vertexY[i_811_] - f_817_;
                        float f_827_ = (float) vertexZ[i_811_] - f_818_;
                        float f_828_ = (float) vertexX[i_812_] - f;
                        float f_829_ = (float) vertexY[i_812_] - f_817_;
                        float f_830_ = (float) vertexZ[i_812_] - f_818_;
                        float f_831_ = (float) vertexX[i_813_] - f;
                        float f_832_ = (float) vertexY[i_813_] - f_817_;
                        float f_833_ = (float) vertexZ[i_813_] - f_818_;
                        float f_834_ = f_820_ * f_824_ - f_821_ * f_823_;
                        float f_835_ = f_821_ * f_822_ - f_819_ * f_824_;
                        float f_836_ = f_819_ * f_823_ - f_820_ * f_822_;
                        float f_837_ = f_823_ * f_836_ - f_824_ * f_835_;
                        float f_838_ = f_824_ * f_834_ - f_822_ * f_836_;
                        float f_839_ = f_822_ * f_835_ - f_823_ * f_834_;
                        float f_840_ = 1.0F / (f_837_ * f_819_ + f_838_ * f_820_ + f_839_ * f_821_);
                        fs_807_[0] = (f_837_ * f_825_ + f_838_ * f_826_ + f_839_ * f_827_) * f_840_;
                        fs_807_[1] = (f_837_ * f_828_ + f_838_ * f_829_ + f_839_ * f_830_) * f_840_;
                        fs_807_[2] = (f_837_ * f_831_ + f_838_ * f_832_ + f_839_ * f_833_) * f_840_;
                        f_837_ = f_820_ * f_836_ - f_821_ * f_835_;
                        f_838_ = f_821_ * f_834_ - f_819_ * f_836_;
                        f_839_ = f_819_ * f_835_ - f_820_ * f_834_;
                        f_840_ = 1.0F / (f_837_ * f_822_ + f_838_ * f_823_ + f_839_ * f_824_);
                        fs_808_[0] = (f_837_ * f_825_ + f_838_ * f_826_ + f_839_ * f_827_) * f_840_;
                        fs_808_[1] = (f_837_ * f_828_ + f_838_ * f_829_ + f_839_ * f_830_) * f_840_;
                        fs_808_[2] = (f_837_ * f_831_ + f_838_ * f_832_ + f_839_ * f_833_) * f_840_;
                    } else {
                        short i_841_ = faceA[i_804_];
                        short i_842_ = faceB[i_804_];
                        short i_843_ = faceC[i_804_];
                        int i_844_ = universe.originX[i_805_];
                        int i_845_ = universe.originY[i_805_];
                        int i_846_ = universe.originZ[i_805_];
                        float[] fs_847_ = (universe.matrices[i_805_]);
                        byte direction = mesh.texDirection[i_805_];
                        float f = ((float) (mesh.texOffsetX[i_805_]) / 256.0F);
                        if (mappingType == 1) {
                            float f_849_ = ((float) (mesh.texSpaceScaleZ[i_805_]) / 1024.0F);
                            Class246.cylinderMap(i_846_, vertexZ[i_841_], direction, 8, vertexX[i_841_], fs, vertexY[i_841_], f, i_845_, i_844_, f_849_, fs_847_);
                            fs_807_[0] = fs[0];
                            fs_808_[0] = fs[1];
                            Class246.cylinderMap(i_846_, vertexZ[i_842_], direction, 8, vertexX[i_842_], fs, vertexY[i_842_], f, i_845_, i_844_, f_849_, fs_847_);
                            fs_807_[1] = fs[0];
                            fs_808_[1] = fs[1];
                            Class246.cylinderMap(i_846_, vertexZ[i_843_], direction, 8, vertexX[i_843_], fs, vertexY[i_843_], f, i_845_, i_844_, f_849_, fs_847_);
                            fs_807_[2] = fs[0];
                            fs_808_[2] = fs[1];
                            float f_850_ = f_849_ / 2.0F;
                            if ((direction & 0x1) == 0) {
                                if (fs_807_[1] - fs_807_[0] > f_850_) fs_807_[1] -= f_849_;
                                else if (fs_807_[0] - fs_807_[1] > f_850_) fs_807_[1] += f_849_;
                                if (fs_807_[2] - fs_807_[0] > f_850_) fs_807_[2] -= f_849_;
                                else if (fs_807_[0] - fs_807_[2] > f_850_) fs_807_[2] += f_849_;
                            } else {
                                if (fs_808_[1] - fs_808_[0] > f_850_) fs_808_[1] -= f_849_;
                                else if (fs_808_[0] - fs_808_[1] > f_850_) fs_808_[1] += f_849_;
                                if (fs_808_[2] - fs_808_[0] > f_850_) fs_808_[2] -= f_849_;
                                else if (fs_808_[0] - fs_808_[2] > f_850_) fs_808_[2] += f_849_;
                            }
                        } else if (mappingType == 2) {
                            float f_851_ = ((float) (mesh.texOffsetY[i_805_]) / 256.0F);
                            float f_852_ = ((float) (mesh.texOffsetZ[i_805_]) / 256.0F);
                            int i_853_ = (vertexX[i_842_] - vertexX[i_841_]);
                            int i_854_ = (vertexY[i_842_] - vertexY[i_841_]);
                            int i_855_ = (vertexZ[i_842_] - vertexZ[i_841_]);
                            int i_856_ = (vertexX[i_843_] - vertexX[i_841_]);
                            int i_857_ = (vertexY[i_843_] - vertexY[i_841_]);
                            int i_858_ = (vertexZ[i_843_] - vertexZ[i_841_]);
                            int i_859_ = i_854_ * i_858_ - i_857_ * i_855_;
                            int i_860_ = i_855_ * i_856_ - i_858_ * i_853_;
                            int i_861_ = i_853_ * i_857_ - i_856_ * i_854_;
                            float f_862_ = 64.0F / (float) (mesh.texSpaceScaleX[i_805_]);
                            float f_863_ = 64.0F / (float) (mesh.texSpaceScaleY[i_805_]);
                            float f_864_ = 64.0F / (float) (mesh.texSpaceScaleZ[i_805_]);
                            float f_865_ = (((float) i_859_ * fs_847_[0] + (float) i_860_ * fs_847_[1] + (float) i_861_ * fs_847_[2]) / f_862_);
                            float f_866_ = (((float) i_859_ * fs_847_[3] + (float) i_860_ * fs_847_[4] + (float) i_861_ * fs_847_[5]) / f_863_);
                            float f_867_ = (((float) i_859_ * fs_847_[6] + (float) i_860_ * fs_847_[7] + (float) i_861_ * fs_847_[8]) / f_864_);
                            int i_868_ = Class331.method2635(f_866_, false, f_867_, f_865_);
                            Class262.cubeMap(f_852_, f, fs_847_, vertexZ[i_841_], i_846_, false, direction, i_844_, vertexX[i_841_], vertexY[i_841_], f_851_, fs, i_845_, i_868_);
                            fs_807_[0] = fs[0];
                            fs_808_[0] = fs[1];
                            Class262.cubeMap(f_852_, f, fs_847_, vertexZ[i_842_], i_846_, false, direction, i_844_, vertexX[i_842_], vertexY[i_842_], f_851_, fs, i_845_, i_868_);
                            fs_807_[1] = fs[0];
                            fs_808_[1] = fs[1];
                            Class262.cubeMap(f_852_, f, fs_847_, vertexZ[i_843_], i_846_, false, direction, i_844_, vertexX[i_843_], vertexY[i_843_], f_851_, fs, i_845_, i_868_);
                            fs_807_[2] = fs[0];
                            fs_808_[2] = fs[1];
                        } else if (mappingType == 3) {
                            Class181.sphereMap(i_846_, direction, f, vertexX[i_841_], fs, vertexZ[i_841_], i_844_, vertexY[i_841_], i_845_, -4, fs_847_);
                            fs_807_[0] = fs[0];
                            fs_808_[0] = fs[1];
                            Class181.sphereMap(i_846_, direction, f, vertexX[i_842_], fs, vertexZ[i_842_], i_844_, vertexY[i_842_], i_845_, -4, fs_847_);
                            fs_807_[1] = fs[0];
                            fs_808_[1] = fs[1];
                            Class181.sphereMap(i_846_, direction, f, vertexX[i_843_], fs, vertexZ[i_843_], i_844_, vertexY[i_843_], i_845_, -4, fs_847_);
                            fs_807_[2] = fs[0];
                            fs_808_[2] = fs[1];
                            if ((direction & 0x1) == 0) {
                                if (fs_807_[1] - fs_807_[0] > 0.5F) fs_807_[1]--;
                                else if (fs_807_[0] - fs_807_[1] > 0.5F) fs_807_[1]++;
                                if (fs_807_[2] - fs_807_[0] > 0.5F) fs_807_[2]--;
                                else if (fs_807_[0] - fs_807_[2] > 0.5F) fs_807_[2]++;
                            } else {
                                if (fs_808_[1] - fs_808_[0] > 0.5F) fs_808_[1]--;
                                else if (fs_808_[0] - fs_808_[1] > 0.5F) fs_808_[1]++;
                                if (fs_808_[2] - fs_808_[0] > 0.5F) fs_808_[2]--;
                                else if (fs_808_[0] - fs_808_[2] > 0.5F) fs_808_[2]++;
                            }
                        }
                    }
                }
            }
        }
        if (!bool_802_) texCoordU = texCoordV = null;
        if (mesh.vertexLabel != null && (this.functionMask & 0x20) != 0) vertexLabels = mesh.getVertexLabels(true, -122);
        if (mesh.faceLabel != null && (this.functionMask & 0x180) != 0) faceLabels = mesh.getFaceLabels((byte) 30);
        if (mesh.billboards != null && (this.functionMask & 0x400) != 0) billboardLabels = mesh.getBillboardGroups((byte) -75);
        if (mesh.faceTexture != null) {
            faceTextures = new short[faceCount];
            boolean hasTextures = false;
            for (int i_870_ = 0; i_870_ < faceCount; i_870_++) {
                short i_871_ = mesh.faceTexture[i_870_];
                if (i_871_ != -1) {
                    TextureMetrics metrics = this.toolkit.textureSource.getMetrics(i_871_, -6662);
                    if ((featureMask & 0x40) == 0 || !metrics.disableable) {
                        faceTextures[i_870_] = i_871_;
                        hasTextures = true;
                        if (metrics.alphaBlendMode == 2) transparent = true;
                        if (metrics.speedU != 0 || metrics.speedV != 0) movingTextures = true;
                    } else faceTextures[i_870_] = (short) -1;
                } else faceTextures[i_870_] = (short) -1;
            }
            if (!hasTextures) faceTextures = null;
        } else faceTextures = null;
        if (transparent || billboardFaces != null) {
            faceIndices = new short[faceCount];
            for (int i_872_ = 0; i_872_ < faceCount; i_872_++)
                faceIndices[i_872_] = (short) faceIndex[i_872_];
        }
    }
}
