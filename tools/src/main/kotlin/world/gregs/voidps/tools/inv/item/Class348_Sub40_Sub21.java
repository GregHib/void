package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub21 - minimal stub (missing from trimmed tree)
 * See client/src/Class348_Sub40_Sub21.java for the full original.
 */

final class Class348_Sub40_Sub21 extends Class348_Sub40 {
    static int anInt9280;
    static long[] crc64table;

    public Class348_Sub40_Sub21() {
        super(0, false);
    }

    static {
        anInt9280 = 0;
        crc64table = new long[256];
        for (int i = 0; i < 256; i++) {
            long l = i;
            for (int i_24_ = 0; i_24_ < 8; i_24_++) {
                if ((0x1L & l) == 1L) l = ~0x3693a86a2878f0bdL ^ l >>> 1;
                else l >>>= 1;
            }
            crc64table[i] = l;
        }
    }
}
