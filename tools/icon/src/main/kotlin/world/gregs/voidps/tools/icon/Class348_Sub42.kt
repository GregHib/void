package world.gregs.voidps.tools.icon

/* Class348_Sub42 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal open class Class348_Sub42 : Class348() {
    var aClass348_Sub42_7060: Class348_Sub42? = null
    var aClass348_Sub42_7063: Class348_Sub42? = null
    var key2: Long = 0

    fun unlink2(bool: Boolean) {
        anInt7064++
        if (bool != true) method3163(50.toByte())
        if (this.aClass348_Sub42_7060 != null) {
            this.aClass348_Sub42_7060!!.aClass348_Sub42_7063 = this.aClass348_Sub42_7063
            this.aClass348_Sub42_7063!!.aClass348_Sub42_7060 = this.aClass348_Sub42_7060
            this.aClass348_Sub42_7060 = null
            this.aClass348_Sub42_7063 = null
        }
    }

    companion object {
        var anInt7062: Int = 0
        var anInt7064: Int = 0

        // Trimmed for item_renderer_standalone: the original body reset a
        // handful of unrelated static packet/cache buffers not present in this
        // standalone item-icon renderer. Not in the genuine-members list for
        // this class -- every reachable call site passes method3162(true),
        // which never triggers the "if (bool != true) method3163(...)" guard
        // below, so the body is elided.
        fun method3163(i: Byte) {
            anInt7062++
        }
    }
}
