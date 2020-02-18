package org.redrune.network.model.message

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.Codec
import org.redrune.network.model.packet.Packet
import org.redrune.network.model.packet.PacketBuilder
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
// TODO: finish designing this
abstract class OutboundMessageEncoder(val codec: Codec) : MessageToByteEncoder<Message>() {

    private val logger = InlineLogger()

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        logger.info { "attempting to encode msg $msg" }
        try {
            val builder = PacketBuilder(buffer = out)
            val packet = codec.encode(msg::class, msg, builder)
            out.writeBytes(packet.payload)
            logger.info { "Encoded msg $msg and wrote it to packet $packet" }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}