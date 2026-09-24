package world.gregs.voidps.tools.icon

/* aa_Sub3 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*
* Trimmed for item_renderer_standalone: only the static field used as the
* models archive index storage (aa_Sub3.aClass45_5207) is kept.
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
        var aJs5_5207: Js5? = null
        var aClass348_Sub6_5206: Class348_Sub6 = Class348_Sub6(0, 0)
    }
}
