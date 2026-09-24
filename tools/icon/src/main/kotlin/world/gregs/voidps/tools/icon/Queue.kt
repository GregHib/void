package world.gregs.voidps.tools.icon

/* Class107 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Queue {
    var aClass348_Sub42_1647: Class348_Sub42 = Class348_Sub42()

    fun method1008(i: Int): Class348_Sub42? {
        if (i != 20) aClass348_Sub42_1652 = null
        anInt1653++
        val class348_sub42 = (this.aClass348_Sub42_1647.aClass348_Sub42_7063)
        if (class348_sub42 === this.aClass348_Sub42_1647) return null
        class348_sub42.unlink2(true)
        return class348_sub42
    }

    private var aClass348_Sub42_1652: Class348_Sub42? = null

    fun add(bool: Boolean, class348_sub42: Class348_Sub42) {
        if (class348_sub42.aClass348_Sub42_7060 != null) class348_sub42.unlink2(bool)
        anInt1654++
        class348_sub42.aClass348_Sub42_7063 = this.aClass348_Sub42_1647
        class348_sub42.aClass348_Sub42_7060 = (this.aClass348_Sub42_1647.aClass348_Sub42_7060)
        if (bool == true) {
            class348_sub42.aClass348_Sub42_7060.aClass348_Sub42_7063 = class348_sub42
            class348_sub42.aClass348_Sub42_7063.aClass348_Sub42_7060 = class348_sub42
        }
    }

    init {
        this.aClass348_Sub42_1647.aClass348_Sub42_7060 = this.aClass348_Sub42_1647
        this.aClass348_Sub42_1647.aClass348_Sub42_7063 = this.aClass348_Sub42_1647
    }

    companion object {
        var anInt1653: Int = 0
        var anInt1654: Int = 0
    }
}
