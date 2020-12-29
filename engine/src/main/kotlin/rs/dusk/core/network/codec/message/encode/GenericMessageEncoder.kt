package rs.dusk.core.network.codec.message.encode

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import rs.dusk.core.network.codec.getCipherOut
import rs.dusk.core.network.codec.getCodec
import rs.dusk.core.network.codec.getSized
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.model.message.Message

/**
 * Encoder for writing messages to byte data
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@ChannelHandler.Sharable
object GenericMessageEncoder : MessageToByteEncoder<Message>() {

    private val logger = InlineLogger()

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        val codec = ctx.channel().getCodec()
            ?: throw IllegalStateException("Unable to extract codec from channel - undefined!")

        val encoder = codec.getEncoder(msg::class) as? MessageEncoder<Message>
        if (encoder == null) {
            logger.error { "Unable to find encoder! [msg=$msg, codec=${codec.javaClass.simpleName}]" }
            return
        }
		val cipher = ctx.channel().getCipherOut()
		val sized = ctx.channel().getSized()
		val builder = PacketBuilder(cipher, cipher != null || sized)
        builder.build(out) {
            encoder.encode(it, msg)
        }
        logger.debug { "Encoding successful [encoder=${encoder.javaClass.simpleName}, msg=$msg, codec=${codec.javaClass.simpleName}" }
    }

}
