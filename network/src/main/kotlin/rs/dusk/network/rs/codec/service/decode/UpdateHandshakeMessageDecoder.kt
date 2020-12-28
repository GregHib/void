package rs.dusk.network.rs.codec.service.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.service.ServiceMessageDecoder
import rs.dusk.network.rs.codec.service.decode.message.UpdateHandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateHandshakeMessageDecoder : ServiceMessageDecoder<UpdateHandshakeMessage>(4) {

    override fun decode(packet: PacketReader): UpdateHandshakeMessage {
        val major = packet.readInt()
        return UpdateHandshakeMessage(major)
    }

}
