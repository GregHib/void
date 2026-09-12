package world.gregs.voidps.tools.inv.item;/* Class126 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class ItemSpriteCacheKey implements CacheKey {
    int itemNumMode;
    int graphicShadow;
    static int[] HSV_TO_RGB;
    int invCount;
    boolean itemWearCol;
    int toolkitIndex;
    int itemId;
    int outline;
    static int anInt4988;
    static int anInt4994;

    public final long toLong(byte i) {
        anInt4988++;
        long[] ls = Class348_Sub40_Sub21.crc64table;
        long l = -1L;
        l = (ls[(int) ((l ^ (long) this.toolkitIndex) & 0xffL)] ^ l >>> 8);
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) (this.itemId >> 8) ^ l))]);
        l = (l >>> 8 ^ ls[(int) ((l ^ (long) this.itemId) & 0xffL)]);
        l = (l >>> 8 ^ ls[(int) (0xffL & (l ^ (long) (this.invCount >> 24)))]);
        l = ls[(int) (0xffL & ((long) (this.invCount >> 16) ^ l))] ^ l >>> 8;
        l = (l >>> 8 ^ ls[(int) (0xffL & (l ^ (long) (this.invCount >> 8)))]);
        l = (l >>> 8 ^ ls[(int) ((l ^ (long) this.invCount) & 0xffL)]);
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) this.outline ^ l))]);
        l = ls[(int) ((l ^ (long) (this.graphicShadow >> 24)) & 0xffL)] ^ l >>> 8;
        l = (l >>> 8 ^ ls[(int) ((l ^ (long) (this.graphicShadow >> 16)) & 0xffL)]);
        if (i < 46) return -94L;
        l = ls[(int) ((l ^ (long) (this.graphicShadow >> 8)) & 0xffL)] ^ l >>> 8;
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) this.graphicShadow ^ l))]);
        l = (ls[(int) (0xffL & (l ^ (long) this.itemNumMode))] ^ l >>> 8);
        l = (l >>> 8 ^ ls[(int) (0xffL & ((long) (this.itemWearCol ? 1 : 0) ^ l))]);
        return l;
    }

    /* Not in the genuine-methods list, but Class126 implements Interface14
     * (declaring method53), so it must be present for compilation
     * regardless of whether it was hit at runtime. */
    public final boolean matches(int i, CacheKey other) {
        anInt4994++;
        if (!(other instanceof ItemSpriteCacheKey)) return false;
        ItemSpriteCacheKey data = (ItemSpriteCacheKey) other;
        if (this.toolkitIndex != data.toolkitIndex) return false;
        if (i <= 50) return true;
        if (this.itemId != data.itemId) return false;
        if (this.invCount != data.invCount) return false;
        if (this.outline != data.outline) return false;
        if (data.graphicShadow != this.graphicShadow) return false;
        if (this.itemNumMode != data.itemNumMode) return false;
        return !data.itemWearCol == !this.itemWearCol;
    }
}
