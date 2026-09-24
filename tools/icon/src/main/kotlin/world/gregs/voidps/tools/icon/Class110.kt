package world.gregs.voidps.tools.icon

import java.awt.Canvas

/* Class110 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class110 {
    var anInt1705: Int = 0

    fun method1035(i: Int, i_16_: Int, canvas: Canvas?, i_17_: Int): Class348_Sub31? {
        anInt1705++
        if (i != 9029) return null
        try {
            val class348_sub31: Class348_Sub31 = Class348_Sub31_Sub1()
            class348_sub31.method3008(canvas!!, i_17_, -90, i_16_)
            return class348_sub31
        } catch (throwable: Throwable) {
            val class348_sub31_sub2 = Class348_Sub31_Sub2()
            class348_sub31_sub2.method3008(canvas!!, i_17_, -128, i_16_)
            return class348_sub31_sub2
        }
    }
}
