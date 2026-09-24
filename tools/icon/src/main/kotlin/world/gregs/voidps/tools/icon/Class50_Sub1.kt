package world.gregs.voidps.tools.icon

/* Class50_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class50_Sub1 {
    var anInt5215: Int = 0

    fun unwrap(bool: Boolean, `object`: Any?, i: Int): ByteArray? {
        anInt5215++
        if (`object` == null) return null
        if (`object` is ByteArray) {
            val `is` = `object`
            if (bool) return ha_Sub3.method3873(`is`, 0)
            return `is`
        }
        if (i != 53146732) return null
        if (`object` is Class344) {
            val class344 = `object`
            return class344.method2692(-3672)
        }
        throw IllegalArgumentException()
    }
}
