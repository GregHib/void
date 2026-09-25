package world.gregs.voidps.tools.render

/* Class348_Sub42 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

open class Class348_Sub42 : Class348() {
    var aClass348_Sub42_7060: Class348_Sub42? = null
    var aClass348_Sub42_7063: Class348_Sub42? = null

    fun unlink2() {
        if (this.aClass348_Sub42_7060 != null) {
            this.aClass348_Sub42_7060!!.aClass348_Sub42_7063 = this.aClass348_Sub42_7063
            this.aClass348_Sub42_7063!!.aClass348_Sub42_7060 = this.aClass348_Sub42_7060
            this.aClass348_Sub42_7060 = null
            this.aClass348_Sub42_7063 = null
        }
    }
}
