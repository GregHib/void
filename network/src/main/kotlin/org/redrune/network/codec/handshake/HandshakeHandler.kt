package org.redrune.network.codec.handshake

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.util.ReferenceCountUtil
import org.redrune.network.NetworkHandler
import org.redrune.network.codec.handshake.message.HandshakeRequestMessage
import org.redrune.network.codec.handshake.message.HandshakeRequestType
import org.redrune.network.codec.update.UpdateCodecRepository
import org.redrune.network.codec.update.UpdateHandler
import org.redrune.network.codec.update.decoder.UpdateDecoder
import org.redrune.network.codec.update.encoder.FileResponseEncoder
import org.redrune.network.codec.update.encoder.VersionResponseEncoder

class HandshakeHandler(codecRepository: HandshakeCodecRepository) :
    NetworkHandler<HandshakeRequestMessage>(codecRepository) {

    private val logger = InlineLogger()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HandshakeRequestMessage) {
        val pipeline = ctx.pipeline()
        when (msg.requestType) {
            HandshakeRequestType.UPDATE -> {
                pipeline.remove(HandshakeHandler::class.java)
                pipeline.addLast(
                    UpdateDecoder(),
                    UpdateHandler(UpdateCodecRepository()),
                    FileResponseEncoder(),
                    VersionResponseEncoder()
                )
                val head = ctx.pipeline().firstContext()
                val content = msg.content()
                ReferenceCountUtil.retain(content)
                head.fireChannelRead(content)
                logger.info { "DONE DIS DA"}
            }
        }
    }

}