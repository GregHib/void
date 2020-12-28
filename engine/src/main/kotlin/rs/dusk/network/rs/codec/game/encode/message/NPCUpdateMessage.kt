package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.buffer.write.BufferWriter
import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
data class NPCUpdateMessage(val changes: BufferWriter = BufferWriter(), val updates: BufferWriter = BufferWriter()) :
    Message {
    fun release() {
        changes.buffer.clear()
        updates.buffer.clear()
    }
}