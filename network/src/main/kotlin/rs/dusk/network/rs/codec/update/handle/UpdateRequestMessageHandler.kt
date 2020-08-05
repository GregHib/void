package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import com.google.common.primitives.Ints
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.Cache
import rs.dusk.network.rs.codec.update.UpdateMessageHandler
import rs.dusk.network.rs.codec.update.decode.message.UpdateRequestMessage
import rs.dusk.network.rs.codec.update.encode.message.UpdateResponseMessage
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRequestMessageHandler : UpdateMessageHandler<UpdateRequestMessage>() {

    private val logger = InlineLogger()
    private val cache: Cache by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: UpdateRequestMessage) {
        val (indexId, archiveId, priority) = msg
        val data = cache.getArchive(indexId, archiveId) ?: return logger.debug { "Request $this was invalid - did not exist in cache" }
        val compression: Int = data[0].toInt() and 0xff
        val length = Ints.fromBytes(data[1], data[2], data[3], data[4])
        var attributes = compression
        if (!priority) {
            attributes = attributes or 0x80
        }

        ctx.pipeline().writeAndFlush(UpdateResponseMessage(indexId, archiveId, data, compression, length, attributes))
    }

}