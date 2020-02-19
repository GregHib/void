package org.redrune.network.codec.service.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.tools.constants.FileServerResponseCodes
import org.redrune.network.codec.file.FileCodec
import org.redrune.network.codec.file.encode.message.FileServerVersionMessage
import org.redrune.network.codec.service.handle.ServiceMessageHandler
import org.redrune.network.codec.service.decode.message.FileServiceHandshakeMessage
import org.redrune.network.message.codec.game.GameMessageDecoder
import org.redrune.network.message.codec.game.GameMessageHandler
import org.redrune.network.message.codec.simple.SimpleMessageEncoder
import org.redrune.network.packet.codec.impl.SimplePacketDecoder
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
            replace("packet.decoder", "packet.decoder", SimplePacketDecoder(FileCodec))
            replace("message.decoder", "message.decoder", GameMessageDecoder(FileCodec))
            replace("message.handler", "message.handler", GameMessageHandler(FileCodec))
            replace("message.encoder", "message.encoder", SimpleMessageEncoder(FileCodec))
        }

        pipeline.writeAndFlush(FileServerVersionMessage(response))
    }
}