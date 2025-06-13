package world.gregs.voidps.engine.entity.character.player.chat.global

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * A quick-chat message sent nearby from [source].
 */
data class PublicQuickChatMessage(
    val source: Player,
    val script: Int,
    val file: Int,
    val message: String,
    val data: ByteArray,
) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "public_quick_chat_message"
        else -> null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicQuickChatMessage

        if (source != other.source) return false
        if (script != other.script) return false
        if (file != other.file) return false
        if (message != other.message) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + script
        result = 31 * result + file
        result = 31 * result + message.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
