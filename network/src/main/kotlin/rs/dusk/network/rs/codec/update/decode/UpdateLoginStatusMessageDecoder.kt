package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateLoginStatusMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateLoginStatusMessageDecoder(private val online: Boolean) : UpdateMessageDecoder<UpdateLoginStatusMessage>(3) {

    override fun decode(packet: PacketReader): UpdateLoginStatusMessage {
        val value = packet.readUnsignedMedium()
        return UpdateLoginStatusMessage(online, value)
    }

}