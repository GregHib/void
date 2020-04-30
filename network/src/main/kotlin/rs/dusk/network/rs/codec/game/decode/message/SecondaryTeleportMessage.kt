package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Teleport request send when attempted using action 11 (unknown) and isn't a mod
 */
data class SecondaryTeleportMessage(val x: Int, val y: Int) : Message {
    companion object : MessageCompanion<SecondaryTeleportMessage>()
}