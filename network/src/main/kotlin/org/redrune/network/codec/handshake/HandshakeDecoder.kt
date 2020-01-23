package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable) return

        val majorBuild = buf.readInt()
        out.add(HandshakeRequest(majorBuild))
    }
}