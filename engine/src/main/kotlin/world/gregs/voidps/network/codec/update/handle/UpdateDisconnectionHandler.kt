package world.gregs.voidps.network.codec.update.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Handler

class UpdateDisconnectionHandler : Handler() {

    private val logger = InlineLogger()

    override fun updateDisconnect(context: ChannelHandlerContext, id: Int) {
        if (id != 0) {
            logger.debug { "Invalid disconnect id"  }
        }
        context.close()
    }
}