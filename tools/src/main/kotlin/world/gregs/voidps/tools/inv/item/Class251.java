package world.gregs.voidps.tools.inv.item;/* Class251 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class251 {
    static int anInt3231;
    static int anInt3235;

    static final void method1913(boolean bool, int i, Class46 class46) {
        anInt3235++;
        int i_0_ = -40 % ((-35 - i) / 51);
        int i_1_ = (class46.anInt698 == 0 ? class46.anInt709 : class46.anInt698);
        int i_2_ = (class46.anInt791 != 0 ? class46.anInt791 : class46.anInt789);
        Class367_Sub1.method3534(false, class46.anInt830, i_1_, bool, i_2_, (Class348_Sub40_Sub33.aClass46ArrayArray9427[(class46.anInt830 >> 16)]));
        if (class46.aClass46Array798 != null) Class367_Sub1.method3534(false, class46.anInt830, i_1_, bool, i_2_, class46.aClass46Array798);
        Class348_Sub41 class348_sub41 = ((Class348_Sub41) Class125.aClass356_4915.method3480(class46.anInt830, -6008));
        if (class348_sub41 != null) Class239_Sub3.method1728(i_2_, -1, (class348_sub41.anInt7050), bool, i_1_);
    }

    static final int method1914(int i, int i_3_) {
        anInt3231++;
        if (i != -23590) method1913(false, -115, null);
        return i_3_ & 0xff;
    }

    public Class251() {
        /* empty */
    }
}
