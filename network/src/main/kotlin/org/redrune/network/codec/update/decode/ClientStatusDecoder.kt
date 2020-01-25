package org.redrune.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.handshake.UpdateMessageDecoder
import org.redrune.network.codec.update.message.ClientStatusMessage
import org.redrune.network.codec.update.message.FileRequestType.CLIENT_LOGGED_IN
import org.redrune.network.codec.update.message.FileRequestType.CLIENT_LOGGED_OUT
import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 9:02 p.m.
 */
class ClientStatusDecoder : UpdateMessageDecoder<ClientStatusMessage>(
    3,
    CLIENT_LOGGED_IN.opcode,
    CLIENT_LOGGED_OUT.opcode
) {
    override fun decode(reader: PacketReader, ctx: ChannelHandlerContext): ClientStatusMessage {
        val value = reader.readMedium()
        return ClientStatusMessage(reader.opcode == CLIENT_LOGGED_IN.opcode, value)
    }
}