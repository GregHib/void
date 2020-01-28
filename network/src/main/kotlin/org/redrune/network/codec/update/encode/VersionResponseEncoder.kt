package org.redrune.network.codec.update.encode

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.update.message.VersionResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 7:02 p.m.
 */
class VersionResponseEncoder  : MessageToByteEncoder<VersionResponseMessage>() {
    override fun encode(ctx: ChannelHandlerContext, msg: VersionResponseMessage, out: ByteBuf) {
        println("encoding $msg")
        out.writeByte(msg.opcode)
    }

}