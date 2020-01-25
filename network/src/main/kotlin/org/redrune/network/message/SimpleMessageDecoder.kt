package org.redrune.network.message

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import mu.KotlinLogging
import org.redrune.network.codec.CodecRepository
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 5:46 p.m.
 */
class SimpleMessageDecoder(private val codec: CodecRepository) : MessageToMessageDecoder<Packet>() {

    private val logger = KotlinLogging.logger {}

    @Suppress("UNCHECKED_CAST")
    override fun decode(ctx: ChannelHandlerContext, msg: Packet, out: MutableList<Any>) {
        val decoder = codec.decoder(msg.opcode) as? MessageDecoder<Message>

        if (decoder == null) {
            logger.info { "Unable to find decoder for [packet=$msg]" }
            return
        }
        out.add(decoder.decode(PacketReader(msg), ctx))
    }

}
