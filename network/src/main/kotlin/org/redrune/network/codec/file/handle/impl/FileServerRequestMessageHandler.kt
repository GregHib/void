package org.redrune.network.codec.file.handle.impl

import com.github.michaelbull.logging.InlineLogger
import com.google.common.primitives.Ints
import io.netty.channel.ChannelHandlerContext
import org.redrune.cache.Cache
import org.redrune.network.codec.file.decode.message.FileServerRequestMessage
import org.redrune.network.codec.file.encode.message.FileServerResponseMessage
import org.redrune.network.codec.file.handle.FileServerMessageHandler
import org.redrune.network.codec.game.handle.GameMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerRequestMessageHandler : FileServerMessageHandler<FileServerRequestMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: FileServerRequestMessage) {
        val (indexId, archiveId, priority) = msg
        if (!Cache.valid(indexId, archiveId)) {
            logger.warn { "Request $this was invalid - did not exist in cache" }
            return
        }

        val data = Cache.getFile(indexId, archiveId) ?: return
        val compression: Int = data[0].toInt() and 0xff
        val length = Ints.fromBytes(data[1], data[2], data[3], data[4])
        var attributes = compression
        if (!priority) {
            attributes = attributes or 0x80
        }

        ctx.pipeline().writeAndFlush(FileServerResponseMessage(indexId, archiveId, data, compression, length, attributes))
    }

}