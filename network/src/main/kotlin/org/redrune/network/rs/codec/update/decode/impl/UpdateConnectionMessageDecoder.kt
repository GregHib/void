package org.redrune.network.rs.codec.update.decode.impl

import org.redrune.core.network.codec.packet.access.PacketReader
import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.network.rs.codec.update.decode.UpdateMessageDecoder
import org.redrune.network.rs.codec.update.decode.message.UpdateConnectionMessage
import org.redrune.utility.constants.FileServerOpcodes.CONNECTED

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