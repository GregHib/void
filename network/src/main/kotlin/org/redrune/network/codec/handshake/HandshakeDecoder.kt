package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.update.UpdateDecoder
import org.redrune.network.codec.update.encode.FileRequestEncoder
import org.redrune.network.codec.update.encode.VersionResponseEncoder
import kotlin.experimental.and

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 3:57 p.m.
 */
class HandshakeDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        if (!msg.isReadable) {
            return;
        }

        val opcode = (msg.readByte() and 0xFF.toByte()).toInt()
        val pipeline = ctx.pipeline()
        pipeline.remove(HandshakeDecoder::class.java)

        println("opcode=$opcode")
        when (opcode) {
            HandshakeOpcodes.SERVICE_UPDATE -> {
                pipeline.addFirst(FileRequestEncoder(), VersionResponseEncoder(), UpdateDecoder())
            }
            HandshakeOpcodes.SERVICE_LOGIN -> {

            }
        }
    }


}