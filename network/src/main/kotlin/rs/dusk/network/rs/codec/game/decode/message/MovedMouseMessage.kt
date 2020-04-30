package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

class MovedMouseMessage : Message {
    companion object : MessageCompanion<MovedMouseMessage>()
}