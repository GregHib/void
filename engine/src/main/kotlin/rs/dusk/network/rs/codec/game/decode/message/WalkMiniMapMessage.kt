package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Request for player player to move from current position to a new position via mini-map
 * @param x The target tile x coordinate
 * @param y The target tile y coordinate
 * @param running Whether the client is displaying the player as running
 */
data class WalkMiniMapMessage(val x: Int, val y: Int, val running: Boolean) : Message {
    companion object : MessageCompanion<WalkMiniMapMessage>()
}