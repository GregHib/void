package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.CodecRepository
import org.redrune.tools.crypto.IsaacRandomPair

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 4:07 p.m.
 */
class GamePacketDecoder(private val pair: IsaacRandomPair, private val codec: CodecRepository) : PacketDecoder() {

    override fun readOpcode(buf: ByteBuf): Int {
        return (buf.readUnsignedByte().toInt() - pair.decodingRandom.nextInt()) and 0xff
    }

    override fun getLength(ctx: ChannelHandlerContext, opcode: Int): Int? {
        return codec.decoder(opcode)?.length
    }
}