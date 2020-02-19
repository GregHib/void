package org.redrune.network.codec.file.handle.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.file.decode.message.FileServerConnectionMessage
import org.redrune.network.codec.file.encode.message.FileServerRegistryResponse
import org.redrune.network.codec.file.handle.FileServerMessageHandler
import org.redrune.network.codec.game.handle.GameMessageHandler
import org.redrune.network.getSession
import org.redrune.tools.constants.LoginResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerConnectionMessageHandler : FileServerMessageHandler<FileServerConnectionMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: FileServerConnectionMessage) {
        if (msg.value != 3) {
            ctx.writeAndFlush(FileServerRegistryResponse(LoginResponseCodes.BAD_SESSION_ID))
            logger.warn { "Invalid connection id ${ctx.channel().getSession().getHost()} ${msg.value}" }
            return
        }
        logger.info { "Connection complete ${ctx.channel().getSession().getHost()}" }
    }
}