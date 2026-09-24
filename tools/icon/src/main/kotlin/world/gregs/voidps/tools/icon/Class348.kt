package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache

/* Class348 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal open class Class348 {
    var aClass348_4294: Class348? = null
    var aClass348_4295: Class348? = null
    var aLong4291: Long = 0

    fun method2712(i: Byte): Boolean {
        if (i.toInt() != 4) return true
        anInt4297++
        return this.aClass348_4295 != null
    }

    fun unlink(i: Byte) {
        anInt4285++
        if (this.aClass348_4295 != null) {
            this.aClass348_4295!!.aClass348_4294 = this.aClass348_4294
            this.aClass348_4294!!.aClass348_4295 = this.aClass348_4295
            if (i < 18) method2712(46.toByte())
            this.aClass348_4294 = null
            this.aClass348_4295 = null
        }
    }

    companion object {
        var anInt4285: Int = 0
        var aCache_4286: Cache? = null
        var anInt4297: Int = 0
    }
}
