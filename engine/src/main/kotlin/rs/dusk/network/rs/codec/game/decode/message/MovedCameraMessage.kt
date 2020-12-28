package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

data class MovedCameraMessage(val pitch: Int, val yaw: Int) : Message {
    companion object : MessageCompanion<MovedCameraMessage>()
}