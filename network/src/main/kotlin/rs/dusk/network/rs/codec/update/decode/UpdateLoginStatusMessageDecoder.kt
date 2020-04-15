package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateLoginStatusMessage
import rs.dusk.utility.constants.network.FileServerOpcodes.STATUS_LOGGED_IN
import rs.dusk.utility.constants.network.FileServerOpcodes.STATUS_LOGGED_OUT

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