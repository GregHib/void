package org.redrune.network.codec.service.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.model.message.codec.impl.RS2MessageDecoder
import org.redrune.core.network.model.message.codec.impl.RawMessageEncoder
import org.redrune.core.network.model.packet.codec.impl.SimplePacketDecoder
import org.redrune.core.tools.utility.replace
import org.redrune.network.NetworkChannelHandler
import org.redrune.network.codec.service.decode.message.UpdateHandshakeMessage
import org.redrune.network.codec.service.handle.ServiceMessageHandler
import org.redrune.network.codec.update.UpdateCodec
import org.redrune.network.codec.update.encode.message.UpdateVersionMessage
import org.redrune.utility.constants.FileServerResponseCodes
import org.redrune.utility.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateHandshakeMessageHandler : ServiceMessageHandler<UpdateHandshakeMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: UpdateHandshakeMessage) {
        val major = msg.major
        val response =
            if (major == NetworkConstants.CLIENT_MAJOR_BUILD) {
                FileServerResponseCodes.JS5_RESPONSE_OK
            } else {
                FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
            }

        val pipeline = ctx.pipeline()
        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder(UpdateCodec))
            replace("message.decoder", RS2MessageDecoder(UpdateCodec))
            replace("message.handler", NetworkChannelHandler(UpdateCodec))
            replace("message.encoder", RawMessageEncoder(UpdateCodec))
        }
        pipeline.writeAndFlush(UpdateVersionMessage(response))
    }
}