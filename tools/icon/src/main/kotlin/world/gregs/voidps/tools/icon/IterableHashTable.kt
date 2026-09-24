package world.gregs.voidps.tools.icon

/* Class356 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class IterableHashTable(var anInt4377: Int) {
    var aClass348Array4374: Array<Class348?>
    private var aLong4385: Long = 0
    private var aClass348_4389: Class348? = null
    private var aClass348_4390: Class348? = null
    private var anInt4391 = 0

    /* NOTE: original calls method3479(4) behind "if (bool != true)"; every
     * genuine call site in this batch passes bool==true, so that branch
     * (Class348_Sub21 cache lookup, unrelated to icon rendering) is
     * unreachable dead code and omitted rather than pulling in that
     * unrelated subsystem. */
    fun method3476(bool: Boolean): Class348? {
        anInt4384++
        if (aClass348_4389 == null) return null
        val class348 = (this.aClass348Array4374[((this.anInt4377 - 1).toLong() and aLong4385).toInt()])
        while ( /**/aClass348_4389 !== class348) {
            if (aClass348_4389!!.aLong4291 == aLong4385) {
                val class348_3_ = aClass348_4389
                aClass348_4389 = aClass348_4389!!.aClass348_4294
                return class348_3_
            }
            aClass348_4389 = aClass348_4389!!.aClass348_4294
        }
        aClass348_4389 = null
        return null
    }

    fun method3480(l: Long, i: Int): Class348? {
        try {
            aLong4385 = l
            anInt4379++
            val class348 = (this.aClass348Array4374[(l and (this.anInt4377 + -1).toLong()).toInt()])
            if (i != -6008) method3484(80)
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
        } catch (runtimeexception: RuntimeException) {
            throw ItemType.method2929(runtimeexception, "eq.C(" + l + ',' + i + ')')
        }
    }

    fun method3481(i: Int) {
        anInt4375++
        var i_8_ = i
        while (this.anInt4377 > i_8_) {
            val class348 = this.aClass348Array4374[i_8_]
            while (true) {
                val class348_9_ = class348!!.aClass348_4294
                if (class348_9_ === class348) break
                class348_9_!!.unlink(54.toByte())
            }
            i_8_++
        }
        aClass348_4389 = null
        aClass348_4390 = null
    }

    fun method3482(i: Int): Class348? {
        anInt4381++
        if (anInt4391 > i && (aClass348_4390 !== this.aClass348Array4374[-1 + anInt4391])) {
            val class348 = aClass348_4390
            aClass348_4390 = class348!!.aClass348_4294
            return class348
        }
        while (this.anInt4377 > anInt4391) {
            val class348 = (this.aClass348Array4374[anInt4391++]!!.aClass348_4294)
            if (this.aClass348Array4374[-1 + anInt4391] !== class348) {
                aClass348_4390 = class348!!.aClass348_4294
                return class348
            }
        }
        return null
    }

    fun put(i: Byte, l: Long, class348: Class348?) {
        try {
            anInt4382++
            if (i < 18) method3481(71)
            if (class348!!.aClass348_4295 != null) class348.unlink(57.toByte())
            val class348_10_ = (this.aClass348Array4374[(l and (-1 + this.anInt4377).toLong()).toInt()])
            class348.aClass348_4294 = class348_10_
            class348.aClass348_4295 = class348_10_!!.aClass348_4295
            class348.aClass348_4295!!.aClass348_4294 = class348
            class348.aClass348_4294!!.aClass348_4295 = class348
            class348.aLong4291 = l
        } catch (runtimeexception: RuntimeException) {
            throw ItemType.method2929(runtimeexception, ("eq.K(" + i + ',' + l + ',' + (if (class348 != null) "{...}" else "null") + ')'))
        }
    }

    fun method3484(i: Int): Class348? {
        anInt4391 = i
        anInt4386++
        return method3482(0)
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

    companion object {
        var anInt4375: Int = 0
        var anInt4379: Int = 0
        var anInt4381: Int = 0
        var anInt4382: Int = 0
        var anInt4384: Int = 0
        var anInt4386: Int = 0
    }
}
