package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class QuickChatPhraseDefinition(
    override var id: Int = -1,
    var stringParts: Array<String>? = null,
    var responses: IntArray? = null,
    var ids: Array<IntArray>? = null,
    var types: IntArray? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {

    fun buildString(enums: Array<EnumDefinition>, items: Array<ItemDefinition>, data: ByteArray) = buildString(80) {
        val (_, stringParts, _, ids, types) = this@QuickChatPhraseDefinition
        if (stringParts != null) {
            if (types != null && ids != null) {
                val reader = BufferReader(data)
                for (index in types.indices) {
                    append(stringParts[index])
                    val type = getType(index)
                    val key = when (type?.byteCount) {
                        4 -> reader.readInt()
                        2 -> reader.readShort()
                        1 -> reader.readByte()
                        else -> 0
                    }
                    val string = when (type) {
                        QuickChatType.MultipleChoice -> enums[ids[index].first()].getString(key)
                        QuickChatType.AllItems, QuickChatType.TradeItems -> items[key].name
                        QuickChatType.SlayerAssignment -> {
                            enums[ids[index].first()].getString(key)
                        }
                        QuickChatType.ClanRank, QuickChatType.SkillExperience -> enums[ids[index].first()].getString(key)
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

    override fun toString(): String = "QuickChatOptionDefinition(id=$id, stringParts=${stringParts?.contentToString()}, options=${responses?.contentToString()}, ids=${ids?.contentDeepToString()}, types=${types?.contentToString()})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuickChatPhraseDefinition

        if (id != other.id) return false
        if (stringParts != null) {
            if (other.stringParts == null) return false
            if (!stringParts.contentEquals(other.stringParts)) return false
        } else if (other.stringParts != null) {
            return false
        }
        if (responses != null) {
            if (other.responses == null) return false
            if (!responses.contentEquals(other.responses)) return false
        } else if (other.responses != null) {
            return false
        }
        if (ids != null) {
            if (other.ids == null) return false
            if (!ids.contentDeepEquals(other.ids)) return false
        } else if (other.ids != null) {
            return false
        }
        if (types != null) {
            if (other.types == null) return false
            if (!types.contentEquals(other.types)) return false
        } else if (other.types != null) {
            return false
        }
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (stringParts?.contentHashCode() ?: 0)
        result = 31 * result + (responses?.contentHashCode() ?: 0)
        result = 31 * result + (ids?.contentDeepHashCode() ?: 0)
        result = 31 * result + (types?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = QuickChatPhraseDefinition()
    }
}
