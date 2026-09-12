package world.gregs.voidps.tools.inv.item;/* Class348_Sub42_Sub13 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub42_Sub13 extends Class348_Sub42 {
    static int anInt9618;

    static final void method3232(double d, byte i) {
        if (i <= -54) {
            if (d != Class299_Sub2_Sub1.aDouble8713) {
                for (int i_0_ = 0; i_0_ < 256; i_0_++) {
                    int i_1_ = (int) (255.0 * Math.pow((double) i_0_ / 255.0, d));
                    Class318_Sub1_Sub3_Sub3.anIntArray10266[i_0_] = Math.min(i_1_, 255);
                }
                Class299_Sub2_Sub1.aDouble8713 = d;
            }
            anInt9618++;
        }
    }
}
