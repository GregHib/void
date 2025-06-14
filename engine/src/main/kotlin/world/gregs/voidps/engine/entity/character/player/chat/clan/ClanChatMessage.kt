package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * A freeform [message] sent nearby from [source].
 */
data class ClanChatMessage(
    val source: Player,
    val effects: Int,
    val message: String,
    val compressed: ByteArray,
) : Event {
    constructor(source: Player, effects: Int, message: String, huffman: Huffman) : this(source, effects, message, huffman.compress(message))

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "clan_chat_message"
        else -> null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClanChatMessage

        if (source != other.source) return false
        if (effects != other.effects) return false
        if (message != other.message) return false
        if (!compressed.contentEquals(other.compressed)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + effects
        result = 31 * result + message.hashCode()
        result = 31 * result + compressed.contentHashCode()
        return result
    }
}
