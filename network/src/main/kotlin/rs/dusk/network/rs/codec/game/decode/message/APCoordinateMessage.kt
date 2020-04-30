package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

data class APCoordinateMessage(val first: Int, val second: Int, val third: Int, val fourth: Int, val fifth: Int) :
    Message {
    companion object : MessageCompanion<APCoordinateMessage>()
}