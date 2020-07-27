package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Quick chat private message sent to another player
 * @param name The friends display name
 * @param file The quick chat file id
 * @param data Any additional display data required (skill levels etc...)
 */
data class PrivateQuickChatMessage(val name: String, val file: Int, val data: ByteArray?) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateQuickChatMessage

        if (name != other.name) return false
        if (file != other.file) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + file
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}