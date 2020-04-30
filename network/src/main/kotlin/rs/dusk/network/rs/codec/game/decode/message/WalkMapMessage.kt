package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Request for a player to move from current position to a new position on the map
 * @param x The target tile x coordinate
 * @param y The target tile y coordinate
 * @param running Whether the client is displaying the player as running
 */
data class WalkMapMessage(val x: Int, val y: Int, val running: Boolean) : Message {
    //TODO test running
    companion object : MessageCompanion<WalkMapMessage>()
}