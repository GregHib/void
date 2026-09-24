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
        this.key2 = (0x7fffffffffffffffL.inv() and this.key2 or method599(-120) + 500L)
        aQueue_8241.add(true, this)
        if (i != -16058) { /* aClass105_9658 = null; (not needed) */
        }
    }

    companion object {
        var aIterableHashTable_1585: IterableHashTable = IterableHashTable(16)

        var anInt8788: Int = 0
        var aFloat8784: Float = 0f

        fun method2516(i: Int, i_7_: Byte, i_8_: Int): Class348_Sub42_Sub15 {
            anInt8788++
            var class348_sub42_sub15 = (aIterableHashTable_1585.method3480((i_8_.toLong() shl 32 or i.toLong()), i_7_.toInt() xor 0x171e.inv()) as Class348_Sub42_Sub15?)
            if (i_7_.toInt() != 105) aFloat8784 = 0.99212307f
            if (class348_sub42_sub15 == null) {
                class348_sub42_sub15 = Class348_Sub42_Sub15(i_8_, i)
                aIterableHashTable_1585.put(91.toByte(), (class348_sub42_sub15.aLong4291), class348_sub42_sub15)
            }
            return class348_sub42_sub15
        }

        var aLong8728: Long = 0

        var anInt1113: Int = 0

        @Synchronized
        fun method599(i: Int): Long {
            if (i > -52) return -121L
            anInt1113++
            val l = System.currentTimeMillis()
            if (Matrix_Sub1.Companion.aLong5663 > l) aLong8728 += Matrix_Sub1.Companion.aLong5663 - l
            Matrix_Sub1.Companion.aLong5663 = l
            return l + aLong8728
        }

        var aQueue_8241: Queue = Queue()
        /* Minimal stub: only the cache-lookup construction path (method2516)
     * and method3251 (called from Class73.method741, itself dead code -
     * Class73.method742's callers always pass i==104 so method741 is
     * never actually invoked) are needed to satisfy compilation. Extends
     * Class348_Sub42 (needed for the Class348-typed cache lookup cast in
     * method2516) so aLong4291/aLong7057 are inherited
     * rather than redeclared here. */
        var anInt9648: Int = 0
    }
}
