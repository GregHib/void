package world.gregs.voidps.tools.icon;/* Class348_Sub1_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 *
 * Trimmed for item_renderer_standalone: only the static Class308 cache field
 * (aClass308_8815) used by ha.method3664/method3692's byte-shuffle cache is kept.
 */

final class Class348_Sub1_Sub2 extends Class348_Sub1 {
    static Class308 aClass308_8815 = new Class308(16);
    static int anInt8811;

    /* Needed by Class291's constructor (genuine: Class291(byte[], int, byte[]))
     * to verify a checksum against reference-table data. */
    static final byte[] method2730(int i, int i_4_, byte[] is, int i_5_) {
        anInt8811++;
        byte[] is_6_;
        if (i_4_ > 0) {
            is_6_ = new byte[i_5_];
            for (int i_7_ = 0; i_5_ > i_7_; i_7_++)
                is_6_[i_7_] = is[i_4_ + i_7_];
        } else is_6_ = is;
        Class85 class85 = new Class85();
        class85.method829(i + -4682);
        class85.method832(i_5_ * 8, is_6_, -69);
        byte[] is_8_ = new byte[64];
        class85.method833(true, 0, is_8_);
        return is_8_;
    }
}
