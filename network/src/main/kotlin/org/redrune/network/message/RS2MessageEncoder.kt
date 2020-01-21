package org.redrune.network.message

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import mu.KotlinLogging
import org.redrune.network.NetworkBinder
import org.redrune.network.codec.message.Message
import org.redrune.network.codec.message.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
class RS2MessageEncoder : MessageToMessageEncoder<Message>() {

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: MutableList<Any>) {
        val encoder = NetworkBinder.getEncoder(msg::class) as? MessageEncoder<Message>
        if (encoder == null) {
            logger.info("Unable to find encoder for message $msg")
        } else {
            val packet = encoder.encode(msg)
            out.add(packet)
            logger.info("Successfully encoded packet #${packet.opcode} and from message $msg")
        }
    }

    private val logger = KotlinLogging.logger {}
}