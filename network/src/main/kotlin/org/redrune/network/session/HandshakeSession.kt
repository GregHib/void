package org.redrune.network.session

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import org.redrune.network.codec.file.UpdateDecoder
import org.redrune.network.codec.handshake.HandshakeRequestMessage
import org.redrune.network.codec.handshake.HandshakeRequestType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class HandshakeSession(channel: Channel) : Session(channel) {

    private val logger = InlineLogger()

    override fun messageReceived(msg: Any) {
        when (msg) {
            is HandshakeRequestMessage -> {
                when (msg.requestType) {
                    HandshakeRequestType.UPDATE -> {
                        logger.info { "DEFINED THIS" }
                        pipeline.addBefore("network.handler", "update.decoder", UpdateDecoder())
                        val head = pipeline.firstContext()
                        logger.info { "head=$head" }
//                        head.write(PacketReader(msg.bufHolder.content()))

                        val bufHolder = msg.bufHolder
                        val content = bufHolder.content()

//                        head.writeAndFlush(content).addListener(ChannelFutureListener.CLOSE)
                    }
                }

            }
        }
        logger.info { "Message $msg received" }
    }
}
