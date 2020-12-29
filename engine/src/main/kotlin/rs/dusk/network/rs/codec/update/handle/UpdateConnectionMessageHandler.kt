package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.network.rs.codec.login.encode.LoginResponseEncoder

class UpdateConnectionMessageHandler(
    private val responseEncoder: LoginResponseEncoder
) : MessageHandler() {

    private val logger = InlineLogger()

    override fun updateConnection(context: ChannelHandlerContext, id: Int) {
        if (id != 3) {
            responseEncoder.encode(context.channel(), LoginResponseCode.BadSessionId.opcode)
            logger.debug { "Invalid connection id ${context.channel()} $id" }
            return
        }

        logger.info { "Connection complete ${context.channel()}" }
    }
}