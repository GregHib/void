package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.codec.service.FileServerResponseCodes
import rs.dusk.network.rs.codec.update.UpdateCodec
import rs.dusk.network.rs.codec.update.encode.message.UpdateVersionMessage
import rs.dusk.utility.getIntProperty
import rs.dusk.utility.inject

class UpdateHandshakeMessageHandler : MessageHandler() {

    private val clientMajorBuild: Int = getIntProperty("clientBuild")
    private val update: UpdateCodec by inject()

    override fun updateHandshake(context: ChannelHandlerContext, version: Int) {
        val response = if (version == clientMajorBuild) {
            FileServerResponseCodes.JS5_RESPONSE_OK
        } else {
            FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
        }

        val channel = context.channel()
        val pipeline = context.pipeline()

        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder())
            replace("message.decoder", OpcodeMessageDecoder)
            replace("message.encoder", GenericMessageEncoder)

            channel.setCodec(update)
        }
        pipeline.writeAndFlush(UpdateVersionMessage(response))
    }
}