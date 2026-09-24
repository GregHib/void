package world.gregs.voidps.tools.icon

/* Class274 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class LocalisedText private constructor(string: String?, string_7_: String?, string_8_: String?, string_9_: String?) {
    private val aStringArray3481: Array<String?>
    override fun toString(): String {
        anInt3482++
        throw IllegalStateException()
    }

    fun method2063(i: Int, i_10_: Int): String? {
        if (i_10_ != 544) method2061(126)
        anInt3479++
        return aStringArray3481[i]
    }

    init {
        try {
            aStringArray3481 = arrayOf<String?>(string, string_7_, string_8_, string_9_)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("va.<init>(" + (if (string != null) "{...}" else "null") + ',' + (if (string_7_ != null) "{...}" else "null") + ',' + (if (string_8_ != null) "{...}" else "null") + ',' + (if (string_9_ != null) "{...}" else "null") + ')'))
        }
    }

    companion object {
        var anInt3477: Int = 0
        var anInt3479: Int = 0
        var anInt3482: Int = 0

        /* NOTE: these three static instances are not touched by Class274's own
     * genuine methods, but Class255 (same batch) unconditionally reads them
     * in its genuine constructor/method1940 (e.g. Class274.aClass274_3490.
     * method2063(...)), so they are kept here for that real cross-class need. */
        var MEMBERS_OBJECT: LocalisedText?
        var Discard: LocalisedText?
        var TAKE: LocalisedText?
        var DROP: LocalisedText?
        var M: LocalisedText?
        var K: LocalisedText?

        /* NOTE: method2061 is not in the genuine-methods list (never observed
     * covered - all real call sites reachable from this renderer pass
     * i_10_==544, so "if (i_10_ != 544) method2061(126)" is unreachable
     * dead code). Kept here as a minimal stub (counter increment only,
     * original body nulled out static caches on ~10 unrelated classes
     * that are out of scope for icon rendering) purely so the genuine
     * method2063 call site resolves at compile time. */
        fun method2061(i: Int) {
            anInt3477++
        }

        init {
            MEMBERS_OBJECT = LocalisedText("Members object", "Gegenstand für Mitglieder", "Objet d'abonnés", "Objeto para membros")
            Discard = LocalisedText("Discard", "Ablegen", "Jeter", "Descartar")
            TAKE = LocalisedText("Take", "Nehmen", "Prendre", "Pegar")
            DROP = LocalisedText("Drop", "Fallen lassen", "Poser", "Largar")
            M = LocalisedText("M", "M", "M", "M")
            K = LocalisedText("K", "T", "K", "K")
        }
    }
}
