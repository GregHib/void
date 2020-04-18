package rs.dusk.network.rs.codec.login.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameOpcodes.LOGIN_DETAILS
import rs.dusk.network.rs.codec.login.LoginMessageEncoder
import rs.dusk.network.rs.codec.login.encode.message.GameLoginDetails

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameLoginDetailsMessageEncoder : LoginMessageEncoder<GameLoginDetails>() {

    override fun encode(builder: PacketWriter, msg: GameLoginDetails) {
        builder.apply {
            writeOpcode(LOGIN_DETAILS, PacketType.BYTE)
            writeByte(2)//Rights
            writeByte(0)//Unknown - something to do with skipping chat messages
            writeByte(0)
            writeByte(0)
            writeByte(0)
            writeByte(0)//Moves chat box position
            writeShort(1)//Player index
            writeByte(true)
            writeMedium(0)
            writeByte(true)
            writeString("Greg")//Display name
        }
    }

}
