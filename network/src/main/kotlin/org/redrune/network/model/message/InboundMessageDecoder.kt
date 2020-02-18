package org.redrune.network.model.message

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.redrune.network.codec.Codec
import org.redrune.network.model.packet.PacketReader
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class InboundMessageDecoder(private val codec: Codec) : MessageToMessageDecoder<PacketReader>() {

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, reader: PacketReader, out: MutableList<Any>) {
        try {
            val message = codec.decode(reader)
            out.add(message)
            logger.info { "Decoded [reader=$reader]  into [message=$message]" }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}