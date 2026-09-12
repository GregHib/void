package world.gregs.voidps.tools.inv.item;/* Class316 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class316 {
    private final int[] anIntArray3961;

    Class316(int[] is) {
        int i;
        for (i = 1; is.length - -(is.length >> 1) >= i; i <<= 1) {
            /* empty */
        }
        anIntArray3961 = new int[i + i];
        for (int i_34_ = 0; i_34_ < i + i; i_34_++)
            anIntArray3961[i_34_] = -1;
        for (int i_35_ = 0; is.length > i_35_; i_35_++) {
            int i_36_;
            for (i_36_ = is[i_35_] & -1 + i; anIntArray3961[i_36_ - -i_36_ - -1] != -1; i_36_ = -1 + i & 1 + i_36_) {
                /* empty */
            }
            anIntArray3961[i_36_ + i_36_] = is[i_35_];
            anIntArray3961[i_36_ - (-i_36_ + -1)] = i_35_;
        }
    }
}
