package world.gregs.voidps.engine.entity.character.player.chat.global

import world.gregs.voidps.engine.event.Event

/**
 * A quick-chat message a player wants (but has yet) to say to everyone nearby.
 * @param chatType 0=public, 1=clan
 */
data class PublicQuickChat(
    val chatType: Int,
    val file: Int,
    val data: ByteArray
) : Event {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicQuickChat

        if (chatType != other.chatType) return false
        if (file != other.file) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chatType
        result = 31 * result + file
        result = 31 * result + data.contentHashCode()
        return result
    }
}