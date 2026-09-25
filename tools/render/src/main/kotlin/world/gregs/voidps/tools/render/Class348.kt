package world.gregs.voidps.tools.render

import world.gregs.voidps.cache.Cache

open class Class348 {
    var aClass348_4294: Class348? = null
    var aClass348_4295: Class348? = null
    var aLong4291: Long = 0

    fun unlink() {
        if (this.aClass348_4295 != null) {
            this.aClass348_4295!!.aClass348_4294 = this.aClass348_4294
            this.aClass348_4294!!.aClass348_4295 = this.aClass348_4295
            this.aClass348_4294 = null
            this.aClass348_4295 = null
        }
    }

    companion object {
        var aCache_4286: Cache? = null
    }
}
