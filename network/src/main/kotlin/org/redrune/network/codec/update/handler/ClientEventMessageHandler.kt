package org.redrune.network.codec.update.handler

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.update.message.impl.ClientEventMessage
import org.redrune.network.model.message.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class ClientEventMessageHandler : MessageHandler<ClientEventMessage>() {
    override fun handle(ctx: ChannelHandlerContext, msg: ClientEventMessage) {
        // TODO: this
    }
}