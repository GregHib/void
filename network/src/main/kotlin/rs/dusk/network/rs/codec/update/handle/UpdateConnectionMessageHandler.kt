package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.network.rs.codec.update.decode.message.UpdateConnectionMessage
import rs.dusk.network.rs.codec.update.encode.message.UpdateRegistryResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateConnectionMessageHandler : MessageHandler<UpdateConnectionMessage>() {

    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: UpdateConnectionMessage) {
        if (msg.value != 3) {
            ctx.writeAndFlush(UpdateRegistryResponse(LoginResponseCode.BadSessionId))
            logger.debug { "Invalid connection id ${ctx.channel().getSession().getIp()} ${msg.value}" }
            return
        }
        logger.info { "Connection complete ${ctx.channel().getSession().getIp()}" }
    }
}