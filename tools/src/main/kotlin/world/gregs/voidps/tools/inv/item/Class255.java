package world.gregs.voidps.tools.inv.item;/* Class255 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class255 {
    private final Class45 aClass45_3267;
    Class45 aClass45_3268;
    private boolean aBoolean3269;
    int anInt3271;
    static int anInt3266;
    static int anInt3282;
    static int anInt3283;
    private final Class60 aClass60_3278 = new Class60(64);
    int anInt3286;
    Class175 aClass175_3288 = new Class175(250);
    private Class126 aClass126_3289 = new Class126();
    private final String[] aStringArray3290;
    private Class326 aClass326_3292;
    private final String[] aStringArray3293;

    /* NOTE: originally calls method1935(...) behind "if (i_2_ != 83)".
     * method1935 is not in the genuine-methods list, so it is treated as
     * dead code and omitted. Also calls this.aClass175_3288.method1348(...)
     * inside "if (!bool)" - Class175's genuine list lacks method1348, so
     * that branch appears dead too (bool is apparently always true at real
     * call sites) and method1348 was not carried into the trimmed Class175.
     * Both flagged for a follow-up compile-fix pass. */
    final Class105 method1932(ha var_ha, int i, int i_0_, Class324 class324, Class154 class154, int i_1_, boolean bool, byte i_2_, ha var_ha_3_, int i_4_, boolean bool_5_, int i_6_) {
        try {
            anInt3266++;
            Class105 class105 = method1941(i_6_, (byte) -74, i_4_, i_0_, i, i_1_, var_ha_3_, class154);
            if (class105 != null) return class105;
            Class213 class213 = method1940(90, i_4_);
            if (i_0_ > 1 && class213.anIntArray2762 != null) {
                int i_7_ = -1;
                for (int i_8_ = 0; i_8_ < 10; i_8_++) {
                    if ((class213.anIntArray2831[i_8_] <= i_0_) && class213.anIntArray2831[i_8_] != 0) i_7_ = class213.anIntArray2762[i_8_];
                }
                if (i_7_ != -1) class213 = method1940(127, i_7_);
            }
            if (i_2_ != 83) method1935(-83, -37, null, null, false, -49);
            int[] is = class213.method1562(i_0_, bool_5_, i, var_ha_3_, var_ha, class324, class154, i_1_, (byte) -102, i_6_);
            if (is == null) return null;
            Class105 class105_9_;
            if (bool) class105_9_ = var_ha.method3662(36, is, (byte) 94, 0, 36, 32);
            else class105_9_ = var_ha_3_.method3662(36, is, (byte) 94, 0, 36, 32);
            if (!bool) {
                Class126 class126 = new Class126();
                class126.anInt4982 = i;
                class126.aBoolean4990 = class154 != null;
                class126.anInt4989 = i_0_;
                class126.anInt4992 = i_4_;
                class126.anInt4981 = i_1_;
                class126.anInt4991 = var_ha_3_.anInt4567;
                class126.anInt4993 = i_6_;
                /* this.aClass175_3288.method1348(70, class105_9_, class126);
                 * unreachable per JaCoCo coverage - both real call sites
                 * pass bool == true, so "if (!bool)" never executes. Not
                 * calling into Class175 here (owned by a parallel agent,
                 * off limits; its trimmed genuine-methods list lacks
                 * method1348 already). */
            }
            return class105_9_;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.C(" + (var_ha != null ? "{...}" : "null") + ',' + i + ',' + i_0_ + ',' + (class324 != null ? "{...}" : "null") + ',' + (class154 != null ? "{...}" : "null") + ',' + i_1_ + ',' + bool + ',' + i_2_ + ',' + (var_ha_3_ != null ? "{...}" : "null") + ',' + i_4_ + ',' + bool_5_ + ',' + i_6_ + ')'));
        }
    }

    /* NOTE: real call sites always pass i_2_ == 83 into method1932, so this
     * branch is unreachable in practice (matching the genuine-methods
     * trimming decision) - added back with a no-op body only so the still
     * present call site in method1932 compiles, without pulling in Class64's
     * EA/fa/na/V/G/HA/RA accessors (out of this batch's scope). */
    static final void method1935(int i, int i_10_, Class30 class30, Class64 class64, boolean bool, int i_11_) {
        /* empty - dead branch at real call sites, see note above */
    }

    final Class213 method1940(int i, int i_13_) {
        anInt3283++;
        Class213 class213;
        synchronized (aClass60_3278) {
            class213 = (Class213) aClass60_3278.method583(i_13_, 90);
        }
        if (class213 != null) return class213;
        byte[] is;
        synchronized (aClass45_3267) {
            is = aClass45_3267.method410(-1860, Class54.method500(7, i_13_), Class251.method1914(-23590, i_13_));
        }
        class213 = new Class213();
        class213.aClass255_2761 = this;
        class213.anInt2769 = i_13_;
        class213.aStringArray2811 = new String[]{null, null, Class274.aClass274_3490.method2063(this.anInt3286, 544), null, null};
        class213.aStringArray2763 = (new String[]{null, null, null, null, Class274.aClass274_3491.method2063(this.anInt3286, 544)});
        if (is != null) class213.method1569(768, new Class348_Sub49(is));
        class213.method1563((byte) 92);
        int i_14_ = 4 / ((i - 13) / 59);
        if (class213.anInt2833 != -1) class213.method1570(1, method1940(90, class213.anInt2758), method1940(101, class213.anInt2833));
        if (class213.anInt2812 != -1) class213.method1556(method1940(-58, class213.anInt2778), (byte) -29, method1940(-82, class213.anInt2812));
        if (!aBoolean3269 && class213.aBoolean2783) {
            class213.aString2795 = Class274.aClass274_3488.method2063(this.anInt3286, 544);
            class213.anInt2827 = 0;
            class213.aStringArray2811 = aStringArray3290;
            class213.aStringArray2763 = aStringArray3293;
            class213.aBoolean2755 = false;
            class213.anIntArray2772 = null;
            if (class213.aClass356_2757 != null) {
                boolean bool = false;
                for (Class348 class348 = class213.aClass356_2757.method3484(0); class348 != null; class348 = class213.aClass356_2757.method3482(0)) {
                    Class254 class254 = aClass326_3292.method2600((int) class348.aLong4291, 28364);
                    if (class254.aBoolean3261) class348.method2715((byte) 60);
                    else bool = true;
                }
                if (!bool) class213.aClass356_2757 = null;
            }
        }
        synchronized (aClass60_3278) {
            aClass60_3278.method582(class213, i_13_, (byte) -118);
        }
        return class213;
    }

    final Class105 method1941(int i, byte i_15_, int i_16_, int i_17_, int i_18_, int i_19_, ha var_ha, Class154 class154) {
        try {
            aClass126_3289.anInt4992 = i_16_;
            aClass126_3289.anInt4989 = i_17_;
            aClass126_3289.anInt4991 = var_ha.anInt4567;
            aClass126_3289.anInt4981 = i_19_;
            if (i_15_ != -74) aClass126_3289 = null;
            aClass126_3289.aBoolean4990 = class154 != null;
            aClass126_3289.anInt4982 = i_18_;
            anInt3282++;
            aClass126_3289.anInt4993 = i;
            return (Class105) this.aClass175_3288.method1340(123, aClass126_3289);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.D(" + i + ',' + i_15_ + ',' + i_16_ + ',' + i_17_ + ',' + i_18_ + ',' + i_19_ + ',' + (var_ha != null ? "{...}" : "null") + ',' + (class154 != null ? "{...}" : "null") + ')'));
        }
    }

    Class255(Class230 class230, int i, boolean bool, Class326 class326, Class45 class45, Class45 class45_22_) {
        try {
            aBoolean3269 = bool;
            aClass326_3292 = class326;
            this.aClass45_3268 = class45_22_;
            this.anInt3286 = i;
            aClass45_3267 = class45;
            if (aClass45_3267 != null) {
                int i_23_ = -1 + aClass45_3267.method414(-1);
                this.anInt3271 = aClass45_3267.method407(0, i_23_) + i_23_ * 256;
            } else this.anInt3271 = 0;
            aStringArray3290 = (new String[]{null, null, Class274.aClass274_3490.method2063(this.anInt3286, 544), null, null});
            aStringArray3293 = (new String[]{null, null, null, null, Class274.aClass274_3491.method2063(this.anInt3286, 544)});
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.<init>(" + (class230 != null ? "{...}" : "null") + ',' + i + ',' + bool + ',' + (class326 != null ? "{...}" : "null") + ',' + (class45 != null ? "{...}" : "null") + ',' + (class45_22_ != null ? "{...}" : "null") + ')'));
        }
    }
}
