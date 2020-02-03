package org.redrune.network.codec.update.decoder

import org.redrune.network.codec.update.UpdateOpcodes.CONNECTED
import org.redrune.network.codec.update.UpdateOpcodes.DISCONNECT
import org.redrune.network.codec.update.message.impl.ConnectionEventMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class ConnectionEventMessageDecoder : MessageDecoder(intArrayOf(CONNECTED, DISCONNECT), 3) {
    override fun decode(reader: PacketReader): Message {
        val value = reader.readTriByte()
        return ConnectionEventMessage(value)
    }

}