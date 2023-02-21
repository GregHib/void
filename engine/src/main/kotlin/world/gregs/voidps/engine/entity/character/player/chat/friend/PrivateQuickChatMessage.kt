package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A direct quick-chat message sent from [source].
 */
data class PrivateQuickChatMessage(
    val source: Player,
    val file: Int,
    val message: String,
    val data: ByteArray
) : Event {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateQuickChatMessage

        if (source != other.source) return false
        if (file != other.file) return false
        if (message != other.message) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + file
        result = 31 * result + message.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}