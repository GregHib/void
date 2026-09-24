package world.gregs.voidps.tools.icon;/* Class356 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class IterableHashTable {
    Class348[] aClass348Array4374;
    static int anInt4375;
    int anInt4377;
    static int anInt4379;
    static int anInt4381;
    static int anInt4382;
    static int anInt4384;
    private long aLong4385;
    static int anInt4386;
    private Class348 aClass348_4389;
    private Class348 aClass348_4390;
    private int anInt4391 = 0;

    /* NOTE: original calls method3479(4) behind "if (bool != true)"; every
     * genuine call site in this batch passes bool==true, so that branch
     * (Class348_Sub21 cache lookup, unrelated to icon rendering) is
     * unreachable dead code and omitted rather than pulling in that
     * unrelated subsystem. */
    final Class348 method3476(boolean bool) {
        anInt4384++;
        if (aClass348_4389 == null) return null;
        Class348 class348 = (this.aClass348Array4374[(int) ((long) (this.anInt4377 - 1) & aLong4385)]);
        for (/**/; aClass348_4389 != class348; aClass348_4389 = aClass348_4389.aClass348_4294) {
            if (aClass348_4389.aLong4291 == aLong4385) {
                Class348 class348_3_ = aClass348_4389;
                aClass348_4389 = aClass348_4389.aClass348_4294;
                return class348_3_;
            }
        }
        aClass348_4389 = null;
        return null;
    }

    final Class348 method3480(long l, int i) {
        try {
            aLong4385 = l;
            anInt4379++;
            Class348 class348 = (this.aClass348Array4374[(int) (l & (long) (this.anInt4377 + -1))]);
            if (i != -6008) method3484(80);
            for (aClass348_4389 = class348.aClass348_4294; aClass348_4389 != class348; aClass348_4389 = aClass348_4389.aClass348_4294) {
                if (l == aClass348_4389.aLong4291) {
                    Class348 class348_7_ = aClass348_4389;
                    aClass348_4389 = aClass348_4389.aClass348_4294;
                    return class348_7_;
                }
            }
            aClass348_4389 = null;
            return null;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, "eq.C(" + l + ',' + i + ')');
        }
    }

    final void method3481(int i) {
        anInt4375++;
        for (int i_8_ = i; this.anInt4377 > i_8_; i_8_++) {
            Class348 class348 = this.aClass348Array4374[i_8_];
            for (; ; ) {
                Class348 class348_9_ = class348.aClass348_4294;
                if (class348_9_ == class348) break;
                class348_9_.unlink((byte) 54);
            }
        }
        aClass348_4389 = null;
        aClass348_4390 = null;
    }

    final Class348 method3482(int i) {
        anInt4381++;
        if (anInt4391 > i && (aClass348_4390 != this.aClass348Array4374[-1 + anInt4391])) {
            Class348 class348 = aClass348_4390;
            aClass348_4390 = class348.aClass348_4294;
            return class348;
        }
        while (this.anInt4377 > anInt4391) {
            Class348 class348 = (this.aClass348Array4374[anInt4391++].aClass348_4294);
            if (this.aClass348Array4374[-1 + anInt4391] != class348) {
                aClass348_4390 = class348.aClass348_4294;
                return class348;
            }
        }
        return null;
    }

    final void put(byte i, long l, Class348 class348) {
        try {
            anInt4382++;
            if (i < 18) method3481(71);
            if (class348.aClass348_4295 != null) class348.unlink((byte) 57);
            Class348 class348_10_ = (this.aClass348Array4374[(int) (l & (long) (-1 + this.anInt4377))]);
            class348.aClass348_4294 = class348_10_;
            class348.aClass348_4295 = class348_10_.aClass348_4295;
            class348.aClass348_4295.aClass348_4294 = class348;
            class348.aClass348_4294.aClass348_4295 = class348;
            class348.aLong4291 = l;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("eq.K(" + i + ',' + l + ',' + (class348 != null ? "{...}" : "null") + ')'));
        }
    }

    final Class348 method3484(int i) {
        anInt4391 = i;
        anInt4386++;
        return method3482(0);
    }

    IterableHashTable(int i) {
        this.anInt4377 = i;
        this.aClass348Array4374 = new Class348[i];
        for (int i_11_ = 0; i > i_11_; i_11_++) {
            Class348 class348 = this.aClass348Array4374[i_11_] = new Class348();
            class348.aClass348_4294 = class348;
            class348.aClass348_4295 = class348;
        }
    }
}
