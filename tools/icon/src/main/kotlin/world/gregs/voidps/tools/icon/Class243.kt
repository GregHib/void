package world.gregs.voidps.tools.icon

/* Class243 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class243 {
    private val aClass318_3166 = Class318()
    private var aClass318_3167: Class318? = null

    fun method1869(i: Int, class318: Class318) {
        if (class318.aClass318_3976 != null) class318.method2373(false)
        anInt3158++
        class318.aClass318_3976 = aClass318_3166.aClass318_3976
        if (i > -81) aClass318_3167 = null
        class318.aClass318_3970 = aClass318_3166
        class318.aClass318_3976!!.aClass318_3970 = class318
        class318.aClass318_3970!!.aClass318_3976 = class318
    }

    fun method1870(i: Int): Class318? {
        if (i > -103) aClass318_3167 = null
        anInt3162++
        val class318 = aClass318_3166.aClass318_3976
        if (aClass318_3166 === class318) {
            aClass318_3167 = null
            return null
        }
        aClass318_3167 = class318!!.aClass318_3976
        return class318
    }

    fun method1872(i: Int): Class318? {
        anInt3163++
        val class318 = aClass318_3166.aClass318_3970
        if (i != 8) method1878(126.toByte())
        if (class318 === aClass318_3166) {
            aClass318_3167 = null
            return null
        }
        aClass318_3167 = class318!!.aClass318_3970
        return class318
    }

    fun method1875(i: Int): Class318? {
        anInt3160++
        val class318 = aClass318_3166.aClass318_3970
        if (class318 === aClass318_3166) return null
        class318!!.method2373(false)
        if (i != 60) method1878(16.toByte())
        return class318
    }

    fun method1878(i: Byte): Class318? {
        anInt3159++
        val class318 = aClass318_3167
        val i_1_ = -59 % ((67 - i) / 55)
        if (class318 === aClass318_3166) {
            aClass318_3167 = null
            return null
        }
        aClass318_3167 = class318!!.aClass318_3970
        return class318
    }

    init {
        aClass318_3166.aClass318_3976 = aClass318_3166
        aClass318_3166.aClass318_3970 = aClass318_3166
    }

    companion object {
        var anInt3158: Int = 0
        var anInt3159: Int = 0
        var anInt3160: Int = 0
        var anInt3162: Int = 0
        var anInt3163: Int = 0
    }
}
