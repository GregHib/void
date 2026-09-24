package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index

/* Class300 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class300 {
    var anInt3815: Int = 0
    var aBoolean3819: Boolean = false

    fun load(i: Int, cache: Cache, i_5_: Int, i_6_: Int): Mesh? {
        if (i_6_ != -1) aBoolean3819 = true
        anInt3815++
        val `is` = cache.data(Index.MODELS, i_5_, i)
        if (`is` == null) return null
        return Mesh(`is`)
    }
}
