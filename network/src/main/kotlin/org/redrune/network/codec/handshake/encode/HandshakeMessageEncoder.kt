package org.redrune.network.codec.handshake.encode

import org.redrune.network.codec.handshake.model.HandshakeMessage
import org.redrune.network.codec.message.MessageEncoder
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class HandshakeMessageEncoder: MessageEncoder<HandshakeMessage>() {
    override fun encode(message: HandshakeMessage): Packet {
        return PacketBuilder().writeByte(message.response).build()
    }
}