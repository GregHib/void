package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * A direct freeform [message] sent from [source].
 */
data class PrivateChatMessage(
    val source: Player,
    val message: String,
    val compressed: ByteArray,
) : Event {
    constructor(source: Player, message: String, huffman: Huffman) : this(source, message, huffman.compress(message))

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "private_chat_message"
        else -> null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateChatMessage

        if (source != other.source) return false
        if (message != other.message) return false
        if (!compressed.contentEquals(other.compressed)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + compressed.contentHashCode()
        return result
    }
}
