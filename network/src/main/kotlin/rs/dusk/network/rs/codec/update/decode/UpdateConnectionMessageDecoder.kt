package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateConnectionMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateConnectionMessageDecoder : UpdateMessageDecoder<UpdateConnectionMessage>(3) {

    override fun decode(packet: PacketReader): UpdateConnectionMessage {
        val value = packet.readUnsignedMedium()
        return UpdateConnectionMessage(value)
    }

}