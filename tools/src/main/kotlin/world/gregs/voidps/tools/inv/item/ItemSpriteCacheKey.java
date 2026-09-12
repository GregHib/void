package world.gregs.voidps.tools.inv.item;/* Class126 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class ItemSpriteCacheKey implements Interface14 {
    int anInt4981;
    int anInt4982;
    static int[] anIntArray4983;
    int anInt4989;
    boolean itemWearCol;
    int toolkitIndex;
    int anInt4992;
    int anInt4993;
    static int anInt4988;
    static int anInt4994;

    public final long method52(byte i) {
        anInt4988++;
        long[] ls = Class348_Sub40_Sub21.aLongArray9283;
        long l = -1L;
        l = (ls[(int) ((l ^ (long) this.toolkitIndex) & 0xffL)] ^ l >>> 8);
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) (this.anInt4992 >> 8) ^ l))]);
        l = (l >>> 8 ^ ls[(int) ((l ^ (long) this.anInt4992) & 0xffL)]);
        l = (l >>> 8 ^ ls[(int) (0xffL & (l ^ (long) (this.anInt4989 >> 24)))]);
        l = ls[(int) (0xffL & ((long) (this.anInt4989 >> 16) ^ l))] ^ l >>> 8;
        l = (l >>> 8 ^ ls[(int) (0xffL & (l ^ (long) (this.anInt4989 >> 8)))]);
        l = (l >>> 8 ^ ls[(int) ((l ^ (long) this.anInt4989) & 0xffL)]);
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) this.anInt4993 ^ l))]);
        l = ls[(int) ((l ^ (long) (this.anInt4982 >> 24)) & 0xffL)] ^ l >>> 8;
        l = (l >>> 8 ^ ls[(int) ((l ^ (long) (this.anInt4982 >> 16)) & 0xffL)]);
        if (i < 46) return -94L;
        l = ls[(int) ((l ^ (long) (this.anInt4982 >> 8)) & 0xffL)] ^ l >>> 8;
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) this.anInt4982 ^ l))]);
        l = (ls[(int) (0xffL & (l ^ (long) this.anInt4981))] ^ l >>> 8);
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) (this.itemWearCol ? 1 : 0) ^ l))]);
        return l;
    }

    /* Not in the genuine-methods list, but Class126 implements Interface14
     * (declaring method53), so it must be present for compilation
     * regardless of whether it was hit at runtime. */
    public final boolean method53(int i, Interface14 interface14) {
        anInt4994++;
        if (!(interface14 instanceof ItemSpriteCacheKey)) return false;
        ItemSpriteCacheKey itemSpriteCacheKey_3_ = (ItemSpriteCacheKey) interface14;
        if (this.toolkitIndex != itemSpriteCacheKey_3_.toolkitIndex) return false;
        if (i <= 50) return true;
        if (this.anInt4992 != itemSpriteCacheKey_3_.anInt4992) return false;
        if (this.anInt4989 != itemSpriteCacheKey_3_.anInt4989) return false;
        if (this.anInt4993 != itemSpriteCacheKey_3_.anInt4993) return false;
        if (itemSpriteCacheKey_3_.anInt4982 != this.anInt4982) return false;
        if (this.anInt4981 != itemSpriteCacheKey_3_.anInt4981) return false;
        return !itemSpriteCacheKey_3_.itemWearCol == !this.itemWearCol;
    }
}
