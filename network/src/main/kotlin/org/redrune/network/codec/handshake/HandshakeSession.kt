package org.redrune.network.codec.handshake

import io.netty.channel.Channel
import mu.KotlinLogging
import org.redrune.network.Session
import org.redrune.network.codec.CodecRepository
import org.redrune.network.message.Message
import org.redrune.network.message.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:25 a.m.
 */
class HandshakeSession(channel: Channel, codec: CodecRepository) : Session(channel, codec) {

    private val logger = KotlinLogging.logger {}

    @Suppress("UNCHECKED_CAST")
    override fun messageReceived(msg: Message) {
        val handler = codec.handler(msg::class) as? MessageHandler<Message>
            ?: run {
                logger.warn("No handler for message: $msg")
                return
            }

        handler.handle(this, msg)
    }


}