package world.gregs.voidps.tools.render

/* Class356 */

class IterableHashTable(var anInt4377: Int) {
    var aClass348Array4374: Array<Class348?>
    private var aClass348_4389: Class348? = null

    fun method3480(l: Long): Class348? {
        val class348 = (this.aClass348Array4374[(l and (this.anInt4377 + -1).toLong()).toInt()])
        aClass348_4389 = class348!!.aClass348_4294
        while (aClass348_4389 !== class348) {
            if (l == aClass348_4389!!.aLong4291) {
                val class348_7_ = aClass348_4389
                aClass348_4389 = aClass348_4389!!.aClass348_4294
                return class348_7_
            }
            aClass348_4389 = aClass348_4389!!.aClass348_4294
        }
        aClass348_4389 = null
        return null
    }

    fun put(l: Long, class348: Class348?) {
        if (class348!!.aClass348_4295 != null) class348.unlink()
        val class348_10_ = (this.aClass348Array4374[(l and (-1 + this.anInt4377).toLong()).toInt()])
        class348.aClass348_4294 = class348_10_
        class348.aClass348_4295 = class348_10_!!.aClass348_4295
        class348.aClass348_4295!!.aClass348_4294 = class348
        class348.aClass348_4294!!.aClass348_4295 = class348
        class348.aLong4291 = l
    }

    init {
        this.aClass348Array4374 = arrayOfNulls<Class348>(anInt4377)
        var i_11_ = 0
        while (anInt4377 > i_11_) {
            this.aClass348Array4374[i_11_] = Class348()
            val class348 = this.aClass348Array4374[i_11_]
            class348!!.aClass348_4294 = class348
            class348.aClass348_4295 = class348
            i_11_++
        }
    }
}
