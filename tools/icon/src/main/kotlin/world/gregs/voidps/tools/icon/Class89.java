package world.gregs.voidps.tools.icon;/* Class89 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class89 {
    static int[] anIntArray1508 = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int i_23_ = i;
            for (int i_24_ = 0; i_24_ < 8; i_24_++) {
                if ((i_23_ & 0x1) != 1) i_23_ >>>= 1;
                else i_23_ = i_23_ >>> 1 ^ ~0x12477cdf;
            }
            anIntArray1508[i] = i_23_;
        }
    }
}
