package org.redrune.network.codec.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.cache.Cache
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import java.util.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class FileRequestDecoder : ByteToMessageDecoder() {

    /**
     * The list of requests
     */
    private val requests = LinkedList<FileRequest>()


    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val session: NetworkSession? = ctx.channel().attr(NetworkConstants.SESSION_KEY).get()
        session?.state = NetworkSession.SessionState.HANDSHAKE
        println(buf.readableBytes())
        while (buf.readableBytes() >= 4) {
            val priority = buf.readUnsignedByte().toInt()
            handleRequest(ctx, buf, priority)
        }
    }

    /**
     * Decodes a file request and serves it to the client
     */
    private fun handleRequest(ctx: ChannelHandlerContext, buf: ByteBuf, priority: Int) {
        val indexId: Int = buf.readUnsignedByte().toInt()
        val archiveId = buf.readUnsignedShort()
        if (indexId != 255) {
            if (Cache.indexes.lastIndex <= indexId || Cache.indexes[indexId] == null || !Cache.indexes[indexId].archiveExists(
                    archiveId
                )
            ) {
                return
            }
        } else if (archiveId != 255) {
            if (Cache.indexes.lastIndex <= archiveId || Cache.indexes[archiveId] == null) {
                return
            }
        }
        when (priority) {
            0 -> {
                requests.add(FileRequest(indexId, archiveId, false))
            }
            1 -> {
                ctx.writeAndFlush(Cache.getArchive(indexId, archiveId, true))
            }
            2, 3 -> {
                requests.clear()
            }
        }
        while (requests.size > 0) {
            val request = requests.removeFirst()
            ctx.writeAndFlush(Cache.getArchive(request.indexId, request.archiveId, request.priority))
        }
    }

}