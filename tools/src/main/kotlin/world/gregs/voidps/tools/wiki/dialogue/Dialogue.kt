package world.gregs.voidps.tools.wiki.dialogue

import net.pearx.kasechange.toCamelCase

internal data class Dialogue(
    val id: Int,
    val type: String,
    var next: List<Int>,
    var previous: List<Int>,
    val options: List<String> = emptyList(),
    val text: String = "",
    val name: String = ""
) {

    val dialogue: String
        get() = if (type == OPTIONS) options.joinToString(", ") else "$name: $text"

    fun methodName(): String {
        return toMethodName(if (type == OPTIONS) options.first() else text)
    }

    fun toMap() = if (type == "DIALOGUE_OPTIONS") {
        mapOf(
            "type" to type,
            "next" to next,
            "previous" to previous,
            "options" to options)
    } else {
        mapOf(
            "type" to type,
            "next" to next,
            "previous" to previous,
            "text" to text,
            "name" to name)
    }

    fun print() {
        if (text.contains(':')) {
            return
        }
        println("// dialogue $id")
        when (type) {
            "DIALOGUE_OPTIONS" -> {
                println("""
                        choice("${options.first()}") {
                            ${options.drop(1).map { "option(\"${it}\")\n" }}
                        }
                    """)
            }
            "DIALOGUE_NPC" -> println("npc<Talk>(\"${text}\")")
            "DIALOGUE_PLAYER" -> println("player<Talk>(\"${text}\")")
        }
    }

    companion object {

        const val OPTIONS = "DIALOGUE_OPTIONS"
        const val NPC = "DIALOGUE_NPC"
        const val PLAYER = "DIALOGUE_PLAYER"

        fun toMethodName(text: String) = "${
            text.replace("%", "").replace(";", "").replace("!", "").replace(":", "").replace("(", "").replace(")", "").replace("?", "").replace(",", "").replace("*", "").replace("'", "").toCamelCase()
        }()"

        fun fromMap(map: Map<String, Any>, key: String) = Dialogue(
            key.toInt(),
            map["type"] as String,
            map["next"] as List<Int>,
            map["previous"] as List<Int>,
            map["options"] as? List<String> ?: emptyList(),
            map["text"] as? String ?: "",
            map["name"] as? String ?: ""
        )
    }
}