package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.update.decode.message.UpdateDisconnectionMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateDisconnectionMessageDecoder : MessageDecoder<UpdateDisconnectionMessage>(3) {

    override fun decode(packet: PacketReader): UpdateDisconnectionMessage {
        val value = packet.readUnsignedMedium()
        return UpdateDisconnectionMessage(value)
    }

}