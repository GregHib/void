package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub32 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub32 extends Class348_Sub40 {
    static Class273 aClass273_9415 = new Class273("", 12);
    static int anInt9417;
    private static short[] aShortArray9421 = {-10304, 9104, 25485, 4620, 4540};
    private static short[] aShortArray9422 = {-1, -1, -1, -1, -1};
    private static short[] aShortArray9423 = {6798, 8741, 25238, 4626, 4550};
    static short[][] aShortArrayArray9424 = {aShortArray9423, aShortArray9421, aShortArray9422};

    public static void method3133(byte i) {
        aClass273_9415 = null;
        if (i != -109) aClass273_9415 = null;
        aShortArray9423 = null;
        aShortArray9421 = null;
        aShortArrayArray9424 = null;
        aShortArray9422 = null;
    }

    final int[][] method3047(int i, int i_9_) {
        if (i_9_ != -1564599039) method3133((byte) 4);
        anInt9417++;
        int[][] is = this.aClass322_7033.method2557(-78, i);
        if (this.aClass322_7033.aBoolean4035) {
            int[][] is_10_ = this.method3039((byte) -104, i, 0);
            int[] is_11_ = is_10_[0];
            int[] is_12_ = is_10_[1];
            int[] is_13_ = is_10_[2];
            int[] is_14_ = is[0];
            int[] is_15_ = is[1];
            int[] is_16_ = is[2];
            for (int i_17_ = 0; Class348_Sub40_Sub6.anInt9139 > i_17_; i_17_++) {
                is_14_[i_17_] = -is_11_[i_17_] + 4096;
                is_15_[i_17_] = 4096 + -is_12_[i_17_];
                is_16_[i_17_] = -is_13_[i_17_] + 4096;
            }
        }
        return is;
    }

    public Class348_Sub40_Sub32() {
        super(1, false);
    }
}
