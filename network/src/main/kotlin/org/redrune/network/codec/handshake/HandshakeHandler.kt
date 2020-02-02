package org.redrune.network.codec.handshake

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.util.ReferenceCountUtil
import org.redrune.network.NetworkHandler
import org.redrune.network.codec.update.UpdateHandler
import org.redrune.network.codec.update.decoder.UpdateDecoder
import org.redrune.network.codec.update.encoder.FileResponseEncoder
import org.redrune.network.codec.update.encoder.VersionResponseEncoder

class HandshakeHandler : NetworkHandler<HandshakeRequestMessage>() {

    private val logger = InlineLogger()

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info { "Channel connected: " + ctx.channel().remoteAddress() + "." }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HandshakeRequestMessage) {
        val pipeline = ctx.pipeline()
        when (msg.requestType) {
            HandshakeRequestType.UPDATE -> {
                pipeline.remove(HandshakeHandler::class.java)
                pipeline.addLast(
                    UpdateDecoder(),
                    UpdateHandler(), FileResponseEncoder(), VersionResponseEncoder())
                val head = ctx.pipeline().firstContext()
                val content = msg.content()
                ReferenceCountUtil.retain(content)
                head.fireChannelRead(content)
            }
        }
    }

}