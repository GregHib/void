package world.gregs.voidps.tools.icon

/* Interface14 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal interface CacheKey {
    fun toLong(i: Byte): Long

    fun matches(i: Int, cacheKey_0_: CacheKey?): Boolean
}
