package org.redrune.network.message

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import org.redrune.network.codec.CodecRegistry
import org.slf4j.LoggerFactory

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
class GameMessageEncoder : MessageToMessageEncoder<Message>() {

    /**
     * The logger for this class
     */
    private val logger = LoggerFactory.getLogger(GameMessageEncoder::class.java)

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: MutableList<Any>) {
        println("message being encoded into packet! $msg")

        val encoder = CodecRegistry.getEncoder(msg::class) as? MessageEncoder<Message>
        if (encoder == null) {
            logger.info("Unable to find encoder for message $msg")
        } else {
            val packet = encoder.encode(msg)
            out.add(packet)
            logger.info("Successfully encoded packet #${packet.opcode} and from message $msg")
        }
    }
}