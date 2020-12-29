package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.network.rs.codec.update.encode.message.UpdateRegistryResponse

class UpdateLoginStatusHandler : MessageHandler() {

    private val logger = InlineLogger()

    override fun updateLoginStatus(context: ChannelHandlerContext, online: Boolean, value: Int) {
        if (value != 0) {
            context.writeAndFlush(UpdateRegistryResponse(LoginResponseCode.BadSessionId))
            logger.debug { "Invalid login id ${context.channel()} $value" }
            return
        }

        logger.info { "Client is ${if (online) "logged in" else "logged out"} ${context.channel()}" }
    }
}