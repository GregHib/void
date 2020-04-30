package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * @param keys key's pressed - Pair<Key, Time>
 */
data class KeysPressedMessage(val keys: List<Pair<Int, Int>>) : Message {
    companion object : MessageCompanion<KeysPressedMessage>()
}