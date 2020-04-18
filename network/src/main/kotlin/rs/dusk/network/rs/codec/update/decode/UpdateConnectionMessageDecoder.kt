package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.update.FileServerOpcodes.CONNECTED
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateConnectionMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [CONNECTED], length = 3)
class UpdateConnectionMessageDecoder : UpdateMessageDecoder<UpdateConnectionMessage>() {

    override fun decode(packet: PacketReader): UpdateConnectionMessage {
        val value = packet.readMedium()
        return UpdateConnectionMessage(value)
    }

}