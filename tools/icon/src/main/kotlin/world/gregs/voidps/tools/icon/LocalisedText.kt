package world.gregs.voidps.tools.icon

/* Class274 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class LocalisedText private constructor(string: String?, string_7_: String?, string_8_: String?, string_9_: String?) {
    private val aStringArray3481: Array<String?>
    override fun toString(): String {
        throw IllegalStateException()
    }

    fun method2063(i: Int): String? {
        return aStringArray3481[i]
    }

    init {
        aStringArray3481 = arrayOf<String?>(string, string_7_, string_8_, string_9_)
    }

    companion object {

        var Discard: LocalisedText?
        var TAKE: LocalisedText?
        var DROP: LocalisedText?

        init {
            Discard = LocalisedText("Discard", "Ablegen", "Jeter", "Descartar")
            TAKE = LocalisedText("Take", "Nehmen", "Prendre", "Pegar")
            DROP = LocalisedText("Drop", "Fallen lassen", "Poser", "Largar")
        }
    }
}
