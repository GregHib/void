package world.gregs.void.network.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Handler
import world.gregs.void.network.codec.login.LoginResponseCode
import world.gregs.void.network.codec.login.encode.LoginResponseEncoder

class UpdateConnectionHandler(
    private val responseEncoder: LoginResponseEncoder
) : Handler() {

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