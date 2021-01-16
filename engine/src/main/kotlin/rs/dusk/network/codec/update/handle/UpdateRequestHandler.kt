package rs.dusk.network.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import com.google.common.primitives.Ints
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.Cache
import rs.dusk.network.codec.Handler
import rs.dusk.network.codec.update.encode.UpdateResponseEncoder
import rs.dusk.utility.inject

class UpdateRequestHandler : Handler() {

    private val logger = InlineLogger()
    private val cache: Cache by inject()
    private val responseEncoder = UpdateResponseEncoder()

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