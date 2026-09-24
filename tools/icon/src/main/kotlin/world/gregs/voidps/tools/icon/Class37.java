package world.gregs.voidps.tools.icon;/* Class37 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class37 {
    static int anInt493;

    static final synchronized byte[] method359(int i, int i_9_) {
        anInt493++;
        if (i == 100 && Class348_Sub40_Sub31.anInt9412 > 0) {
            byte[] is = Class24.aByteArrayArray358[--Class348_Sub40_Sub31.anInt9412];
            Class24.aByteArrayArray358[Class348_Sub40_Sub31.anInt9412] = null;
            return is;
        }
        if (i == 5000 && Class348_Sub40_Sub21.anInt9280 > 0) {
            byte[] is = (Class133.aByteArrayArray1918[--Class348_Sub40_Sub21.anInt9280]);
            Class133.aByteArrayArray1918[Class348_Sub40_Sub21.anInt9280] = null;
            return is;
        }
        if (i_9_ != -1) method359(-88, -45);
        if (i == 30000 && Class348_Sub31.anInt6913 > 0) {
            byte[] is = (Class285_Sub2.aByteArrayArray8505[--Class348_Sub31.anInt6913]);
            Class285_Sub2.aByteArrayArray8505[Class348_Sub31.anInt6913] = null;
            return is;
        }
        if (Class348_Sub40_Sub6.aByteArrayArrayArray9134 != null) {
            for (int i_10_ = 0; Class59_Sub2_Sub2.anIntArray8684.length > i_10_; i_10_++) {
                if ((i == Class59_Sub2_Sub2.anIntArray8684[i_10_]) && Class190.anIntArray2552[i_10_] > 0) {
                    byte[] is = (Class348_Sub40_Sub6.aByteArrayArrayArray9134[i_10_][--Class190.anIntArray2552[i_10_]]);
                    Class348_Sub40_Sub6.aByteArrayArrayArray9134[i_10_][Class190.anIntArray2552[i_10_]] = null;
                    return is;
                }
            }
        }
        return new byte[i];
    }
}
