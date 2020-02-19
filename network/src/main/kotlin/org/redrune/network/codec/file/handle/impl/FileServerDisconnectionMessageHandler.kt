package org.redrune.network.codec.file.handle.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.file.decode.message.FileServerDisconnectionMessage
import org.redrune.network.codec.file.handle.FileServerMessageHandler
import org.redrune.network.codec.game.handle.GameMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerDisconnectionMessageHandler : FileServerMessageHandler<FileServerDisconnectionMessage>() {

    private val logger = InlineLogger()
    override fun handle(ctx: ChannelHandlerContext, msg: FileServerDisconnectionMessage) {
        if (msg.value != 0) {
            logger.warn { "Invalid disconnect id"  }
        }
        ctx.close()
    }
}