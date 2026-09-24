package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub4 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub4 : Class348_Sub40(0, true) {
    override fun method3042(i: Int, i_0_: Int): IntArray? {
        anInt9115++
        if (i_0_ != 255) aClass262_9111 = null
        val `is` = this.aClass191_7032!!.method1433(0, i)
        if (this.aClass191_7032!!.aBoolean2570) Class214.method1579(`is`!!, 0, Class348_Sub40_Sub6.Companion.anInt9139, Class239_Sub18.anIntArray6035!![i])
        return `is`
    }

    companion object {
        var aClass262_9111: Class262? = Class262()
        var aTextureSource9113: TextureSource? = null
        var anInt9115: Int = 0
    }
}
