package rs.dusk.network.rs.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler

class UpdateDisconnectionMessageHandler : MessageHandler() {

    private val logger = InlineLogger()

    override fun updateDisconnect(context: ChannelHandlerContext, id: Int) {
        if (id != 0) {
            logger.debug { "Invalid disconnect id"  }
        }
        context.close()
    }
}