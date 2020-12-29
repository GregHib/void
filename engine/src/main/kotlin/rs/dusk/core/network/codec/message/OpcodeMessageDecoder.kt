package rs.dusk.core.network.codec.message

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import rs.dusk.core.network.codec.getCodec
import rs.dusk.core.network.codec.packet.access.PacketReader

@ChannelHandler.Sharable
object OpcodeMessageDecoder : MessageToMessageDecoder<PacketReader>() {

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, msg: PacketReader, out: MutableList<Any>) {
        val codec = ctx.channel().getCodec()
            ?: throw IllegalStateException("Unable to extract codec from channel - undefined!")

        val decoder = codec.getDecoder(msg.opcode)
        if (decoder == null) {
            logger.error { "Unable to find message decoder [msg=$msg, codec=${codec.javaClass.simpleName}, codec=$codec]" }
            return
        }
        decoder.decode(ctx, msg)
        logger.debug { "Message decoded successful [decoder=${decoder.javaClass.simpleName}, codec=$codec]" }
    }
}