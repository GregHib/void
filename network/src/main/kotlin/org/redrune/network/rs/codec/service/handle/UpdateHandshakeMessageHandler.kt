package org.redrune.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.encode.RawMessageEncoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.decode.SimplePacketDecoder
import org.redrune.core.tools.utility.replace
import org.redrune.network.rs.codec.NetworkEventHandler
import org.redrune.network.rs.codec.service.decode.message.UpdateHandshakeMessage
import org.redrune.network.rs.codec.service.ServiceMessageHandler
import org.redrune.network.rs.codec.update.UpdateCodec
import org.redrune.network.rs.codec.update.encode.message.UpdateVersionMessage
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
            replace("message.decoder", OpcodeMessageDecoder(UpdateCodec))
            replace("message.handler", NetworkMessageHandler(UpdateCodec, NetworkEventHandler()))
            replace("message.encoder", RawMessageEncoder(UpdateCodec))
        }
        pipeline.writeAndFlush(UpdateVersionMessage(response))
    }
}