package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

@Suppress("ArrayInDataClass")
data class QuickChatPhraseDefinition(
    override var id: Int = -1,
    var stringParts: Array<String>? = null,
    var responses: IntArray? = null,
    var ids: Array<IntArray>? = null,
    var types: IntArray? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {

    fun buildString(enums: Array<EnumDefinition>, items: Array<ItemDefinition>, data: ByteArray) = buildString(80) {
        val (_, stringParts, _, ids, types) = this@QuickChatPhraseDefinition
        if (stringParts != null) {
            if (types != null && ids != null) {
                val reader = BufferReader(data)
                for (index in types.indices) {
                    append(stringParts[index])
                    val count = QuickChatType.getType(types[index])?.bitCount ?: 0
                    val key = reader.readBits(count)
                    val string = when (getType(index)) {
                        QuickChatType.MultipleChoice -> enums.get(ids[index].first()).getString(key)
                        QuickChatType.AllItems, QuickChatType.TradeItems -> items.get(key).name
                        QuickChatType.SlayerAssignment, QuickChatType.ClanRank, QuickChatType.SkillExperience -> enums.get(ids[index].first()).getString(key)
                        else -> key.toString()
                    }
                    append(string)
                }
            }
            append(stringParts.last())
        }
    }

    fun getType(index: Int): QuickChatType? {
        return QuickChatType.getType(types?.getOrNull(index) ?: return null)
    }

    override fun toString(): String {
        return "QuickChatOptionDefinition(id=$id, stringParts=${stringParts?.contentToString()}, options=${responses?.contentToString()}, ids=${ids?.contentDeepToString()}, types=${types?.contentToString()})"
    }

    companion object {
        val EMPTY = QuickChatPhraseDefinition()
    }
}