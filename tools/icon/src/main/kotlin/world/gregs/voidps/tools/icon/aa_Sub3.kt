package world.gregs.voidps.tools.icon

/* aa_Sub3 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class aa_Sub3(i: Int, i_3_: Int, `is`: IntArray?, is_4_: IntArray?) : aa() {
    var anIntArray5201: IntArray?
    var anIntArray5202: IntArray?

    init {
        try {
            this.anIntArray5201 = `is`
            this.anIntArray5202 = is_4_
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("nba.<init>(" + i + ',' + i_3_ + ',' + (if (`is` != null) "{...}" else "null") + ',' + (if (is_4_ != null) "{...}" else "null") + ')'))
        }
    }

    companion object {
        var aClass348_Sub6_5206: Class348_Sub6 = Class348_Sub6(0, 0)
    }
}
