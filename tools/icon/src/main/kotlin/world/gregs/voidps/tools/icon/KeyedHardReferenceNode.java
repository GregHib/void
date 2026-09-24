package world.gregs.voidps.tools.icon;/* Class348_Sub42_Sub9_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 *
 * Trimmed: extends Class348_Sub42_Sub9 (needed by Class175.method1340,
 * genuine) and keeps the aClass356_10442 cache (needed by Class60.method589,
 * out of this batch).
 */

final class KeyedHardReferenceNode extends KeyReferenceNode {
    static IterableHashTable aIterableHashTable_10442 = new IterableHashTable(8);
    static int anInt10441;
    private final Object anObject10440;

    final Object get(int i) {
        anInt10441++;
        return anObject10440;
    }

    KeyedHardReferenceNode(CacheKey cacheKey, Object object, int i) {
        super(cacheKey, i);
        anObject10440 = object;
    }

    static int anInt10445;

    final boolean method3206(byte i) {
        anInt10445++;
        return false;
    }
}
