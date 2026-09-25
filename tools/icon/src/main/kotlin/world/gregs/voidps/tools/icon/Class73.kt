package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.Cache

/* Class73 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class73 {
    var cache: Cache? = null

    var aClass60_2844: Class60 = Class60(64)

    fun list(i_0_: Int): BillboardType {
        var billboardType = aClass60_2844.method583(i_0_.toLong()) as BillboardType?
        if (billboardType != null) return billboardType
        val `is` = cache!!.data(Index.BILLBOARDS, 0, i_0_)
        billboardType = BillboardType()
        if (`is` != null) billboardType.method1419(Packet(`is`))
        aClass60_2844.method582(billboardType, i_0_.toLong())
        return billboardType
    }
}
