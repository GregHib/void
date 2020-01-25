package org.redrune.network.message

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import mu.KotlinLogging
import org.redrune.network.Session
import org.redrune.network.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 5:27 p.m.
 */
class SimpleMessageEncoder : MessageToMessageEncoder<Message>() {

    private val logger = KotlinLogging.logger {}

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: MutableList<Any>) {
        val encoder =
            ctx.channel().attr(Session.SESSION_KEY).get().codec.encoder(msg::class) as? MessageEncoder<Message>
        if (encoder == null) {
            logger.info { "Unable to find encoder for [msg=$msg]" }
            return
        }
        val builder = PacketBuilder(buffer = ctx.alloc().buffer())
        encoder.encode(builder, msg)
        out.add(builder.toPacket())
    }

}