package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.update.FileServerOpcodes.DISCONNECTED
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateDisconnectionMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [DISCONNECTED], length = 3)
class UpdateDisconnectionMessageDecoder : UpdateMessageDecoder<UpdateDisconnectionMessage>() {

    override fun decode(packet: PacketReader): UpdateDisconnectionMessage {
        val value = packet.readUnsignedMedium()
        return UpdateDisconnectionMessage(value)
    }

}