package org.redrune.network.codec.file.handle.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.game.handle.GameMessageHandler
import org.redrune.network.codec.file.decode.message.FileServerLoginStatusMessage
import org.redrune.network.codec.file.encode.message.FileServerRegistryResponse
import org.redrune.network.codec.file.handle.FileServerMessageHandler
import org.redrune.network.getSession
import org.redrune.tools.constants.LoginResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerLoginStatusHandler : FileServerMessageHandler<FileServerLoginStatusMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: FileServerLoginStatusMessage) {
        val (login, value) = msg
        if (value != 0) {
            ctx.writeAndFlush(FileServerRegistryResponse(LoginResponseCodes.BAD_SESSION_ID))
            logger.warn { "Invalid login id ${ctx.channel().getSession().getHost()} $value" }
            return
        }

        logger.info { "Client is ${if (login) "logged in" else "logged out"} ${ctx.channel().getSession().getHost()}" }
    }
}