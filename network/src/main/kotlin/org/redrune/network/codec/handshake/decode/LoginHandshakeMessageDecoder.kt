package org.redrune.network.codec.handshake.decode

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.handshake.HandshakeOpcodes
import org.redrune.network.codec.handshake.message.LoginHandshakeMessage
import org.redrune.network.message.MessageDecoder
import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 3:03 p.m.
 */
class LoginHandshakeMessageDecoder : MessageDecoder<LoginHandshakeMessage>(0, HandshakeOpcodes.SERVICE_LOGIN) {
    override fun decode(reader: PacketReader, ctx: ChannelHandlerContext): LoginHandshakeMessage {
        return LoginHandshakeMessage(0)
    }
}