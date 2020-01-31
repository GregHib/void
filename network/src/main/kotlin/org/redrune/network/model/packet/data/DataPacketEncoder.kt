package org.redrune.network.model.packet.data

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class DataPacketEncoder : MessageToByteEncoder<DataPacket>() {
    override fun encode(ctx: ChannelHandlerContext, msg: DataPacket, out: ByteBuf) {
        out.writeBytes(msg.payload)
    }
}