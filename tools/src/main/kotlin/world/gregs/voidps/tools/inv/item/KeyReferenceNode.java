package world.gregs.voidps.tools.inv.item;/* Class348_Sub42_Sub9 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class KeyReferenceNode extends Class348_Sub42 {
    int anInt9556;
    CacheKey cacheKey;

    abstract Object get(int i);

    KeyReferenceNode(CacheKey cacheKey, int i) {
        this.cacheKey = cacheKey;
        this.anInt9556 = i;
    }

    abstract boolean method3206(byte i);
}
