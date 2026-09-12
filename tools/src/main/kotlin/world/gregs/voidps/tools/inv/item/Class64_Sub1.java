package world.gregs.voidps.tools.inv.item;/* Class64_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class64_Sub1 extends Class64 {
    static int anInt5346 = 4096;
    static int anInt5350;
    private short[] aShortArray5311;
    private int[] anIntArray5312;
    private Class360[] aClass360Array5313;
    private float[][] aFloatArrayArray5314;
    private int[] anIntArray5315;
    private int anInt5316;
    private short[] aShortArray5317;
    private int[] anIntArray5318;
    private Class101_Sub1 aClass101_Sub1_5320;
    private int[] anIntArray5321;
    private Class129[] aClass129Array5322;
    private boolean aBoolean5323 = false;
    private short aShort5324;
    private byte[] aByteArray5325;
    private int[] anIntArray5326;
    private short[] aShortArray5327;
    private short aShort5329;
    private int[][] anIntArrayArray5330;
    private short aShort5331;
    private int[] anIntArray5332;
    private short[] aShortArray5333;
    private int[][] anIntArrayArray5334;
    private Class342[] aClass342Array5335;
    private int[] anIntArray5337;
    private int anInt5338;
    private int anInt5340 = 0;
    private int anInt5342;
    private int anInt5375;
    private int[] anIntArray5343;
    private int anInt5344;
    private float[][] aFloatArrayArray5345;
    private short aShort5348;
    private int anInt5349;
    private int anInt5351;
    private short aShort5352;
    private final ha_Sub1 aHa_Sub1_5353;
    private int anInt5354;
    private int[] anIntArray5355;
    private int[] anIntArray5356;
    private boolean aBoolean5357 = false;
    private byte[] aByteArray5358;
    private int[] anIntArray5359;
    private Class360[] aClass360Array5360;
    private Class6[] aClass6Array5361;
    private int[] anIntArray5362;
    private Class350[] aClass350Array5363;
    private short[] aShortArray5364;
    private short aShort5365;
    private int[] anIntArray5366;
    private Class167 aClass167_5367;
    private int[] anIntArray5368;
    private boolean aBoolean5369;
    private short[] aShortArray5370;
    private int[] anIntArray5371;
    private boolean aBoolean5372;
    private int[] anIntArray5373;
    private int[] anIntArray5377;
    private int[][] anIntArrayArray5379;
    private int[] anIntArray5381;
    private boolean aBoolean5382;
    private Class109 aClass109_5383;
    private int[] anIntArray5384;
    private Class41[] aClass41Array5385;
    private byte[] aByteArray5386;
    private int anInt5387;
    private short[] aShortArray5388;
    private int anInt5389;
    private boolean aBoolean5391;
    private int[] anIntArray5392;
    private short aShort5393;
    private short[] aShortArray5394;
    private short aShort5395;
    private int[] anIntArray5398;
    private int[] anIntArray5399;
    private int[] anIntArray5400;

    private final boolean method629(int i) {
        if (aByteArray5325 == null) return false;
        return aByteArray5325[i] != 0;
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
        Class167 class167 = aHa_Sub1_5353.method3724(thread);
        aClass109_5383 = class167.aClass109_2220;
        if (class167 != aClass167_5367) {
            aClass167_5367 = class167;
            anIntArray5362 = aClass167_5367.anIntArray2222;
            anIntArray5399 = aClass167_5367.anIntArray2244;
            anIntArray5384 = aClass167_5367.anIntArray2214;
            anIntArray5392 = aClass167_5367.anIntArray2237;
            anIntArray5321 = aClass167_5367.anIntArray2234;
            anIntArray5343 = aClass167_5367.anIntArray2230;
            anIntArray5355 = aClass167_5367.anIntArray2213;
            anIntArray5359 = aClass167_5367.anIntArray2218;
            anIntArray5373 = aClass167_5367.anIntArray2241;
            anIntArray5398 = aClass167_5367.anIntArray2245;
            anIntArray5315 = aClass167_5367.anIntArray2238;
            anIntArray5371 = aClass167_5367.anIntArray2247;
            anIntArray5381 = aClass167_5367.anIntArray2235;
            anIntArray5377 = aClass167_5367.anIntArray2240;
            anIntArray5326 = aClass167_5367.anIntArray2236;
            anIntArray5318 = aClass167_5367.anIntArray2216;
            anIntArray5400 = aClass167_5367.anIntArray2242;
        }
    }

    private final void method634(boolean bool) {
        if (aHa_Sub1_5353.anInt7485 > 1) {
            synchronized (this) {
                method657(bool);
            }
        } else method657(bool);
    }

    final Class129[] method619() {
        return aClass129Array5322;
    }

    final int V() {
        if (!aBoolean5323) method655();
        return aShort5395;
    }

    private final void method635(int i) {
        short i_39_ = aShortArray5317[i];
        short i_40_ = aShortArray5394[i];
        short i_41_ = aShortArray5364[i];
        if (aShortArray5388 == null || aShortArray5388[i] == -1) {
            if (aByteArray5325 == null) aClass109_5383.anInt1674 = 0;
            else aClass109_5383.anInt1674 = aByteArray5325[i] & 0xff;
            if (anIntArray5366[i] == -1)
                aClass109_5383.method1018((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], (Class126.anIntArray4983[anIntArray5368[i] & 0xffff]));
            else
                aClass109_5383.method1022((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], (float) (anIntArray5368[i] & 0xffff), (float) (anIntArray5337[i] & 0xffff), (float) (anIntArray5366[i] & 0xffff));
        } else {
            int i_42_ = -16777216;
            if (aByteArray5325 != null) i_42_ = 255 - (aByteArray5325[i] & 0xff) << 24;
            if (anIntArray5366[i] == -1) {
                int i_43_ = i_42_ | anIntArray5368[i] & 0xffffff;
                aClass109_5383.method1024((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_43_, i_43_, i_43_, aClass167_5367.anInt2192, 0, 0, 0, aShortArray5388[i]);
            } else
                aClass109_5383.method1024((float) anIntArray5343[i_39_], (float) anIntArray5343[i_40_], (float) anIntArray5343[i_41_], (float) anIntArray5321[i_39_], (float) anIntArray5321[i_40_], (float) anIntArray5321[i_41_], (float) anIntArray5355[i_39_], (float) anIntArray5355[i_40_], (float) anIntArray5355[i_41_], aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_42_ | anIntArray5368[i] & 0xffffff, i_42_ | anIntArray5337[i] & 0xffffff, i_42_ | anIntArray5366[i] & 0xffffff, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
        }
    }

    final void method615(Class101 class101, Class318_Sub3 class318_sub3, int i) {
        method654(class101, class318_sub3, -1, i);
    }

    final int HA() {
        if (!aBoolean5323) method655();
        return aShort5352;
    }

    private final void method636() {
        if (anInt5354 == 0 && aClass360Array5360 == null) {
            if (aHa_Sub1_5353.anInt7485 > 1) {
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
        return anInt5349;
    }

    final boolean method618() {
        if (aShortArray5388 == null) return true;
        for (int i = 0; i < aShortArray5388.length; i++) {
            if (aShortArray5388[i] != -1 && !aHa_Sub1_5353.method3725(aShortArray5388[i])) return false;
        }
        return true;
    }

    final void method612() {
        /* empty */
    }

    private final int method642(int i, short i_305_, int i_306_) {
        int i_307_ = Class10.anIntArray179[method637(i, i_306_)];
        Class12 class12 = aHa_Sub1_5353.aD4579.method3(i_305_ & 0xffff, -6662);
        int i_308_ = class12.aByte201 & 0xff;
        if (i_308_ != 0) {
            int i_309_ = 131586 * i_306_;
            if (i_308_ == 256) i_307_ = i_309_;
            else {
                int i_310_ = i_308_;
                int i_311_ = 256 - i_308_;
                i_307_ = ((((i_309_ & 0xff00ff) * i_310_ + (i_307_ & 0xff00ff) * i_311_) & ~0xff00ff) + (((i_309_ & 0xff00) * i_310_ + (i_307_ & 0xff00) * i_311_) & 0xff0000)) >> 8;
            }
        }
        int i_312_ = class12.aByte216 & 0xff;
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

    final void method608(Class101 class101, Class318_Sub3 class318_sub3, int i, int i_316_) {
        method654(class101, class318_sub3, i, i_316_);
    }

    final boolean NA() {
        if (anIntArrayArray5334 == null) return false;
        anInt5338 = 0;
        anInt5375 = 0;
        anInt5342 = 0;
        return true;
    }

    private final void method643(int i, boolean bool, boolean bool_317_) {
        if (anIntArray5366[i] != -2) {
            short i_318_ = aShortArray5317[i];
            short i_319_ = aShortArray5394[i];
            short i_320_ = aShortArray5364[i];
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
                aClass109_5383.aBoolean1671 = i_321_ < 0 || i_322_ < 0 || i_323_ < 0 || i_321_ > aClass167_5367.anInt2221 || i_322_ > aClass167_5367.anInt2221 || i_323_ > aClass167_5367.anInt2221;
                if (bool_317_) {
                    int i_336_ = anIntArray5400[i];
                    if (i_336_ == -1 || !aClass6Array5361[i_336_].aBoolean145) method658(i);
                } else {
                    int i_337_ = anIntArray5400[i];
                    if (i_337_ != -1) {
                        Class6 class6 = aClass6Array5361[i_337_];
                        Class350 class350 = aClass350Array5363[i_337_];
                        if (!class6.aBoolean145) method635(i);
                        aHa_Sub1_5353.method3720(class350.anInt4312, class350.anInt4310, class350.anInt4320, class350.anInt4309, class350.anInt4307, class350.anInt4308, class6.aShort146 & 0xffff, class350.anInt4313, class6.aByte148, class6.aByte156);
                    } else method635(i);
                }
            }
        }
    }

    final boolean F() {
        return aBoolean5382;
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
        int i_541_ = aHa_Sub1_5353.anInt7482;
        short i_542_ = aShortArray5317[i];
        short i_543_ = aShortArray5394[i];
        short i_544_ = aShortArray5364[i];
        int i_545_ = anIntArray5392[i_542_];
        int i_546_ = anIntArray5392[i_543_];
        int i_547_ = anIntArray5392[i_544_];
        if (aByteArray5325 == null) aClass109_5383.anInt1674 = 0;
        else aClass109_5383.anInt1674 = aByteArray5325[i] & 0xff;
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
                anIntArray5315[i_540_] = (aClass167_5367.anInt2229 + ((i_548_ + ((anIntArray5399[i_544_] - i_548_) * i_551_ >> 16)) * aHa_Sub1_5353.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aClass167_5367.anInt2215 + ((i_549_ + ((anIntArray5384[i_544_] - i_549_) * i_551_ >> 16)) * aHa_Sub1_5353.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_550_ + (((anIntArray5366[i] & 0xffff) - i_550_) * i_551_ >> 16));
            }
            if (i_546_ >= i_541_) {
                int i_552_ = (i_541_ - i_545_) * (65536 / (i_546_ - i_545_));
                anIntArray5315[i_540_] = (aClass167_5367.anInt2229 + ((i_548_ + ((anIntArray5399[i_543_] - i_548_) * i_552_ >> 16)) * aHa_Sub1_5353.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aClass167_5367.anInt2215 + ((i_549_ + ((anIntArray5384[i_543_] - i_549_) * i_552_ >> 16)) * aHa_Sub1_5353.anInt7497 / i_541_));
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
                anIntArray5315[i_540_] = (aClass167_5367.anInt2229 + ((i_553_ + ((anIntArray5399[i_542_] - i_553_) * i_556_ >> 16)) * aHa_Sub1_5353.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aClass167_5367.anInt2215 + ((i_554_ + ((anIntArray5384[i_542_] - i_554_) * i_556_ >> 16)) * aHa_Sub1_5353.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_555_ + (((anIntArray5368[i] & 0xffff) - i_555_) * i_556_ >> 16));
            }
            if (i_547_ >= i_541_) {
                int i_557_ = (i_541_ - i_546_) * (65536 / (i_547_ - i_546_));
                anIntArray5315[i_540_] = (aClass167_5367.anInt2229 + ((i_553_ + ((anIntArray5399[i_544_] - i_553_) * i_557_ >> 16)) * aHa_Sub1_5353.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aClass167_5367.anInt2215 + ((i_554_ + ((anIntArray5384[i_544_] - i_554_) * i_557_ >> 16)) * aHa_Sub1_5353.anInt7497 / i_541_));
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
                anIntArray5315[i_540_] = (aClass167_5367.anInt2229 + ((i_558_ + ((anIntArray5399[i_543_] - i_558_) * i_561_ >> 16)) * aHa_Sub1_5353.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aClass167_5367.anInt2215 + ((i_559_ + ((anIntArray5384[i_543_] - i_559_) * i_561_ >> 16)) * aHa_Sub1_5353.anInt7497 / i_541_));
                anIntArray5381[i_540_] = i_541_;
                anIntArray5377[i_540_++] = (i_560_ + (((anIntArray5337[i] & 0xffff) - i_560_) * i_561_ >> 16));
            }
            if (i_545_ >= i_541_) {
                int i_562_ = (i_541_ - i_547_) * (65536 / (i_545_ - i_547_));
                anIntArray5315[i_540_] = (aClass167_5367.anInt2229 + ((i_558_ + ((anIntArray5399[i_542_] - i_558_) * i_562_ >> 16)) * aHa_Sub1_5353.anInt7491 / i_541_));
                anIntArray5371[i_540_] = (aClass167_5367.anInt2215 + ((i_559_ + ((anIntArray5384[i_542_] - i_559_) * i_562_ >> 16)) * aHa_Sub1_5353.anInt7497 / i_541_));
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
        aClass109_5383.aBoolean1671 = false;
        if (i_540_ == 3) {
            if (i_563_ < 0 || i_564_ < 0 || i_565_ < 0 || i_563_ > aClass167_5367.anInt2221 || i_564_ > aClass167_5367.anInt2221 || i_565_ > aClass167_5367.anInt2221) aClass109_5383.aBoolean1671 = true;
            if (aShortArray5388 == null || aShortArray5388[i] == -1) {
                if (anIntArray5366[i] == -1) aClass109_5383.method1018((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, (Class126.anIntArray4983[anIntArray5368[i] & 0xffff]));
                else aClass109_5383.method1022((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, (float) anIntArray5377[0], (float) anIntArray5377[1], (float) anIntArray5377[2]);
            } else {
                int i_569_ = -16777216;
                if (aByteArray5325 != null) i_569_ = 255 - (aByteArray5325[i] & 0xff) << 24;
                int i_570_ = i_569_ | anIntArray5368[i] & 0xffffff;
                if (anIntArray5366[i] == -1)
                    aClass109_5383.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_570_, i_570_, i_570_, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
                else
                    aClass109_5383.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_570_, i_570_, i_570_, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
            }
        }
        if (i_540_ == 4) {
            if (i_563_ < 0 || i_564_ < 0 || i_565_ < 0 || i_563_ > aClass167_5367.anInt2221 || i_564_ > aClass167_5367.anInt2221 || i_565_ > aClass167_5367.anInt2221 || anIntArray5315[3] < 0 || anIntArray5315[3] > aClass167_5367.anInt2221) aClass109_5383.aBoolean1671 = true;
            if (aShortArray5388 == null || aShortArray5388[i] == -1) {
                if (anIntArray5366[i] == -1) {
                    int i_571_ = Class126.anIntArray4983[anIntArray5368[i] & 0xffff];
                    aClass109_5383.method1018((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, i_571_);
                    aClass109_5383.method1018((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_546_, (float) anIntArray5381[3], i_571_);
                } else {
                    aClass109_5383.method1022((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, (float) anIntArray5377[0], (float) anIntArray5377[1], (float) anIntArray5377[2]);
                    aClass109_5383.method1022((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_546_, (float) anIntArray5381[3], (float) anIntArray5377[0], (float) anIntArray5377[2], (float) anIntArray5377[3]);
                }
            } else {
                int i_572_ = -16777216;
                if (aByteArray5325 != null) i_572_ = 255 - (aByteArray5325[i] & 0xff) << 24;
                int i_573_ = i_572_ | anIntArray5368[i] & 0xffffff;
                if (anIntArray5366[i] == -1) {
                    aClass109_5383.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_573_, i_573_, i_573_, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
                    aClass109_5383.method1024((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_547_, (float) anIntArray5381[3], aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_573_, i_573_, i_573_, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
                } else {
                    aClass109_5383.method1024((float) i_566_, (float) i_567_, (float) i_568_, (float) i_563_, (float) i_564_, (float) i_565_, (float) i_545_, (float) i_546_, (float) i_547_, aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_573_, i_573_, i_573_, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
                    aClass109_5383.method1024((float) i_566_, (float) i_568_, (float) anIntArray5371[3], (float) i_563_, (float) i_565_, (float) anIntArray5315[3], (float) i_545_, (float) i_547_, (float) anIntArray5381[3], aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_573_, i_573_, i_573_, (aClass167_5367.anInt2192), 0, 0, 0, aShortArray5388[i]);
                }
            }
        }
    }

    // dependency of method657, not in genuine list
    private final void method647() {
        if (anInt5354 == 0) method634(false);
        else if (aHa_Sub1_5353.anInt7485 > 1) {
            synchronized (this) {
                method640();
            }
        } else method640();
    }

    // dependency of method647, not in genuine list
    private final void method640() {
        for (int i = 0; i < anInt5351; i++) {
            short i_287_ = aShortArray5388 != null ? aShortArray5388[i] : (short) -1;
            if (i_287_ == -1) {
                int i_288_ = aShortArray5311[i] & 0xffff;
                int i_289_ = (i_288_ & 0x7f) * anInt5344 >> 7;
                short i_290_ = Class25.method303(i_288_ & ~0x7f | i_289_, 30);
                if (anIntArray5366[i] == -1) {
                    int i_291_ = anIntArray5368[i] & ~0x1ffff;
                    anIntArray5368[i] = i_291_ | Class291.method2198(0, i_291_ >> 17, i_290_);
                } else if (anIntArray5366[i] != -2) {
                    int i_292_ = anIntArray5368[i] & ~0x1ffff;
                    anIntArray5368[i] = i_292_ | Class291.method2198(0, i_292_ >> 17, i_290_);
                    i_292_ = anIntArray5337[i] & ~0x1ffff;
                    anIntArray5337[i] = i_292_ | Class291.method2198(0, i_292_ >> 17, i_290_);
                    i_292_ = anIntArray5366[i] & ~0x1ffff;
                    anIntArray5366[i] = i_292_ | Class291.method2198(0, i_292_ >> 17, i_290_);
                }
            }
        }
        anInt5354 = 2;
    }

    final void C(int i) {
        if ((anInt5316 & 0x1000) != 4096) throw new IllegalStateException();
        anInt5344 = i;
        anInt5354 = 0;
    }

    final int RA() {
        if (!aBoolean5323) method655();
        return aShort5393;
    }

    final boolean method623(int i, int i_474_, Class101 class101, boolean bool, int i_475_, int i_476_) {
        return method645(i, i_474_, class101, bool, i_475_, i_476_);
    }

    final Class342[] method604() {
        return aClass342Array5335;
    }

    final int WA() {
        return anInt5344;
    }

    final void method621() {
        if (aHa_Sub1_5353.anInt7485 > 1) {
            synchronized (this) {
                this.aBoolean1124 = false;
                this.notifyAll();
            }
        }
    }

    final boolean method628(int i, int i_486_, Class101 class101, boolean bool, int i_487_) {
        return method645(i, i_486_, class101, bool, i_487_, -1);
    }

    final int ma() {
        if (!aBoolean5323) method655();
        return aShort5348;
    }

    // dependency of method623 and method628, not in genuine list
    private final boolean method645(int i, int i_488_, Class101 class101, boolean bool, int i_489_, int i_490_) {
        aClass101_Sub1_5320 = (Class101_Sub1) class101;
        Class101_Sub1 class101_sub1 = aHa_Sub1_5353.aClass101_Sub1_7492;
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
        int i_503_ = aHa_Sub1_5353.anInt7510;
        int i_504_ = aHa_Sub1_5353.anInt7504;
        int i_505_ = aHa_Sub1_5353.anInt7491;
        int i_506_ = aHa_Sub1_5353.anInt7497;
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
            if (f_529_ >= (float) aHa_Sub1_5353.anInt7482) {
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
            for (int i_532_ = 0; i_532_ < anInt5340; i_532_++) {
                int i_533_ = anIntArray5356[i_532_];
                int i_534_ = anIntArray5332[i_532_];
                int i_535_ = anIntArray5312[i_532_];
                float f_536_ = f + (f_493_ * (float) i_533_ + f_494_ * (float) i_534_ + f_495_ * (float) i_535_);
                float f_537_ = f_491_ + (f_496_ * (float) i_533_ + f_497_ * (float) i_534_ + f_498_ * (float) i_535_);
                float f_538_ = f_492_ + (f_499_ * (float) i_533_ + f_500_ * (float) i_534_ + f_501_ * (float) i_535_);
                if (f_538_ >= (float) aHa_Sub1_5353.anInt7482) {
                    if (i_490_ > 0) f_538_ = (float) i_490_;
                    anIntArray5321[i_532_] = i_503_ + (int) (f_536_ * (float) i_505_ / f_538_);
                    anIntArray5343[i_532_] = i_504_ + (int) (f_537_ * (float) i_506_ / f_538_);
                } else anIntArray5321[i_532_] = -999999;
            }
            for (int i_539_ = 0; i_539_ < anInt5351; i_539_++) {
                if (anIntArray5321[aShortArray5317[i_539_]] != -999999 && anIntArray5321[aShortArray5394[i_539_]] != -999999 && anIntArray5321[aShortArray5364[i_539_]] != -999999 && method630(i, i_488_, anIntArray5343[aShortArray5317[i_539_]], anIntArray5343[aShortArray5394[i_539_]], anIntArray5343[aShortArray5364[i_539_]], anIntArray5321[aShortArray5317[i_539_]], anIntArray5321[aShortArray5394[i_539_]], anIntArray5321[aShortArray5364[i_539_]]))
                    return true;
            }
        }
        return false;
    }

    final void LA(int i) {
        if ((anInt5316 & 0x2000) != 8192) throw new IllegalStateException();
        anInt5349 = i;
        anInt5354 = 0;
    }

    private final void method649() {
        aClass360Array5360 = new Class360[anInt5387];
        for (int i = 0; i < anInt5387; i++)
            aClass360Array5360[i] = new Class360();
        for (int i = 0; i < anInt5351; i++) {
            short i_599_ = aShortArray5317[i];
            short i_600_ = aShortArray5394[i];
            short i_601_ = aShortArray5364[i];
            int i_602_ = anIntArray5356[i_600_] - anIntArray5356[i_599_];
            int i_603_ = anIntArray5332[i_600_] - anIntArray5332[i_599_];
            int i_604_ = anIntArray5312[i_600_] - anIntArray5312[i_599_];
            int i_605_ = anIntArray5356[i_601_] - anIntArray5356[i_599_];
            int i_606_ = anIntArray5332[i_601_] - anIntArray5332[i_599_];
            int i_607_ = anIntArray5312[i_601_] - anIntArray5312[i_599_];
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
            if (aByteArray5386 == null) i_612_ = (byte) 0;
            else i_612_ = aByteArray5386[i];
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
                if (aClass41Array5385 == null) aClass41Array5385 = new Class41[anInt5351];
                Class41 class41 = aClass41Array5385[i] = new Class41();
                class41.anInt561 = i_608_;
                class41.anInt560 = i_609_;
                class41.anInt559 = i_610_;
            }
        }
    }

    private final void method650(boolean bool, boolean bool_616_, int i, int i_617_) {
        if (aClass6Array5361 != null) {
            for (int i_618_ = 0; i_618_ < anInt5389; i_618_++) {
                Class6 class6 = aClass6Array5361[i_618_];
                anIntArray5400[class6.anInt144] = i_618_;
            }
        }
        if (aBoolean5382 || aClass6Array5361 != null) {
            if ((anInt5316 & 0x100) == 0 && aShortArray5327 != null) {
                for (int i_619_ = 0; i_619_ < anInt5351; i_619_++) {
                    short i_620_ = aShortArray5327[i_619_];
                    method643(i_620_, bool, bool_616_);
                }
            } else {
                for (int i_621_ = 0; i_621_ < anInt5351; i_621_++) {
                    if (!method629(i_621_) && !method638(i_621_)) method643(i_621_, bool, bool_616_);
                }
                if (aByteArray5358 == null) {
                    for (int i_622_ = 0; i_622_ < anInt5351; i_622_++) {
                        if (method629(i_622_) || method638(i_622_)) method643(i_622_, bool, bool_616_);
                    }
                } else {
                    for (int i_623_ = 0; i_623_ < 12; i_623_++) {
                        for (int i_624_ = 0; i_624_ < anInt5351; i_624_++) {
                            if (aByteArray5358[i_624_] == i_623_ && (method629(i_624_) || method638(i_624_))) method643(i_624_, bool, bool_616_);
                        }
                    }
                }
            }
        } else {
            for (int i_625_ = 0; i_625_ < anInt5351; i_625_++)
                method643(i_625_, bool, bool_616_);
        }
    }

    private final void method654(Class101 class101, Class318_Sub3 class318_sub3, int i, int i_632_) {
        if (anInt5387 >= 1) {
            aClass101_Sub1_5320 = (Class101_Sub1) class101;
            Class101_Sub1 class101_sub1 = aHa_Sub1_5353.aClass101_Sub1_7492;
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
            if (i_636_ < aHa_Sub1_5353.anInt7494 && i_637_ > aHa_Sub1_5353.anInt7482) {
                float f_638_ = (class101_sub1.aFloat5686 + (class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5673 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5669 * aClass101_Sub1_5320.aFloat5681));
                float f_639_ = (bool ? class101_sub1.aFloat5673 : ((class101_sub1.aFloat5672 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5673 * (aClass101_Sub1_5320.aFloat5678)) + (class101_sub1.aFloat5669 * (aClass101_Sub1_5320.aFloat5680))));
                int i_640_ = (int) (f_638_ + (float) aShort5329 * f_639_);
                int i_641_ = (int) (f_638_ + (float) aShort5365 * f_639_);
                int i_642_;
                int i_643_;
                if (i_640_ > i_641_) {
                    i_642_ = ((i_641_ - aShort5324) * aHa_Sub1_5353.anInt7491);
                    i_643_ = ((i_640_ + aShort5324) * aHa_Sub1_5353.anInt7491);
                } else {
                    i_642_ = ((i_640_ - aShort5324) * aHa_Sub1_5353.anInt7491);
                    i_643_ = ((i_641_ + aShort5324) * aHa_Sub1_5353.anInt7491);
                }
                if (i == -1) {
                    if (i_642_ / i_637_ >= aHa_Sub1_5353.anInt7508 || (i_643_ / i_637_ <= aHa_Sub1_5353.anInt7509)) return;
                } else if (i_642_ / i >= aHa_Sub1_5353.anInt7508 || (i_643_ / i <= aHa_Sub1_5353.anInt7509)) return;
                float f_644_ = (class101_sub1.aFloat5685 + (class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5686) + (class101_sub1.aFloat5678 * aClass101_Sub1_5320.aFloat5685) + (class101_sub1.aFloat5666 * aClass101_Sub1_5320.aFloat5681));
                float f_645_ = (bool ? class101_sub1.aFloat5678 : ((class101_sub1.aFloat5655 * aClass101_Sub1_5320.aFloat5673) + (class101_sub1.aFloat5678 * (aClass101_Sub1_5320.aFloat5678)) + (class101_sub1.aFloat5666 * (aClass101_Sub1_5320.aFloat5680))));
                int i_646_ = (int) (f_644_ + (float) aShort5329 * f_645_);
                int i_647_ = (int) (f_644_ + (float) aShort5365 * f_645_);
                int i_648_;
                int i_649_;
                if (i_646_ > i_647_) {
                    i_648_ = ((i_647_ - aShort5324) * aHa_Sub1_5353.anInt7497);
                    i_649_ = ((i_646_ + aShort5324) * aHa_Sub1_5353.anInt7497);
                } else {
                    i_648_ = ((i_646_ - aShort5324) * aHa_Sub1_5353.anInt7497);
                    i_649_ = ((i_647_ + aShort5324) * aHa_Sub1_5353.anInt7497);
                }
                if (i == -1) {
                    if (i_648_ / i_637_ >= aHa_Sub1_5353.anInt7506 || (i_649_ / i_637_ <= aHa_Sub1_5353.anInt7490)) return;
                } else if (i_648_ / i >= aHa_Sub1_5353.anInt7506 || (i_649_ / i <= aHa_Sub1_5353.anInt7490)) return;
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
                if (aHa_Sub1_5353.anInt7485 > 1) {
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
                aClass109_5383.method1023((i_632_ & 0x2) != 0);
                boolean bool_656_ = false;
                boolean bool_657_ = i_636_ <= aHa_Sub1_5353.anInt7482;
                boolean bool_658_ = (bool_657_ || aClass129Array5322 != null || aClass342Array5335 != null);
                aClass167_5367.anInt2221 = aClass109_5383.anInt1679;
                aClass167_5367.anInt2229 = aClass109_5383.anInt1665;
                aClass167_5367.anInt2215 = aClass109_5383.anInt1668;
                int i_659_ = aHa_Sub1_5353.anInt7491;
                int i_660_ = aHa_Sub1_5353.anInt7497;
                int i_661_ = aHa_Sub1_5353.anInt7482;
                if (i == -1) {
                    for (int i_662_ = 0; i_662_ < anInt5340; i_662_++) {
                        int i_663_ = anIntArray5356[i_662_];
                        int i_664_ = anIntArray5332[i_662_];
                        int i_665_ = anIntArray5312[i_662_];
                        float f_666_ = (f_638_ + f_650_ * (float) i_663_ + f_639_ * (float) i_664_ + f_653_ * (float) i_665_);
                        float f_667_ = (f_644_ + f_651_ * (float) i_663_ + f_645_ * (float) i_664_ + f_654_ * (float) i_665_);
                        float f_668_ = (f + f_652_ * (float) i_663_ + f_633_ * (float) i_664_ + f_655_ * (float) i_665_);
                        anIntArray5355[i_662_] = (int) f_668_;
                        if (f_668_ >= (float) i_661_) {
                            anIntArray5321[i_662_] = (aClass167_5367.anInt2229 + (int) (f_666_ * (float) i_659_ / f_668_));
                            anIntArray5343[i_662_] = (aClass167_5367.anInt2215 + (int) (f_667_ * (float) i_660_ / f_668_));
                        } else {
                            anIntArray5321[i_662_] = -5000;
                            bool_656_ = true;
                        }
                        if (bool_658_) {
                            anIntArray5399[i_662_] = (int) f_666_;
                            anIntArray5384[i_662_] = (int) f_667_;
                            anIntArray5392[i_662_] = (int) f_668_;
                        }
                        if (aClass167_5367.aBoolean2195) anIntArray5362[i_662_] = (int) ((aClass101_Sub1_5320.aFloat5685) + ((aClass101_Sub1_5320.aFloat5655 * (float) i_663_) + (aClass101_Sub1_5320.aFloat5678 * (float) i_664_) + (aClass101_Sub1_5320.aFloat5666 * (float) i_665_)));
                    }
                    if (aClass6Array5361 != null) {
                        for (int i_669_ = 0; i_669_ < anInt5389; i_669_++) {
                            Class6 class6 = aClass6Array5361[i_669_];
                            Class350 class350 = aClass350Array5363[i_669_];
                            short i_670_ = aShortArray5317[class6.anInt144];
                            short i_671_ = aShortArray5394[class6.anInt144];
                            short i_672_ = aShortArray5364[class6.anInt144];
                            int i_673_ = ((anIntArray5356[i_670_] + anIntArray5356[i_671_] + anIntArray5356[i_672_]) / 3);
                            int i_674_ = ((anIntArray5332[i_670_] + anIntArray5332[i_671_] + anIntArray5332[i_672_]) / 3);
                            int i_675_ = ((anIntArray5312[i_670_] + anIntArray5312[i_671_] + anIntArray5312[i_672_]) / 3);
                            float f_676_ = ((float) class350.anInt4316 + (f_638_ + f_650_ * (float) i_673_ + f_639_ * (float) i_674_ + f_653_ * (float) i_675_));
                            float f_677_ = ((float) class350.anInt4317 + (f_644_ + f_651_ * (float) i_673_ + f_645_ * (float) i_674_ + f_654_ * (float) i_675_));
                            float f_678_ = (f + f_652_ * (float) i_673_ + f_633_ * (float) i_674_ + f_655_ * (float) i_675_);
                            if (f_678_ > (float) (aHa_Sub1_5353.anInt7482)) {
                                class350.anInt4312 = (aHa_Sub1_5353.anInt7510 + (int) (f_676_ * (float) i_659_ / f_678_));
                                class350.anInt4310 = (aHa_Sub1_5353.anInt7504 + (int) (f_677_ * (float) i_660_ / f_678_));
                                class350.anInt4320 = ((int) f_678_ - class6.anInt154);
                                class350.anInt4309 = (int) ((float) ((class350.anInt4314) * (class6.aShort150) * i_659_) / (f_678_ * 128.0F));
                                class350.anInt4307 = (int) ((float) ((class350.anInt4311) * (class6.aShort143) * i_660_) / (f_678_ * 128.0F));
                            } else class350.anInt4309 = class350.anInt4307 = 0;
                        }
                    }
                } else {
                    for (int i_679_ = 0; i_679_ < anInt5340; i_679_++) {
                        int i_680_ = anIntArray5356[i_679_];
                        int i_681_ = anIntArray5332[i_679_];
                        int i_682_ = anIntArray5312[i_679_];
                        float f_683_ = (f_638_ + f_650_ * (float) i_680_ + f_639_ * (float) i_681_ + f_653_ * (float) i_682_);
                        float f_684_ = (f_644_ + f_651_ * (float) i_680_ + f_645_ * (float) i_681_ + f_654_ * (float) i_682_);
                        float f_685_ = (f + f_652_ * (float) i_680_ + f_633_ * (float) i_681_ + f_655_ * (float) i_682_);
                        anIntArray5355[i_679_] = (int) f_685_;
                        anIntArray5321[i_679_] = (aClass167_5367.anInt2229 + (int) (f_683_ * (float) i_659_ / (float) i));
                        anIntArray5343[i_679_] = (aClass167_5367.anInt2215 + (int) (f_684_ * (float) i_660_ / (float) i));
                        if (bool_658_) {
                            anIntArray5399[i_679_] = (int) f_683_;
                            anIntArray5384[i_679_] = (int) f_684_;
                            anIntArray5392[i_679_] = i;
                        }
                        if (aClass167_5367.aBoolean2195) anIntArray5362[i_679_] = (int) ((aClass101_Sub1_5320.aFloat5685) + ((aClass101_Sub1_5320.aFloat5655 * (float) i_680_) + (aClass101_Sub1_5320.aFloat5678 * (float) i_681_) + (aClass101_Sub1_5320.aFloat5666 * (float) i_682_)));
                    }
                    if (aClass6Array5361 != null) {
                        for (int i_686_ = 0; i_686_ < anInt5389; i_686_++) {
                            Class6 class6 = aClass6Array5361[i_686_];
                            Class350 class350 = aClass350Array5363[i_686_];
                            short i_687_ = aShortArray5317[class6.anInt144];
                            short i_688_ = aShortArray5394[class6.anInt144];
                            short i_689_ = aShortArray5364[class6.anInt144];
                            int i_690_ = ((anIntArray5356[i_687_] + anIntArray5356[i_688_] + anIntArray5356[i_689_]) / 3);
                            int i_691_ = ((anIntArray5332[i_687_] + anIntArray5332[i_688_] + anIntArray5332[i_689_]) / 3);
                            int i_692_ = ((anIntArray5312[i_687_] + anIntArray5312[i_688_] + anIntArray5312[i_689_]) / 3);
                            float f_693_ = (f_638_ + f_650_ * (float) i_690_ + f_639_ * (float) i_691_ + f_653_ * (float) i_692_);
                            float f_694_ = (f_644_ + f_651_ * (float) i_690_ + f_645_ * (float) i_691_ + f_654_ * (float) i_692_);
                            float f_695_ = (f + f_652_ * (float) i_690_ + f_633_ * (float) i_691_ + f_655_ * (float) i_692_);
                            class350.anInt4312 = (aHa_Sub1_5353.anInt7510 + (int) (f_693_ * (float) i_659_ / (float) i));
                            class350.anInt4310 = (aHa_Sub1_5353.anInt7504 + (int) (f_694_ * (float) i_660_ / (float) i));
                            class350.anInt4320 = i - class6.anInt154;
                            class350.anInt4309 = (class350.anInt4314 * class6.aShort150 * i_659_ / (i << 7));
                            class350.anInt4307 = (class350.anInt4311 * class6.aShort143 * i_660_ / (i << 7));
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
                        class318_sub3.anInt6405 = (aHa_Sub1_5353.anInt7510 + (int) (f_703_ * (float) i_659_ / (float) i_706_));
                        class318_sub3.anInt6402 = (aHa_Sub1_5353.anInt7504 + (int) (f_704_ * (float) i_660_ / (float) i_706_));
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
                        class318_sub3.anInt6406 = (aHa_Sub1_5353.anInt7510 + (int) (f_707_ * (float) i_659_ / (float) i_710_));
                        class318_sub3.anInt6404 = (aHa_Sub1_5353.anInt7504 + (int) (f_708_ * (float) i_660_ / (float) i_710_));
                    } else bool_696_ = true;
                    if (bool_696_) {
                        if (f_705_ < (float) i_661_ && f_709_ < (float) i_661_) bool_697_ = false;
                        else if (f_705_ < (float) i_661_) {
                            float f_711_ = ((f_709_ - (float) (aHa_Sub1_5353.anInt7482)) / (f_709_ - f_705_));
                            int i_712_ = (int) (f_707_ + (f_707_ - f_703_) * f_711_);
                            int i_713_ = (int) (f_708_ + (f_708_ - f_704_) * f_711_);
                            int i_714_ = i_661_;
                            if (i != -1) i_714_ = i;
                            class318_sub3.anInt6405 = (aHa_Sub1_5353.anInt7510 + i_712_ * i_659_ / i_714_);
                            class318_sub3.anInt6402 = (aHa_Sub1_5353.anInt7504 + i_713_ * i_660_ / i_714_);
                        } else if (f_709_ < (float) i_661_) {
                            float f_715_ = ((f_705_ - (float) i_661_) / (f_705_ - f_709_));
                            int i_716_ = (int) (f_703_ + (f_703_ - f_707_) * f_715_);
                            int i_717_ = (int) (f_704_ + (f_704_ - f_708_) * f_715_);
                            int i_718_ = i_661_;
                            if (i != -1) i_718_ = i;
                            class318_sub3.anInt6405 = (aHa_Sub1_5353.anInt7510 + i_716_ * i_659_ / i_718_);
                            class318_sub3.anInt6402 = (aHa_Sub1_5353.anInt7504 + i_717_ * i_660_ / i_718_);
                        }
                    }
                    if (bool_697_) {
                        if (f_705_ > f_709_) {
                            int i_719_ = (int) f_705_;
                            if (i != -1) i_719_ = i;
                            class318_sub3.anInt6403 = (aHa_Sub1_5353.anInt7510 + (int) ((f_703_ + (float) aShort5324) * (float) i_659_ / (float) i_719_) - (class318_sub3.anInt6405));
                        } else {
                            int i_720_ = (int) f_709_;
                            if (i != -1) i_720_ = i;
                            class318_sub3.anInt6403 = (aHa_Sub1_5353.anInt7510 + (int) ((f_707_ + (float) aShort5324) * (float) i_659_ / (float) i_720_) - (class318_sub3.anInt6406));
                        }
                        class318_sub3.aBoolean6401 = true;
                    }
                }
                method634(true);
                aClass109_5383.aBoolean1669 = (i_632_ & 0x1) == 0;
                aClass109_5383.aBoolean1667 = false;
                try {
                    method650(bool_656_, ((aClass167_5367.aBoolean2201 && (i_637_ > aClass167_5367.anInt2210)) || aClass167_5367.aBoolean2195), i_636_, i_637_ - i_636_);
                } catch (Exception exception) {
                    /* empty */
                }
                if (aClass6Array5361 != null) {
                    for (int i_721_ = 0; i_721_ < anInt5351; i_721_++)
                        anIntArray5400[i_721_] = -1;
                }
                aClass109_5383 = null;
                if (aHa_Sub1_5353.anInt7485 > 1) {
                    synchronized (this) {
                        aBoolean5357 = false;
                        this.notifyAll();
                    }
                }
            }
        }
    }

    final boolean r() {
        return aBoolean5391;
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
            for (int i_729_ = 0; i_729_ < anInt5387; i_729_++) {
                int i_730_ = anIntArray5356[i_729_];
                int i_731_ = anIntArray5332[i_729_];
                int i_732_ = anIntArray5312[i_729_];
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
            if ((anInt5316 & 0x97098) == 0 && aFloatArrayArray5314 == null) aShortArray5311 = null;
            if (bool) aByteArray5386 = null;
        } else {
            method636();
            int i = aHa_Sub1_5353.anInt7484;
            int i_734_ = aHa_Sub1_5353.anInt7473;
            int i_735_ = aHa_Sub1_5353.anInt7479;
            int i_736_ = aHa_Sub1_5353.anInt7500 >> 8;
            int i_737_ = aHa_Sub1_5353.anInt7474 * 768 / anInt5349;
            int i_738_ = aHa_Sub1_5353.anInt7478 * 768 / anInt5349;
            if (anIntArray5368 == null) {
                anIntArray5368 = new int[anInt5351];
                anIntArray5337 = new int[anInt5351];
                anIntArray5366 = new int[anInt5351];
            }
            for (int i_739_ = 0; i_739_ < anInt5351; i_739_++) {
                byte i_740_;
                if (aByteArray5386 == null) i_740_ = (byte) 0;
                else i_740_ = aByteArray5386[i_739_];
                byte i_741_;
                if (aByteArray5325 == null) i_741_ = (byte) 0;
                else i_741_ = aByteArray5325[i_739_];
                short i_742_;
                if (aShortArray5388 == null) i_742_ = (short) -1;
                else i_742_ = aShortArray5388[i_739_];
                if (i_741_ == -2) i_740_ = (byte) 3;
                if (i_741_ == -1) i_740_ = (byte) 2;
                if (i_742_ == -1) {
                    if (i_740_ == 0) {
                        int i_743_ = aShortArray5311[i_739_] & 0xffff;
                        int i_744_ = (i_743_ & 0x7f) * anInt5344 >> 7;
                        short i_745_ = Class25.method303(i_743_ & ~0x7f | i_744_, 30);
                        Class360 class360;
                        if (aClass360Array5313 != null && (aClass360Array5313[aShortArray5317[i_739_]] != null)) class360 = aClass360Array5313[aShortArray5317[i_739_]];
                        else class360 = aClass360Array5360[aShortArray5317[i_739_]];
                        int i_746_ = (((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16);
                        int i_747_ = i_746_ > 256 ? i_737_ : i_738_;
                        int i_748_ = (i_736_ >> 1) + (i_747_ * i_746_ >> 17);
                        anIntArray5368[i_739_] = i_748_ << 17 | Class291.method2198(0, i_748_, i_745_);
                        if (aClass360Array5313 != null && (aClass360Array5313[aShortArray5394[i_739_]] != null)) class360 = aClass360Array5313[aShortArray5394[i_739_]];
                        else class360 = aClass360Array5360[aShortArray5394[i_739_]];
                        i_746_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_747_ = i_746_ > 256 ? i_737_ : i_738_;
                        i_748_ = (i_736_ >> 1) + (i_747_ * i_746_ >> 17);
                        anIntArray5337[i_739_] = i_748_ << 17 | Class291.method2198(0, i_748_, i_745_);
                        if (aClass360Array5313 != null && (aClass360Array5313[aShortArray5364[i_739_]] != null)) class360 = aClass360Array5313[aShortArray5364[i_739_]];
                        else class360 = aClass360Array5360[aShortArray5364[i_739_]];
                        i_746_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_747_ = i_746_ > 256 ? i_737_ : i_738_;
                        i_748_ = (i_736_ >> 1) + (i_747_ * i_746_ >> 17);
                        anIntArray5366[i_739_] = i_748_ << 17 | Class291.method2198(0, i_748_, i_745_);
                    } else if (i_740_ == 1) {
                        int i_749_ = aShortArray5311[i_739_] & 0xffff;
                        int i_750_ = (i_749_ & 0x7f) * anInt5344 >> 7;
                        short i_751_ = Class25.method303(i_749_ & ~0x7f | i_750_, 30);
                        Class41 class41 = aClass41Array5385[i_739_];
                        int i_752_ = ((i * class41.anInt561 + i_734_ * class41.anInt560 + i_735_ * class41.anInt559) >> 16);
                        int i_753_ = i_752_ > 256 ? i_737_ : i_738_;
                        int i_754_ = (i_736_ >> 1) + (i_753_ * i_752_ >> 17);
                        anIntArray5368[i_739_] = i_754_ << 17 | Class291.method2198(0, i_754_, i_751_);
                        anIntArray5366[i_739_] = -1;
                    } else if (i_740_ == 3) {
                        anIntArray5368[i_739_] = 128;
                        anIntArray5366[i_739_] = -1;
                    } else anIntArray5366[i_739_] = -2;
                } else {
                    int i_755_ = aShortArray5311[i_739_] & 0xffff;
                    if (i_740_ == 0) {
                        Class360 class360;
                        if (aClass360Array5313 != null && (aClass360Array5313[aShortArray5317[i_739_]] != null)) class360 = aClass360Array5313[aShortArray5317[i_739_]];
                        else class360 = aClass360Array5360[aShortArray5317[i_739_]];
                        int i_756_ = (((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16);
                        int i_757_ = i_756_ > 256 ? i_737_ : i_738_;
                        int i_758_ = method656((i_736_ >> 2) + (i_757_ * i_756_ >> 18));
                        anIntArray5368[i_739_] = i_758_ << 24 | method642(i_755_, i_742_, i_758_);
                        if (aClass360Array5313 != null && (aClass360Array5313[aShortArray5394[i_739_]] != null)) class360 = aClass360Array5313[aShortArray5394[i_739_]];
                        else class360 = aClass360Array5360[aShortArray5394[i_739_]];
                        i_756_ = ((i * class360.anInt4430 + i_734_ * class360.anInt4428 + i_735_ * class360.anInt4427) / class360.anInt4429) >> 16;
                        i_757_ = i_756_ > 256 ? i_737_ : i_738_;
                        i_758_ = method656((i_736_ >> 2) + (i_757_ * i_756_ >> 18));
                        anIntArray5337[i_739_] = i_758_ << 24 | method642(i_755_, i_742_, i_758_);
                        if (aClass360Array5313 != null && (aClass360Array5313[aShortArray5364[i_739_]] != null)) class360 = aClass360Array5313[aShortArray5364[i_739_]];
                        else class360 = aClass360Array5360[aShortArray5364[i_739_]];
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
            if ((anInt5316 & 0x97098) == 0 && aFloatArrayArray5314 == null) aShortArray5311 = null;
            if (bool) aByteArray5386 = null;
            anInt5354 = 2;
        }
    }

    final void O(int i, int i_765_, int i_766_) {
        if (i != 128 && (anInt5316 & 0x1) != 1) throw new IllegalStateException();
        if (i_765_ != 128 && (anInt5316 & 0x2) != 2) throw new IllegalStateException();
        if (i_766_ != 128 && (anInt5316 & 0x4) != 4) throw new IllegalStateException();
        synchronized (this) {
            for (int i_767_ = 0; i_767_ < anInt5340; i_767_++) {
                anIntArray5356[i_767_] = anIntArray5356[i_767_] * i >> 7;
                anIntArray5332[i_767_] = anIntArray5332[i_767_] * i_765_ >> 7;
                anIntArray5312[i_767_] = anIntArray5312[i_767_] * i_766_ >> 7;
            }
            aBoolean5323 = false;
        }
    }

    final int ua() {
        return anInt5316;
    }

    // dependency of method643, not in genuine list
    private final void method658(int i) {
        if (aClass167_5367.aBoolean2195) {
            short i_777_ = aShortArray5317[i];
            short i_778_ = aShortArray5394[i];
            short i_779_ = aShortArray5364[i];
            int i_780_ = 0;
            int i_781_ = 0;
            int i_782_ = 0;
            if (anIntArray5362[i_777_] > aClass167_5367.anInt2197) i_780_ = 255;
            else if (anIntArray5362[i_777_] > aClass167_5367.anInt2211) i_780_ = ((aClass167_5367.anInt2211 - anIntArray5362[i_777_]) * 255 / (aClass167_5367.anInt2211 - aClass167_5367.anInt2197));
            if (anIntArray5362[i_778_] > aClass167_5367.anInt2197) i_781_ = 255;
            else if (anIntArray5362[i_778_] > aClass167_5367.anInt2211) i_781_ = ((aClass167_5367.anInt2211 - anIntArray5362[i_778_]) * 255 / (aClass167_5367.anInt2211 - aClass167_5367.anInt2197));
            if (anIntArray5362[i_779_] > aClass167_5367.anInt2197) i_782_ = 255;
            else if (anIntArray5362[i_779_] > aClass167_5367.anInt2211) i_782_ = ((aClass167_5367.anInt2211 - anIntArray5362[i_779_]) * 255 / (aClass167_5367.anInt2211 - aClass167_5367.anInt2197));
            if (aByteArray5325 == null) aClass109_5383.anInt1674 = 0;
            else aClass109_5383.anInt1674 = aByteArray5325[i] & 0xff;
            if (aShortArray5388 == null || aShortArray5388[i] == -1) {
                if (anIntArray5366[i] == -1)
                    aClass109_5383.method1027((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], Class6.method206((Class126.anIntArray4983[(anIntArray5368[i] & 0xffff)]), (i_780_ << 24 | (aClass167_5367.anInt2192)), 255), Class6.method206((Class126.anIntArray4983[(anIntArray5368[i] & 0xffff)]), (i_781_ << 24 | (aClass167_5367.anInt2192)), 255), Class6.method206((Class126.anIntArray4983[(anIntArray5368[i] & 0xffff)]), (i_782_ << 24 | (aClass167_5367.anInt2192)), 255));
                else
                    aClass109_5383.method1027((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], Class6.method206((Class126.anIntArray4983[(anIntArray5368[i] & 0xffff)]), (i_780_ << 24 | (aClass167_5367.anInt2192)), 255), Class6.method206((Class126.anIntArray4983[(anIntArray5337[i] & 0xffff)]), (i_781_ << 24 | (aClass167_5367.anInt2192)), 255), Class6.method206((Class126.anIntArray4983[(anIntArray5366[i] & 0xffff)]), (i_782_ << 24 | (aClass167_5367.anInt2192)), 255));
            } else {
                int i_775_ = -16777216;
                if (aByteArray5325 != null) i_775_ = 255 - (aByteArray5325[i] & 0xff) << 24;
                if (anIntArray5366[i] == -1) {
                    int i_776_ = i_775_ | anIntArray5368[i] & 0xffffff;
                    aClass109_5383.method1024((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_776_, i_776_, i_776_, aClass167_5367.anInt2192, i_780_, i_781_, i_782_, aShortArray5388[i]);
                } else
                    aClass109_5383.method1024((float) anIntArray5343[i_777_], (float) anIntArray5343[i_778_], (float) anIntArray5343[i_779_], (float) anIntArray5321[i_777_], (float) anIntArray5321[i_778_], (float) anIntArray5321[i_779_], (float) anIntArray5355[i_777_], (float) anIntArray5355[i_778_], (float) anIntArray5355[i_779_], aFloatArrayArray5314[i][0], aFloatArrayArray5314[i][1], aFloatArrayArray5314[i][2], aFloatArrayArray5345[i][0], aFloatArrayArray5345[i][1], aFloatArrayArray5345[i][2], i_775_ | anIntArray5368[i] & 0xffffff, i_775_ | anIntArray5337[i] & 0xffffff, i_775_ | anIntArray5366[i] & 0xffffff, aClass167_5367.anInt2192, i_780_, i_781_, i_782_, aShortArray5388[i]);
            }
        }
    }

    Class64_Sub1(ha_Sub1 var_ha_Sub1) {
        anInt5354 = 0;
        aBoolean5369 = false;
        anInt5351 = 0;
        aBoolean5372 = false;
        aBoolean5391 = false;
        anInt5387 = 0;
        aBoolean5382 = false;
        aHa_Sub1_5353 = var_ha_Sub1;
    }

    Class64_Sub1(ha_Sub1 var_ha_Sub1, Class124 class124, int i, int i_785_, int i_786_, int i_787_) {
        anInt5354 = 0;
        aBoolean5369 = false;
        anInt5351 = 0;
        aBoolean5372 = false;
        aBoolean5391 = false;
        anInt5387 = 0;
        aBoolean5382 = false;
        aHa_Sub1_5353 = var_ha_Sub1;
        anInt5316 = i;
        anInt5344 = i_785_;
        anInt5349 = i_786_;
        d var_d = aHa_Sub1_5353.aD4579;
        anInt5340 = class124.anInt1836;
        anInt5387 = class124.anInt1821;
        anIntArray5356 = class124.anIntArray1841;
        anIntArray5332 = class124.anIntArray1847;
        anIntArray5312 = class124.anIntArray1852;
        anInt5351 = class124.anInt1817;
        aShortArray5317 = class124.aShortArray1863;
        aShortArray5394 = class124.aShortArray1835;
        aShortArray5364 = class124.aShortArray1855;
        aByteArray5358 = class124.aByteArray1839;
        aShortArray5311 = class124.aShortArray1862;
        aByteArray5325 = class124.aByteArray1834;
        aShortArray5370 = class124.aShortArray1856;
        aByteArray5386 = class124.aByteArray1843;
        aClass129Array5322 = class124.aClass129Array1846;
        aClass342Array5335 = class124.aClass342Array1866;
        aShortArray5333 = class124.aShortArray1842;
        int[] is = new int[anInt5351];
        for (int i_788_ = 0; i_788_ < anInt5351; i_788_++)
            is[i_788_] = i_788_;
        long[] ls = new long[anInt5351];
        boolean bool = (anInt5316 & 0x100) != 0;
        for (int i_789_ = 0; i_789_ < anInt5351; i_789_++) {
            int i_790_ = is[i_789_];
            Class12 class12 = null;
            int i_791_ = 0;
            int i_792_ = 0;
            int i_793_ = 0;
            int i_794_ = 0;
            if (class124.aClass162Array1832 != null) {
                boolean bool_795_ = false;
                for (int i_796_ = 0; i_796_ < class124.aClass162Array1832.length; i_796_++) {
                    Class162 class162 = class124.aClass162Array1832[i_796_];
                    if (i_790_ == class162.anInt2155) {
                        Class189 class189 = Class73.method742(104, (class162.anInt2153));
                        if (class189.aBoolean2531) bool_795_ = true;
                        if (class189.anInt2525 != -1) {
                            Class12 class12_797_ = var_d.method3((class189.anInt2525), -6662);
                            if (class12_797_.anInt200 == 2) aBoolean5382 = true;
                        }
                    }
                }
                if (bool_795_) ls[i_789_] = 9223372036854775807L;
            }
            int i_798_ = -1;
            if (class124.aShortArray1822 != null) {
                i_798_ = class124.aShortArray1822[i_790_];
                if (i_798_ != -1) {
                    class12 = var_d.method3(i_798_ & 0xffff, -6662);
                    if ((i_787_ & 0x40) == 0 || !class12.aBoolean209) {
                        i_793_ = class12.aByte213;
                        i_794_ = class12.aByte202;
                    } else i_798_ = -1;
                }
            }
            boolean bool_799_ = (aByteArray5325 != null && aByteArray5325[i_790_] != 0 || class12 != null && class12.anInt200 == 2);
            if ((bool || bool_799_) && aByteArray5358 != null) i_791_ += aByteArray5358[i_790_] << 17;
            if (bool_799_) i_791_ += 65536;
            i_791_ += (i_793_ & 0xff) << 8;
            i_791_ += i_794_ & 0xff;
            i_792_ += (i_798_ & 0xffff) << 16;
            i_792_ += i_789_ & 0xffff;
            ls[i_789_] = ((long) i_791_ << 32) + (long) i_792_;
            aBoolean5382 |= bool_799_;
        }
        Class348_Sub16_Sub2.method2832(is, ls, 0);
        if (class124.aClass162Array1832 != null) {
            anInt5389 = class124.aClass162Array1832.length;
            aClass6Array5361 = new Class6[anInt5389];
            aClass350Array5363 = new Class350[anInt5389];
            for (int i_800_ = 0; i_800_ < class124.aClass162Array1832.length; i_800_++) {
                Class162 class162 = class124.aClass162Array1832[i_800_];
                Class189 class189 = Class73.method742(104, class162.anInt2153);
                int i_801_ = ((Class126.anIntArray4983[(class124.aShortArray1862[class162.anInt2155]) & 0xffff]) & 0xffffff);
                i_801_ = (i_801_ | 255 - (class124.aByteArray1834 != null ? (class124.aByteArray1834[class162.anInt2155]) & 0xff : 0) << 24);
                aClass6Array5361[i_800_] = new Class6(class162.anInt2155, (class124.aShortArray1863[class162.anInt2155]), (class124.aShortArray1835[class162.anInt2155]), (class124.aShortArray1855[class162.anInt2155]), class189.anInt2526, class189.anInt2530, class189.anInt2525, class189.anInt2533, class189.anInt2534, class189.aBoolean2531, class162.anInt2158);
                aClass350Array5363[i_800_] = new Class350(i_801_);
            }
        }
        aFloatArrayArray5314 = new float[anInt5351][];
        aFloatArrayArray5345 = new float[anInt5351][];
        Class358 class358 = Class59_Sub2_Sub1.method565(255, anInt5351, class124, is);
        Class167 class167 = aHa_Sub1_5353.method3724(Thread.currentThread());
        float[] fs = class167.aFloatArray2226;
        boolean bool_802_ = false;
        for (int i_803_ = 0; i_803_ < anInt5351; i_803_++) {
            int i_804_ = is[i_803_];
            int i_805_;
            if (class124.aByteArray1820 == null) i_805_ = -1;
            else i_805_ = class124.aByteArray1820[i_804_];
            int i_806_ = (class124.aShortArray1822 == null ? -1 : class124.aShortArray1822[i_804_]);
            if (i_806_ != -1 && (i_787_ & 0x40) != 0) {
                Class12 class12 = var_d.method3(i_806_ & 0xffff, -6662);
                if (class12.aBoolean209) i_806_ = -1;
            }
            if (i_806_ != -1) {
                bool_802_ = true;
                float[] fs_807_ = aFloatArrayArray5314[i_804_] = new float[3];
                float[] fs_808_ = aFloatArrayArray5345[i_804_] = new float[3];
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
                    byte i_810_ = class124.aByteArray1823[i_805_];
                    if (i_810_ == 0) {
                        short i_811_ = aShortArray5317[i_804_];
                        short i_812_ = aShortArray5394[i_804_];
                        short i_813_ = aShortArray5364[i_804_];
                        short i_814_ = class124.aShortArray1829[i_805_];
                        short i_815_ = class124.aShortArray1849[i_805_];
                        short i_816_ = class124.aShortArray1825[i_805_];
                        float f = (float) anIntArray5356[i_814_];
                        float f_817_ = (float) anIntArray5332[i_814_];
                        float f_818_ = (float) anIntArray5312[i_814_];
                        float f_819_ = (float) anIntArray5356[i_815_] - f;
                        float f_820_ = (float) anIntArray5332[i_815_] - f_817_;
                        float f_821_ = (float) anIntArray5312[i_815_] - f_818_;
                        float f_822_ = (float) anIntArray5356[i_816_] - f;
                        float f_823_ = (float) anIntArray5332[i_816_] - f_817_;
                        float f_824_ = (float) anIntArray5312[i_816_] - f_818_;
                        float f_825_ = (float) anIntArray5356[i_811_] - f;
                        float f_826_ = (float) anIntArray5332[i_811_] - f_817_;
                        float f_827_ = (float) anIntArray5312[i_811_] - f_818_;
                        float f_828_ = (float) anIntArray5356[i_812_] - f;
                        float f_829_ = (float) anIntArray5332[i_812_] - f_817_;
                        float f_830_ = (float) anIntArray5312[i_812_] - f_818_;
                        float f_831_ = (float) anIntArray5356[i_813_] - f;
                        float f_832_ = (float) anIntArray5332[i_813_] - f_817_;
                        float f_833_ = (float) anIntArray5312[i_813_] - f_818_;
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
                        short i_841_ = aShortArray5317[i_804_];
                        short i_842_ = aShortArray5394[i_804_];
                        short i_843_ = aShortArray5364[i_804_];
                        int i_844_ = class358.anIntArray4416[i_805_];
                        int i_845_ = class358.anIntArray4415[i_805_];
                        int i_846_ = class358.anIntArray4414[i_805_];
                        float[] fs_847_ = (class358.aFloatArrayArray4412[i_805_]);
                        byte i_848_ = class124.aByteArray1853[i_805_];
                        float f = ((float) (class124.anIntArray1867[i_805_]) / 256.0F);
                        if (i_810_ == 1) {
                            float f_849_ = ((float) (class124.anIntArray1844[i_805_]) / 1024.0F);
                            Class246.method1885(i_846_, anIntArray5312[i_841_], i_848_, 8, anIntArray5356[i_841_], fs, anIntArray5332[i_841_], f, i_845_, i_844_, f_849_, fs_847_);
                            fs_807_[0] = fs[0];
                            fs_808_[0] = fs[1];
                            Class246.method1885(i_846_, anIntArray5312[i_842_], i_848_, 8, anIntArray5356[i_842_], fs, anIntArray5332[i_842_], f, i_845_, i_844_, f_849_, fs_847_);
                            fs_807_[1] = fs[0];
                            fs_808_[1] = fs[1];
                            Class246.method1885(i_846_, anIntArray5312[i_843_], i_848_, 8, anIntArray5356[i_843_], fs, anIntArray5332[i_843_], f, i_845_, i_844_, f_849_, fs_847_);
                            fs_807_[2] = fs[0];
                            fs_808_[2] = fs[1];
                            float f_850_ = f_849_ / 2.0F;
                            if ((i_848_ & 0x1) == 0) {
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
                        } else if (i_810_ == 2) {
                            float f_851_ = ((float) (class124.anIntArray1857[i_805_]) / 256.0F);
                            float f_852_ = ((float) (class124.anIntArray1865[i_805_]) / 256.0F);
                            int i_853_ = (anIntArray5356[i_842_] - anIntArray5356[i_841_]);
                            int i_854_ = (anIntArray5332[i_842_] - anIntArray5332[i_841_]);
                            int i_855_ = (anIntArray5312[i_842_] - anIntArray5312[i_841_]);
                            int i_856_ = (anIntArray5356[i_843_] - anIntArray5356[i_841_]);
                            int i_857_ = (anIntArray5332[i_843_] - anIntArray5332[i_841_]);
                            int i_858_ = (anIntArray5312[i_843_] - anIntArray5312[i_841_]);
                            int i_859_ = i_854_ * i_858_ - i_857_ * i_855_;
                            int i_860_ = i_855_ * i_856_ - i_858_ * i_853_;
                            int i_861_ = i_853_ * i_857_ - i_856_ * i_854_;
                            float f_862_ = 64.0F / (float) (class124.anIntArray1859[i_805_]);
                            float f_863_ = 64.0F / (float) (class124.anIntArray1816[i_805_]);
                            float f_864_ = 64.0F / (float) (class124.anIntArray1844[i_805_]);
                            float f_865_ = (((float) i_859_ * fs_847_[0] + (float) i_860_ * fs_847_[1] + (float) i_861_ * fs_847_[2]) / f_862_);
                            float f_866_ = (((float) i_859_ * fs_847_[3] + (float) i_860_ * fs_847_[4] + (float) i_861_ * fs_847_[5]) / f_863_);
                            float f_867_ = (((float) i_859_ * fs_847_[6] + (float) i_860_ * fs_847_[7] + (float) i_861_ * fs_847_[8]) / f_864_);
                            int i_868_ = Class331.method2635(f_866_, false, f_867_, f_865_);
                            Class262.method1991(f_852_, f, fs_847_, anIntArray5312[i_841_], i_846_, false, i_848_, i_844_, anIntArray5356[i_841_], anIntArray5332[i_841_], f_851_, fs, i_845_, i_868_);
                            fs_807_[0] = fs[0];
                            fs_808_[0] = fs[1];
                            Class262.method1991(f_852_, f, fs_847_, anIntArray5312[i_842_], i_846_, false, i_848_, i_844_, anIntArray5356[i_842_], anIntArray5332[i_842_], f_851_, fs, i_845_, i_868_);
                            fs_807_[1] = fs[0];
                            fs_808_[1] = fs[1];
                            Class262.method1991(f_852_, f, fs_847_, anIntArray5312[i_843_], i_846_, false, i_848_, i_844_, anIntArray5356[i_843_], anIntArray5332[i_843_], f_851_, fs, i_845_, i_868_);
                            fs_807_[2] = fs[0];
                            fs_808_[2] = fs[1];
                        } else if (i_810_ == 3) {
                            Class181.method1367(i_846_, i_848_, f, anIntArray5356[i_841_], fs, anIntArray5312[i_841_], i_844_, anIntArray5332[i_841_], i_845_, -4, fs_847_);
                            fs_807_[0] = fs[0];
                            fs_808_[0] = fs[1];
                            Class181.method1367(i_846_, i_848_, f, anIntArray5356[i_842_], fs, anIntArray5312[i_842_], i_844_, anIntArray5332[i_842_], i_845_, -4, fs_847_);
                            fs_807_[1] = fs[0];
                            fs_808_[1] = fs[1];
                            Class181.method1367(i_846_, i_848_, f, anIntArray5356[i_843_], fs, anIntArray5312[i_843_], i_844_, anIntArray5332[i_843_], i_845_, -4, fs_847_);
                            fs_807_[2] = fs[0];
                            fs_808_[2] = fs[1];
                            if ((i_848_ & 0x1) == 0) {
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
        if (!bool_802_) aFloatArrayArray5314 = aFloatArrayArray5345 = null;
        if (class124.anIntArray1868 != null && (anInt5316 & 0x20) != 0) anIntArrayArray5334 = class124.method1100(true, -122);
        if (class124.anIntArray1824 != null && (anInt5316 & 0x180) != 0) anIntArrayArray5330 = class124.method1094((byte) 30);
        if (class124.aClass162Array1832 != null && (anInt5316 & 0x400) != 0) anIntArrayArray5379 = class124.method1093((byte) -75);
        if (class124.aShortArray1822 != null) {
            aShortArray5388 = new short[anInt5351];
            boolean bool_869_ = false;
            for (int i_870_ = 0; i_870_ < anInt5351; i_870_++) {
                short i_871_ = class124.aShortArray1822[i_870_];
                if (i_871_ != -1) {
                    Class12 class12 = aHa_Sub1_5353.aD4579.method3(i_871_, -6662);
                    if ((i_787_ & 0x40) == 0 || !class12.aBoolean209) {
                        aShortArray5388[i_870_] = i_871_;
                        bool_869_ = true;
                        if (class12.anInt200 == 2) aBoolean5382 = true;
                        if (class12.aByte198 != 0 || class12.aByte211 != 0) aBoolean5391 = true;
                    } else aShortArray5388[i_870_] = (short) -1;
                } else aShortArray5388[i_870_] = (short) -1;
            }
            if (!bool_869_) aShortArray5388 = null;
        } else aShortArray5388 = null;
        if (aBoolean5382 || aClass6Array5361 != null) {
            aShortArray5327 = new short[anInt5351];
            for (int i_872_ = 0; i_872_ < anInt5351; i_872_++)
                aShortArray5327[i_872_] = (short) is[i_872_];
        }
    }

    static {
        anInt5350 = 4096;
    }
}
