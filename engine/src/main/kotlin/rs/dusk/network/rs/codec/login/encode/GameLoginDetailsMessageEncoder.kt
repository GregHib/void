package rs.dusk.network.rs.codec.login.encode

import io.netty.channel.Channel
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeString
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.network.rs.codec.game.GameOpcodes.LOGIN_DETAILS

class GameLoginDetailsMessageEncoder : MessageEncoder(LOGIN_DETAILS, PacketSize.BYTE) {

    fun encode(
        channel: Channel,
        rights: Int,
        clientIndex: Int,
        displayName: String
    ) = channel.send(13 + string(displayName)) {
        writeByte(rights)
        writeByte(0)// Unknown - something to do with skipping chat messages
        writeByte(0)
        writeByte(0)
        writeByte(0)
        writeByte(0)// Moves chat box position
        writeShort(clientIndex)
        writeByte(true)
        writeMedium(0)
        writeByte(true)
        writeString(displayName)
    }

}
