package org.redrune.network.rs.codec.update.decode

import org.redrune.core.network.codec.packet.access.PacketReader
import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.network.rs.codec.update.UpdateMessageDecoder
import org.redrune.network.rs.codec.update.decode.message.UpdateLoginStatusMessage
import org.redrune.utility.constants.network.FileServerOpcodes.STATUS_LOGGED_IN
import org.redrune.utility.constants.network.FileServerOpcodes.STATUS_LOGGED_OUT

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [STATUS_LOGGED_IN, STATUS_LOGGED_OUT], length = 3)
class UpdateLoginStatusMessageDecoder : UpdateMessageDecoder<UpdateLoginStatusMessage>() {

    override fun decode(packet: PacketReader): UpdateLoginStatusMessage {
        val value = packet.readMedium()
        return UpdateLoginStatusMessage(packet.opcode == STATUS_LOGGED_IN, value)
    }

}