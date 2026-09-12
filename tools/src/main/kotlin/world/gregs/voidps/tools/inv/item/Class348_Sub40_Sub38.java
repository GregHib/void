package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub38 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub38 extends Class348_Sub40 {
    static int anInt9468;
    static int anInt9469;
    private int anInt9470 = 4096;
    static int anInt9472;
    private int anInt9474 = 0;

    public Class348_Sub40_Sub38() {
        super(1, false);
    }

    final void method3049(Class348_Sub49 class348_sub49, int i, int i_0_) {
        if (i_0_ != 31015) anInt9470 = -15;
        int i_1_ = i;
        while_213_:
        do {
            do {
                if (i_1_ == 0) {
                    anInt9474 = class348_sub49.readUnsignedShort(i_0_ + 842366929);
                    break while_213_;
                } else if (i_1_ != 1) {
                    if (i_1_ == 2) break;
                    break while_213_;
                }
                anInt9470 = class348_sub49.readUnsignedShort(i_0_ + 842366929);
                break while_213_;
            } while (false);
            this.aBoolean7045 = class348_sub49.readUnsignedByte(i_0_ + -30760) == 1;
        } while (false);
        anInt9472++;
    }

    final int[][] method3047(int i, int i_2_) {
        if (i_2_ != -1564599039) method3150(true);
        anInt9468++;
        int[][] is = this.aClass322_7033.method2557(-117, i);
        if (this.aClass322_7033.aBoolean4035) {
            int[][] is_3_ = this.method3039((byte) -88, i, 0);
            int[] is_4_ = is_3_[0];
            int[] is_5_ = is_3_[1];
            int[] is_6_ = is_3_[2];
            int[] is_7_ = is[0];
            int[] is_8_ = is[1];
            int[] is_9_ = is[2];
            for (int i_10_ = 0; (i_10_ < Class348_Sub40_Sub6.anInt9139); i_10_++) {
                int i_11_ = is_4_[i_10_];
                int i_12_ = is_5_[i_10_];
                int i_13_ = is_6_[i_10_];
                if (i_11_ < anInt9474) is_7_[i_10_] = anInt9474;
                else is_7_[i_10_] = Math.min(i_11_, anInt9470);
                if (anInt9474 > i_12_) is_8_[i_10_] = anInt9474;
                else is_8_[i_10_] = Math.min(i_12_, anInt9470);
                if (anInt9474 <= i_13_) {
                    is_9_[i_10_] = Math.min(i_13_, anInt9470);
                } else is_9_[i_10_] = anInt9474;
            }
        }
        return is;
    }

    /* NOTE: method3150 is NOT in the genuine-methods list (0 JaCoCo hits)
     * for this renderer, so it is stubbed rather than pulling in
     * Class348_Sub22/Npc and their world-state dependencies to compile a
     * path that never executes here. */
    static final void method3150(boolean bool) {
        anInt9469++;
        if (bool != true) method3150(false);
        throw new IllegalStateException(); // unreachable per JaCoCo coverage
    }
}
