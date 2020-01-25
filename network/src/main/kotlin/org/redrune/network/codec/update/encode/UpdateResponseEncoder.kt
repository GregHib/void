package org.redrune.network.codec.update.encode

import org.redrune.network.codec.handshake.UpdateMessageEncoder
import org.redrune.network.codec.update.message.ClientResponseMessage
import org.redrune.network.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:54 p.m.
 */
class UpdateResponseEncoder : UpdateMessageEncoder<ClientResponseMessage>() {
    override fun encode(buf: PacketBuilder, msg: ClientResponseMessage) {
        buf.writeSmart(msg.code.opcode)
    }
}