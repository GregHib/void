package content.bot.chat

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.cache.definition.data.QuickChatType
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions

/**
 * Looks up quick chat phrases by their template text, e.g. "My Mining level is <SkillLevel>." so rules
 * can be written with readable text instead of magic phrase ids, and encodes slot values into phrase data.
 */
class QuickChatPhrases(private val definitions: QuickChatPhraseDefinitions) {
    private val ids = HashMap<String, Int>()
    private val resolvers = HashMap<Int, QuickChatEnumResolver>()

    init {
        for (definition in definitions.definitions) {
            val text = text(definition) ?: continue
            ids.putIfAbsent(text, definition.id)
        }
    }

    fun id(phrase: String): Int? = ids[phrase]

    operator fun contains(phrase: String) = ids.containsKey(phrase)

    /**
     * Encodes [args] into the phrase data bytes. Numeric slots (levels, varps, combat) are left empty as
     * they're filled in from the speaking player's own stats when the quick chat is sent.
     * @return null if an argument couldn't be resolved
     */
    fun encode(id: Int, args: List<String>): ByteArray? {
        val definition = definitions.get(id)
        val types = definition.types ?: return ByteArray(0)
        val bytes = ArrayList<Byte>(types.size * 2)
        var arg = 0
        for (index in types.indices) {
            val type = definition.getType(index) ?: return null
            val value = when (type) {
                QuickChatType.MultipleChoice -> {
                    val enum = definition.ids?.getOrNull(index)?.firstOrNull() ?: return null
                    val key = args.getOrNull(arg++) ?: return null
                    resolver(enum).index(key) ?: return null
                }
                QuickChatType.AllItems, QuickChatType.TradeItems -> {
                    val key = args.getOrNull(arg++) ?: return null
                    ItemDefinitions.getOrNull(key)?.id ?: return null
                }
                else -> 0
            }
            for (shift in type.byteCount - 1 downTo 0) {
                bytes.add((value shr (shift * 8)).toByte())
            }
        }
        return bytes.toByteArray()
    }

    private fun resolver(enum: Int): QuickChatEnumResolver = resolvers.getOrPut(enum) { QuickChatEnumResolver(EnumDefinitions.get(enum).map ?: emptyMap()) }

    companion object {
        /**
         * Splits a table spot like "adamant_platebody" into one argument per <MultipleChoice> in [phrase]
         */
        fun args(phrase: String, spot: String): List<String> {
            val slots = phrase.split("<MultipleChoice>").size - 1
            return if (slots > 1) spot.split('_', limit = slots) else listOf(spot)
        }

        /**
         * Phrase text with placeholders named after their type, the same format as `QuickChatEnumDump`
         */
        fun text(definition: QuickChatPhraseDefinition): String? {
            val parts = definition.stringParts ?: return null
            return buildString {
                for (index in parts.indices) {
                    append(parts[index])
                    if (index < parts.lastIndex) {
                        append('<').append(definition.getType(index)?.name ?: "?").append('>')
                    }
                }
            }
        }
    }
}
