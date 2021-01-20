package world.gregs.void.network.codec.login.encode

import io.netty.channel.Channel
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeString
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.LOGIN_DETAILS
import world.gregs.void.network.packet.PacketSize

class GameLoginDetailsEncoder : Encoder(LOGIN_DETAILS, PacketSize.BYTE) {

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
