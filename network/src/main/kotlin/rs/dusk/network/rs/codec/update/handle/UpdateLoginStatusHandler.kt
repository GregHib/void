package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.network.rs.codec.update.UpdateMessageHandler
import rs.dusk.network.rs.codec.update.decode.message.UpdateLoginStatusMessage
import rs.dusk.network.rs.codec.update.encode.message.UpdateRegistryResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateLoginStatusHandler : UpdateMessageHandler<UpdateLoginStatusMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: UpdateLoginStatusMessage) {
        val (login, value) = msg
        if (value != 0) {
            ctx.writeAndFlush(UpdateRegistryResponse(LoginResponseCode.BadSessionId))
            logger.warn { "Invalid login id ${ctx.channel().getSession().getIp()} $value" }
            return
        }

        logger.info { "Client is ${if (login) "logged in" else "logged out"} ${ctx.channel().getSession().getIp()}" }
    }
}