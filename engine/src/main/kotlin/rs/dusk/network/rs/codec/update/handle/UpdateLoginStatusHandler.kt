package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Handler
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.network.rs.codec.login.encode.LoginResponseEncoder

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