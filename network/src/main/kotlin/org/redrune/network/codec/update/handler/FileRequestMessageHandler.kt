package org.redrune.network.codec.update.handler

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.cache.Cache
import org.redrune.network.codec.update.message.impl.FileRequestMessage
import org.redrune.network.model.message.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class FileRequestMessageHandler : MessageHandler<FileRequestMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: FileRequestMessage) {
        with(msg) {
            if (!Cache.valid(indexId, archiveId)) {
                logger.info { "Request $this was invalid - did not exist in cache" }
                return
            }
        }
        ctx.pipeline().writeAndFlush(msg)
    }
}