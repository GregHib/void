package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import org.koin.java.KoinJavaComponent
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.message.handle.NetworkMessageHandler
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.tools.utility.replace
import rs.dusk.network.rs.ServerNetworkEventHandler
import rs.dusk.network.rs.codec.service.FileServerResponseCodes
import rs.dusk.network.rs.codec.service.ServiceMessageHandler
import rs.dusk.network.rs.codec.service.decode.message.UpdateHandshakeMessage
import rs.dusk.network.rs.codec.update.UpdateCodec
import rs.dusk.network.rs.codec.update.encode.message.UpdateVersionMessage
import rs.dusk.network.rs.session.UpdateSession

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
            replace("message.encoder", GenericMessageEncoder(UpdateCodec))
        }
        pipeline.writeAndFlush(UpdateVersionMessage(response))
    }
}