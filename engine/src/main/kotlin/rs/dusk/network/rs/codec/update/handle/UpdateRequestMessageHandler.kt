package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import com.google.common.primitives.Ints
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.Cache
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.network.rs.codec.update.encode.UpdateResponseMessageEncoder
import rs.dusk.utility.inject

class UpdateRequestMessageHandler : MessageHandler() {

    private val logger = InlineLogger()
    private val cache: Cache by inject()
    private val responseEncoder = UpdateResponseMessageEncoder()

    override fun updateRequest(context: ChannelHandlerContext, indexId: Int, archiveId: Int, priority: Boolean) {
        val data = cache.getArchive(indexId, archiveId) ?: return logger.debug { "Request $this was invalid - does not exist in cache" }
        val compression: Int = data[0].toInt() and 0xff
        val length = Ints.fromBytes(data[1], data[2], data[3], data[4])
        var attributes = compression
        if (!priority) {
            attributes = attributes or 0x80
        }

        responseEncoder.encode(context.channel(), indexId, archiveId, data, compression, length, attributes)
    }

}