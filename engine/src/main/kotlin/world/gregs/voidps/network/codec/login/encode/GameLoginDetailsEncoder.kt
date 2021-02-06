package world.gregs.voidps.network.codec.login.encode

import io.netty.channel.Channel
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.service.ServiceCodec
import world.gregs.voidps.network.codec.service.ServiceOpcodes
import world.gregs.voidps.network.packet.PacketSize

class GameLoginDetailsEncoder : Encoder(ServiceOpcodes.LOGIN_DETAILS, PacketSize.BYTE) {

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
