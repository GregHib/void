package org.redrune.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import com.google.common.primitives.Ints
import io.netty.channel.ChannelHandlerContext
import org.redrune.cache.CacheDelegate
import org.redrune.network.rs.codec.update.UpdateMessageHandler
import org.redrune.network.rs.codec.update.decode.message.UpdateRequestMessage
import org.redrune.network.rs.codec.update.encode.message.UpdateResponseMessage
import org.redrune.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRequestMessageHandler : UpdateMessageHandler<UpdateRequestMessage>() {

    private val logger = InlineLogger()
    private val cache by inject<CacheDelegate>()

    override fun handle(ctx: ChannelHandlerContext, msg: UpdateRequestMessage) {
        val (indexId, archiveId, priority) = msg
        val data = cache.getArchive(indexId, archiveId) ?: return logger.warn { "Request $this was invalid - did not exist in cache" }
        val compression: Int = data[0].toInt() and 0xff
        val length = Ints.fromBytes(data[1], data[2], data[3], data[4])
        var attributes = compression
        if (!priority) {
            attributes = attributes or 0x80
        }

        ctx.pipeline().writeAndFlush(UpdateResponseMessage(indexId, archiveId, data, compression, length, attributes))
    }

}