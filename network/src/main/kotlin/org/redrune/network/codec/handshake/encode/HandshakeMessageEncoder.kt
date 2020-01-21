package org.redrune.network.codec.handshake.encode

import org.redrune.network.codec.handshake.decode.message.HandshakeMessage
import org.redrune.network.codec.message.MessageEncoder
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketWriter

class HandshakeMessageEncoder: MessageEncoder<HandshakeMessage>() {
    override fun encode(message: HandshakeMessage): Packet {
        val writer = PacketWriter()
        writer.writeByte(message.response)
        return writer.toPacket()
    }
}