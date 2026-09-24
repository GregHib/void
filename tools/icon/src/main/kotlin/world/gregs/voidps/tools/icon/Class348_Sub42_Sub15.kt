package world.gregs.voidps.tools.icon

/* Class348_Sub42_Sub15 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub42_Sub15(i: Int, i_2_: Int) : Class348_Sub42() {
    init {
        this.aLong4291 = i.toLong() shl 32 or i_2_.toLong()
    }

    fun method3251(i: Int) {
        anInt9648++
        this.key2 = (0x7fffffffffffffffL.inv() and this.key2 or Class62.method599(-120) + 500L)
        s_Sub2.aQueue_8241.add(true, this)
        if (i != -16058) { /* aClass105_9658 = null; (not needed) */
        }
    }

    companion object {
        /* Minimal stub: only the cache-lookup construction path (method2516)
     * and method3251 (called from Class73.method741, itself dead code -
     * Class73.method742's callers always pass i==104 so method741 is
     * never actually invoked) are needed to satisfy compilation. Extends
     * Class348_Sub42 (needed for the Class348-typed cache lookup cast in
     * Class318_Sub9_Sub1.method2516) so aLong4291/aLong7057 are inherited
     * rather than redeclared here. */
        var anInt9648: Int = 0
    }
}
