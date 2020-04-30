package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * An option selection on another player
 * @param index The selected player's index
 * @param option The option id - 3 = Trade, 4 = Attack
 */
data class PlayerOptionMessage(val index: Int, val option: Int) : Message {
    companion object : MessageCompanion<PlayerOptionMessage>()
}