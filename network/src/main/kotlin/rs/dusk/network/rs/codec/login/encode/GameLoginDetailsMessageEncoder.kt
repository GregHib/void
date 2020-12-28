package rs.dusk.network.rs.codec.login.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameOpcodes.LOGIN_DETAILS
import rs.dusk.network.rs.codec.login.encode.message.GameLoginDetails

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameLoginDetailsMessageEncoder : MessageEncoder<GameLoginDetails>() {

    override fun encode(builder: PacketWriter, msg: GameLoginDetails) {
        val (rights, clientIndex, displayName) = msg
        builder.apply {
            writeOpcode(LOGIN_DETAILS, PacketType.BYTE)
            writeByte(rights)
            writeByte(0)//Unknown - something to do with skipping chat messages
            writeByte(0)
            writeByte(0)
            writeByte(0)
            writeByte(0)//Moves chat box position
            writeShort(clientIndex)
            writeByte(true)
            writeMedium(0)
            writeByte(true)
            writeString(displayName)
        }
    }

}
