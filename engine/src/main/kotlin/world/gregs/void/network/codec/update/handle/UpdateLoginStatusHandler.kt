package world.gregs.void.network.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Handler
import world.gregs.void.network.codec.login.LoginResponseCode
import world.gregs.void.network.codec.login.encode.LoginResponseEncoder

class UpdateLoginStatusHandler(
    private val responseEncoder: LoginResponseEncoder
) : Handler() {

    private val logger = InlineLogger()

    override fun updateLoginStatus(context: ChannelHandlerContext, online: Boolean, value: Int) {
        if (value != 0) {
            responseEncoder.encode(context.channel(), LoginResponseCode.BadSessionId.opcode)
            logger.debug { "Invalid login id ${context.channel()} $value" }
            return
        }

        logger.info { "Client is ${if (online) "logged in" else "logged out"} ${context.channel()}" }
    }
}