package world.gregs.voidps.tools.icon;/* Class367_Sub8 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class367_Sub8 {
    static int anInt7349;

    static final String method3546(byte[] is, int i, int i_0_, int i_1_) {
        anInt7349++;
        char[] cs = new char[i_0_];
        int i_2_ = 0;
        for (int i_3_ = i; i_3_ < i_0_; i_3_++) {
            int i_4_ = 0xff & is[i_3_ + i_1_];
            if (i_4_ != 0) {
                if (i_4_ >= 128 && i_4_ < 160) {
                    int i_5_ = Class44.aCharArray625[i_4_ - 128];
                    if (i_5_ == 0) i_5_ = 63;
                    i_4_ = i_5_;
                }
                cs[i_2_++] = (char) i_4_;
            }
        }
        return new String(cs, 0, i_2_);
    }
}
