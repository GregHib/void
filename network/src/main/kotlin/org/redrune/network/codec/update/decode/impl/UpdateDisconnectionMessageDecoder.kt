package org.redrune.network.codec.update.decode.impl

import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.core.network.model.packet.access.PacketReader
import org.redrune.network.codec.update.decode.UpdateMessageDecoder
import org.redrune.network.codec.update.decode.message.UpdateDisconnectionMessage
import org.redrune.utility.constants.FileServerOpcodes.DISCONNECTED

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