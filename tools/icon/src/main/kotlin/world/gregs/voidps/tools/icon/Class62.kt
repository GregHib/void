package world.gregs.voidps.tools.icon

/* Class62 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class62 {
    var anInt1113: Int = 0

    @Synchronized
    fun method599(i: Int): Long {
        if (i > -52) return -121L
        anInt1113++
        val l = System.currentTimeMillis()
        if (Matrix_Sub1.Companion.aLong5663 > l) Class318_Sub1_Sub1.aLong8728 += Matrix_Sub1.Companion.aLong5663 - l
        Matrix_Sub1.Companion.aLong5663 = l
        return l + Class318_Sub1_Sub1.aLong8728
    }
}
