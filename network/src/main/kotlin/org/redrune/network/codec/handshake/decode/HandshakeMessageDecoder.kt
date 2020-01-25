package org.redrune.network.codec.handshake.decode

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.handshake.HandshakeOpcodes
import org.redrune.network.codec.handshake.UpdateMessageDecoder
import org.redrune.network.codec.handshake.message.HandshakeMessage
import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeMessageDecoder : UpdateMessageDecoder<HandshakeMessage>(4, HandshakeOpcodes.SERVICE_UPDATE) {

    override fun decode(reader: PacketReader, ctx: ChannelHandlerContext): HandshakeMessage {
        val version = reader.readInt()
        return HandshakeMessage(version)
    }

}