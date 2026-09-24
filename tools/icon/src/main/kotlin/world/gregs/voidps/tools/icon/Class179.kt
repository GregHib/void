package world.gregs.voidps.tools.icon

/* Class179 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class179 {
    var anInt2361: Int = 1
    var anInt2363: Int = 0

    fun wrap(`is`: ByteArray?, bool: Boolean, i: Byte): Any? {
        if (i < 73) anInt2361 = -51
        anInt2363++
        if (`is` == null) return null
        if (`is`.size > 136 && !Class17.aBoolean247) {
            try {
                val class344: Class344 = Class344_Sub1()
                class344.method2691(62.toByte(), `is`)
                return class344
            } catch (throwable: Throwable) {
                Class17.aBoolean247 = true
            }
        }
        if (bool) return ha_Sub3.method3873(`is`, 0)
        return `is`
    }
}
