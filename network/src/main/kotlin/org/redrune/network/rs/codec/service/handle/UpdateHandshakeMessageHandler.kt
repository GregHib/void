package org.redrune.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import org.koin.java.KoinJavaComponent
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.encode.RawMessageEncoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.decode.SimplePacketDecoder
import org.redrune.core.tools.utility.replace
import org.redrune.network.ServerNetworkEventHandler
import org.redrune.network.rs.codec.service.ServiceMessageHandler
import org.redrune.network.rs.codec.service.decode.message.UpdateHandshakeMessage
import org.redrune.network.rs.codec.update.UpdateCodec
import org.redrune.network.rs.codec.update.encode.message.UpdateVersionMessage
import org.redrune.network.rs.session.UpdateSession
import org.redrune.utility.constants.network.FileServerResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateHandshakeMessageHandler : ServiceMessageHandler<UpdateHandshakeMessage>() {

    private val clientMajorBuild = KoinJavaComponent.getKoin().getProperty<Int>("clientBuild")

    override fun handle(ctx: ChannelHandlerContext, msg: UpdateHandshakeMessage) {
        val major = msg.major
        val response =
            if (major == clientMajorBuild) {
                FileServerResponseCodes.JS5_RESPONSE_OK
            } else {
                FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
            }

        val pipeline = ctx.pipeline()
        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder(UpdateCodec))
            replace("message.decoder", OpcodeMessageDecoder(UpdateCodec))
            replace(
                "message.handler", NetworkMessageHandler(
                    UpdateCodec,
                    ServerNetworkEventHandler(UpdateSession(channel()))
                )
            )
            replace("message.encoder", RawMessageEncoder(UpdateCodec))
        }
        pipeline.writeAndFlush(UpdateVersionMessage(response))
    }
}