package world.gregs.voidps.tools.icon

/* Class348_Sub42_Sub9_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*
* Trimmed: extends Class348_Sub42_Sub9 (needed by Class175.method1340,
* genuine) and keeps the aClass356_10442 cache (needed by Class60.method589,
* out of this batch).
*/

internal class KeyedHardReferenceNode(cacheKey: CacheKey?, private val anObject10440: Any?, i: Int) : KeyReferenceNode(cacheKey, i) {
    override fun get(i: Int): Any? {
        anInt10441++
        return anObject10440
    }

    override fun method3206(i: Byte): Boolean {
        anInt10445++
        return false
    }

    companion object {
        var aIterableHashTable_10442: IterableHashTable = IterableHashTable(8)
        var anInt10441: Int = 0
        var anInt10445: Int = 0
    }
}
