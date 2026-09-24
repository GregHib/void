package world.gregs.voidps.tools.icon

import java.lang.ref.SoftReference

/* Class348_Sub42_Sub8_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub42_Sub8_Sub1(`object`: Any?, i: Int) : Class348_Sub42_Sub8(i) {
    private val aSoftReference10428: SoftReference<*>

    override fun method3195(i: Int): Boolean {
        return true
    }

    override fun method3193(i: Int): Any? {
        if (i <= 75) return null
        return aSoftReference10428.get()
    }

    init {
        aSoftReference10428 = SoftReference<Any?>(`object`)
    }
}
