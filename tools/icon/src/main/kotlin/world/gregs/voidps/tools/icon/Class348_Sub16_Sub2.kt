package world.gregs.voidps.tools.icon

/* Class348_Sub16_Sub2 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class348_Sub16_Sub2 {
    var anInt8882: Int = 0

    fun sort(`is`: IntArray?, ls: LongArray?, i: Int) {
        try {
            IOException_Sub1.method129(i, i + -107, ls, ls!!.size - 1, `is`)
            anInt8882++
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("iha.I(" + (if (`is` != null) "{...}" else "null") + ',' + (if (ls != null) "{...}" else "null") + ',' + i + ')'))
        }
    }
}
