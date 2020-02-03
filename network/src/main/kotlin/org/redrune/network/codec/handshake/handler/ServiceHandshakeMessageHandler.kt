package org.redrune.network.codec.handshake.handler

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.NetworkHandler
import org.redrune.network.codec.handshake.message.ServiceType
import org.redrune.network.codec.handshake.message.impl.ServiceHandshakeMessage
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.login.message.LoginHandshakeResponseMessage
import org.redrune.network.model.message.InboundMessageDecoder
import org.redrune.network.model.message.MessageHandler
import org.redrune.network.model.message.OutboundSimpleMessageEncoder
import org.redrune.network.model.packet.SimplePacketDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class ServiceHandshakeMessageHandler : MessageHandler<ServiceHandshakeMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: ServiceHandshakeMessage) {
        val pipeline = ctx.pipeline()

        msg.apply {
            when (type) {
                ServiceType.LOGIN -> {
                    pipeline.replace("packet.decoder", "packet.decoder", SimplePacketDecoder(LoginCodec))
                    pipeline.replace("message.decoder", "message.decoder", InboundMessageDecoder(LoginCodec))
                    pipeline.replace("network.handler", "network.handler", NetworkHandler(LoginCodec))
                    pipeline.replace("message.encode", "message.encode", OutboundSimpleMessageEncoder(LoginCodec))
                    pipeline.writeAndFlush(LoginHandshakeResponseMessage(0))
                    logger.info { "Finished and wrote response" }
                }
            }
        }
    }
}