package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * A quick-chat message a player wants (but has yet) to say directly to a [friend].
 */
data class PrivateQuickChat(
    val friend: String,
    val file: Int,
    val data: ByteArray
) : Event {

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "private_quick_chat"
        else -> null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateQuickChat

        if (friend != other.friend) return false
        if (file != other.file) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = friend.hashCode()
        result = 31 * result + file
        result = 31 * result + data.contentHashCode()
        return result
    }
}