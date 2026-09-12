package world.gregs.voidps.tools.inv.item;/* Class348_Sub42_Sub15 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub42_Sub15 extends Class348_Sub42 {
    /* Minimal stub: only the cache-lookup construction path (method2516)
     * and method3251 (called from Class73.method741, itself dead code -
     * Class73.method742's callers always pass i==104 so method741 is
     * never actually invoked) are needed to satisfy compilation. Extends
     * Class348_Sub42 (needed for the Class348-typed cache lookup cast in
     * Class318_Sub9_Sub1.method2516) so aLong4291/aLong7057 are inherited
     * rather than redeclared here. */
    static int anInt9648;

    Class348_Sub42_Sub15(int i, int i_2_) {
        this.aLong4291 = (long) i << 32 | (long) i_2_;
    }

    final void method3251(int i) {
        anInt9648++;
        this.key2 = (~0x7fffffffffffffffL & this.key2 | Class62.method599(-120) + 500L);
        s_Sub2.aQueue_8241.add(true, this);
        if (i != -16058) { /* aClass105_9658 = null; (not needed) */ }
    }
}
