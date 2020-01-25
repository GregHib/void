package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.CodecRepository
import org.redrune.network.packet.struct.PacketHeader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 4:14 p.m.
 */
class SimplePacketDecoder(private val codec: CodecRepository) : PacketDecoder() {

    override fun getHeader(length: Int, buf: ByteBuf): PacketHeader {
        return PacketHeader.FIXED
    }

    override fun getLength(ctx: ChannelHandlerContext, opcode: Int): Int? {
        println("Codec=$codec")
        return codec.decoder(opcode)?.length
    }
}