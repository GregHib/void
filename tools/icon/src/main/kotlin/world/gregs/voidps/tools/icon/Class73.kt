package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.Cache

/* Class73 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class73 {
    var cache: Cache? = null

    var aClass60_2844: Class60 = Class60(64)

    var aFloatArray4772: FloatArray?
    var anInt4777: Int = 0
    var aClass114_4779: Class114?
    var anIntArray4780: IntArray?
    var aClass219_4782: Class219? = null
    var anInt4775: Int = 0

    /* NOTE: called only from method742 behind "if (i != 104)"; every real
     * caller of method742 across the original client passes i==104, so this
     * is unreachable in practice but kept (with method743) so the genuine
     * method742 call site resolves at compile time. */
    fun method741(i: Byte) {
        aFloatArray4772 = null
        anIntArray4780 = null
        if (i.toInt() != -128) method743(113, -98)
        aClass114_4779 = null
        aClass219_4782 = null
    }

    fun method743(i: Int, i_2_: Int) {
        anInt4775++
        val class348_sub42_sub15 = Class348_Sub42_Sub15.method2516(i_2_, 105.toByte(), i) //9
        class348_sub42_sub15.method3251(i xor 0x3eb0.inv())
    }

    fun list(i: Int, i_0_: Int): BillboardType {
        anInt4777++
        var billboardType = aClass60_2844.method583(i_0_.toLong(), -104) as BillboardType?
        if (billboardType != null) return billboardType
        val `is` = cache!!.data(Index.BILLBOARDS, 0, i_0_)
        if (i != 104) method741(98.toByte())
        billboardType = BillboardType()
        if (`is` != null) billboardType.method1419(i_0_, Packet(`is`), 64.toByte())
        aClass60_2844.method582(billboardType, i_0_.toLong(), (-114).toByte())
        return billboardType
    }

    init {
        aFloatArray4772 = FloatArray(16)
        anIntArray4780 = intArrayOf(104, 120, 136, 168)
        aClass114_4779 = Class114(76, 6)
    }
}
