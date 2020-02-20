package org.redrune.network.codec.service.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.tools.constants.FileServerResponseCodes
import org.redrune.network.codec.update.UpdateCodec
import org.redrune.network.codec.update.encode.message.FileServerVersionMessage
import org.redrune.network.codec.service.handle.ServiceMessageHandler
import org.redrune.network.codec.service.decode.message.FileServiceHandshakeMessage
import org.redrune.network.message.codec.rs.RSMessageDecoder
import org.redrune.network.message.codec.ChannelMessageHandler
import org.redrune.network.message.codec.simple.SimpleMessageEncoder
import org.redrune.network.packet.codec.impl.SimplePacketDecoder
import org.redrune.network.replace
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServiceHandshakeMessageHandler : ServiceMessageHandler<FileServiceHandshakeMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: FileServiceHandshakeMessage) {
        val major = msg.major
        val response =
            if (major == NetworkConstants.CLIENT_MAJOR_BUILD) {
                FileServerResponseCodes.JS5_RESPONSE_OK
            } else {
                FileServerResponseCodes.JS5_RESPONSE_CONNECT_OUTOFDATE
            }

        val pipeline = ctx.pipeline()
        // TODO translate into build function
        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder(UpdateCodec))
            replace("message.decoder", RSMessageDecoder(UpdateCodec))
            replace("message.handler",
                ChannelMessageHandler(UpdateCodec)
            )
            replace("message.encoder", SimpleMessageEncoder(UpdateCodec))
        }
        pipeline.writeAndFlush(FileServerVersionMessage(response))
    }
}