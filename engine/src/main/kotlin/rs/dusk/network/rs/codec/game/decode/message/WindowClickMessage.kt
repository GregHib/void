package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * A click on the game window
 * @param hash Hash of last time since last click (max 32767) & right click boolean (time | rightClick << 15)
 * @param position Position hash (x | y << 16)
 */
data class WindowClickMessage(val hash: Int, val position: Int) : Message {
    companion object : MessageCompanion<WindowClickMessage>()
}