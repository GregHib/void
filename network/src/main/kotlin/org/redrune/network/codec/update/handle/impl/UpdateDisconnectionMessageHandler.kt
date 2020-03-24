package org.redrune.network.codec.update.handle.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.update.decode.message.UpdateDisconnectionMessage
import org.redrune.network.codec.update.handle.UpdateMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateDisconnectionMessageHandler : UpdateMessageHandler<UpdateDisconnectionMessage>() {

    private val logger = InlineLogger()
    override fun handle(ctx: ChannelHandlerContext, msg: UpdateDisconnectionMessage) {
        if (msg.value != 0) {
            logger.warn { "Invalid disconnect id"  }
        }
        ctx.close()
    }
}