package world.gregs.voidps.tools.inv.item;/* Class331 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class331 {
    static int[] anIntArray4128 = new int[5];
    static String[] aStringArray4129 = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    static Class46 aClass46_4130;
    static int anInt4131;

    static final int method2635(float f, boolean bool, float f_0_, float f_1_) {
        anInt4131++;
        float f_2_ = !(f_1_ < 0.0F) ? f_1_ : -f_1_;
        float f_3_ = f < 0.0F ? -f : f;
        if (bool != false) method2637(-85);
        float f_4_ = !(f_0_ < 0.0F) ? f_0_ : -f_0_;
        if (!(f_2_ < f_3_) || !(f_3_ > f_4_)) {
            if (!(f_4_ > f_2_) || !(f_3_ < f_4_)) {
                if (!(f_1_ > 0.0F)) return 5;
                return 4;
            }
            if (f_0_ > 0.0F) return 2;
            return 3;
        }
        if (f > 0.0F) return 0;
        return 1;
    }

    public static void method2637(int i) {
        aStringArray4129 = null;
        aClass46_4130 = null;
        if (i != 0) aClass46_4130 = null;
        anIntArray4128 = null;
    }

    static {
        aClass46_4130 = null;
    }
}
