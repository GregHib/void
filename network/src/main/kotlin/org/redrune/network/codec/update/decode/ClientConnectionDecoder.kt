package org.redrune.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.handshake.UpdateMessageDecoder
import org.redrune.network.codec.update.message.ClientConnectionMessage
import org.redrune.network.codec.update.message.FileRequestType
import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:47 p.m.
 */
class ClientConnectionDecoder :
    UpdateMessageDecoder<ClientConnectionMessage>(3, FileRequestType.CONNECTION_INITIATED.opcode) {
    override fun decode(reader: PacketReader, ctx: ChannelHandlerContext): ClientConnectionMessage {
        val value = reader.readMedium()
        return ClientConnectionMessage(value)
    }
}