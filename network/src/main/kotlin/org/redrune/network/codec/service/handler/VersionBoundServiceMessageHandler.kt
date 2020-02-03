package org.redrune.network.codec.service.handler

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.NetworkHandler
import org.redrune.network.codec.service.message.ServiceType
import org.redrune.network.codec.service.message.impl.VersionBoundServiceMessage
import org.redrune.network.codec.update.UpdateCodec
import org.redrune.network.codec.update.message.UpdateResponseCode
import org.redrune.network.codec.update.message.impl.VersionResponseMessage
import org.redrune.network.model.message.InboundMessageDecoder
import org.redrune.network.model.message.MessageHandler
import org.redrune.network.model.message.OutboundSimpleMessageEncoder
import org.redrune.network.model.packet.SimplePacketDecoder
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class VersionBoundServiceMessageHandler : MessageHandler<VersionBoundServiceMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: VersionBoundServiceMessage) {
        val pipeline = ctx.pipeline()
        msg.apply {
            val js5Response =
                if (majorBuild == NetworkConstants.CLIENT_MAJOR_BUILD) {
                    UpdateResponseCode.JS5_RESPONSE_OK
                } else {
                    UpdateResponseCode.JS5_RESPONSE_CONNECT_OUTOFDATE
                }
            when (type) {
                ServiceType.UPDATE -> {
                    pipeline.replace("packet.decoder", "packet.decoder", SimplePacketDecoder(UpdateCodec))
                    pipeline.replace("message.decoder", "message.decoder", InboundMessageDecoder(UpdateCodec))
                    pipeline.replace("network.handler", "network.handler", NetworkHandler(UpdateCodec))
                    pipeline.replace("message.encode", "message.encode", OutboundSimpleMessageEncoder(UpdateCodec))
                    pipeline.writeAndFlush(VersionResponseMessage(js5Response))
                    logger.info { "Finished and wrote response" }
                }
            }
        }
    }
}