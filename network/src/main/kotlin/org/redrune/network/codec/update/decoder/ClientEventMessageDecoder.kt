package org.redrune.network.codec.update.decoder

import org.redrune.network.codec.update.UpdateOpcodes.STATUS_LOGGED_IN
import org.redrune.network.codec.update.UpdateOpcodes.STATUS_LOGGED_OUT
import org.redrune.network.codec.update.message.impl.ClientEventMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class ClientEventMessageDecoder : MessageDecoder(3, STATUS_LOGGED_IN, STATUS_LOGGED_OUT) {
    override fun decode(reader: PacketReader): Message {
        val status = reader.readTriByte()
        return ClientEventMessage(online = reader.opcode == STATUS_LOGGED_IN)
    }

}
