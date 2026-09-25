package world.gregs.voidps.tools.icon

/* Class107 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Queue {
    var aClass348_Sub42_1647: Class348_Sub42 = Class348_Sub42()

    fun method1008(): Class348_Sub42? {
        val class348_sub42 = (this.aClass348_Sub42_1647.aClass348_Sub42_7063)
        if (class348_sub42 === this.aClass348_Sub42_1647) return null
        class348_sub42!!.unlink2()
        return class348_sub42
    }

    fun add(class348_sub42: Class348_Sub42) {
        if (class348_sub42.aClass348_Sub42_7060 != null) class348_sub42.unlink2()
        class348_sub42.aClass348_Sub42_7063 = this.aClass348_Sub42_1647
        class348_sub42.aClass348_Sub42_7060 = (this.aClass348_Sub42_1647.aClass348_Sub42_7060)
        class348_sub42.aClass348_Sub42_7060!!.aClass348_Sub42_7063 = class348_sub42
        class348_sub42.aClass348_Sub42_7063!!.aClass348_Sub42_7060 = class348_sub42
    }

    init {
        this.aClass348_Sub42_1647.aClass348_Sub42_7060 = this.aClass348_Sub42_1647
        this.aClass348_Sub42_1647.aClass348_Sub42_7063 = this.aClass348_Sub42_1647
    }
}
