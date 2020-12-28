package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.network.rs.codec.update.encode.message.UpdateRegistryResponse

class UpdateConnectionMessageHandler : MessageHandler() {

    private val logger = InlineLogger()

    override fun updateConnection(context: ChannelHandlerContext, id: Int) {
        if (id != 3) {
            context.writeAndFlush(UpdateRegistryResponse(LoginResponseCode.BadSessionId))
            logger.debug { "Invalid connection id ${context.channel().getSession().getIp()} $id" }
            return
        }
        logger.info { "Connection complete ${context.channel().getSession().getIp()}" }
    }
}