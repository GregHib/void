package world.gregs.voidps.tools.inv.item;/* Class358 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class358 {
    float[][] aFloatArrayArray4412;
    int[] anIntArray4414;
    int[] anIntArray4415;
    int[] anIntArray4416;

    Class358(int[] is, int[] is_1_, int[] is_2_, float[][] fs) {
        try {
            this.anIntArray4414 = is_2_;
            this.aFloatArrayArray4412 = fs;
            this.anIntArray4415 = is_1_;
            this.anIntArray4416 = is;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ew.<init>(" + (is != null ? "{...}" : "null") + ',' + (is_1_ != null ? "{...}" : "null") + ',' + (is_2_ != null ? "{...}" : "null") + ',' + (fs != null ? "{...}" : "null") + ')'));
        }
    }
}
