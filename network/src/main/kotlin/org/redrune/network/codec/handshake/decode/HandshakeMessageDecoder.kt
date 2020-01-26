package org.redrune.network.codec.handshake.decode

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.handshake.HandshakeOpcodes
import org.redrune.network.codec.handshake.message.VersionMessage
import org.redrune.network.message.MessageDecoder
import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeMessageDecoder : MessageDecoder<VersionMessage>(4, HandshakeOpcodes.SERVICE_UPDATE) {

    override fun decode(reader: PacketReader, ctx: ChannelHandlerContext): VersionMessage {
        val version = reader.readInt()
        return VersionMessage(version)
    }

}