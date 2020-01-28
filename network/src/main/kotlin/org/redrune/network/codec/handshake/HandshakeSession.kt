package org.redrune.network.codec.handshake

import io.netty.channel.Channel
import mu.KotlinLogging
import org.redrune.network.Session
import org.redrune.network.codec.handshake.request.HandshakeRequestMessage
import org.redrune.network.codec.handshake.request.HandshakeRequestType
import org.redrune.network.codec.handshake.response.HandshakeResponseEncoder
import org.redrune.network.codec.handshake.response.HandshakeResponseMessage
import org.redrune.network.codec.update.UpdateDecoder
import org.redrune.network.codec.update.UpdateSession
import org.redrune.network.codec.update.encode.FileRequestEncoder
import org.redrune.network.codec.update.encode.VersionResponseEncoder
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:25 a.m.
 */
class HandshakeSession(channel: Channel) : Session(channel) {

    private val logger = KotlinLogging.logger {}

    override fun messageReceived(msg: Message) {
        logger.info { "Msg $msg received" }
        if (msg is HandshakeRequestMessage) {
            val type = msg.type
            when (type) {
                HandshakeRequestType.UPDATE -> {
                    logger.info { "Received update type" }
                    pipeline.addFirst(
                        HandshakeResponseEncoder(),
                        FileRequestEncoder(),
                        VersionResponseEncoder(),
                        UpdateDecoder()
                    )
                    channel.attr(SESSION_KEY).set(UpdateSession(channel))
                    send(HandshakeResponseMessage(type, msg.type.opcode))
                }
                else -> {
                    logger.warn { "$type message is unhandled" }
                    return
                }
            }
        }
    }

}