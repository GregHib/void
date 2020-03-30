package org.redrune.network.rs.codec.update.decode

import org.redrune.core.network.codec.packet.access.PacketReader
import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.network.rs.codec.update.UpdateMessageDecoder
import org.redrune.network.rs.codec.update.decode.message.UpdateDisconnectionMessage
import org.redrune.utility.constants.network.FileServerOpcodes.DISCONNECTED

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [DISCONNECTED], length = 3)
class UpdateDisconnectionMessageDecoder : UpdateMessageDecoder<UpdateDisconnectionMessage>() {

    override fun decode(packet: PacketReader): UpdateDisconnectionMessage {
        val value = packet.readMedium()
        return UpdateDisconnectionMessage(value)
    }

}