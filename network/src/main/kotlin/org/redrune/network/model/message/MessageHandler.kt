package org.redrune.network.model.message

import io.netty.channel.ChannelHandlerContext

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
abstract class MessageHandler<T : Message> {

    /**
     * This method handles what is done when the [Message] of type [T] is read
     */
    abstract fun handle(ctx: ChannelHandlerContext, msg: T)
}