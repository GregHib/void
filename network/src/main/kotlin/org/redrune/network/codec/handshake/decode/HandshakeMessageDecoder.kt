package org.redrune.network.codec.handshake.decode

import org.redrune.network.codec.handshake.decode.message.HandshakeMessage
import org.redrune.network.codec.message.MessageDecoder
import org.redrune.network.packet.PacketReader
import kotlin.experimental.and

class HandshakeMessageDecoder: MessageDecoder<HandshakeMessage>(intArrayOf()) {
    override fun decode(packet: PacketReader): HandshakeMessage {
        return HandshakeMessage((packet.readByte() and 0xFF.toByte()).toInt())
    }
}