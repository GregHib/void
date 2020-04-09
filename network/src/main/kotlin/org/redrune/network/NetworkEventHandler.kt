package org.redrune.network

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.codec.message.MessageEncoder
import org.redrune.core.network.codec.message.MessageHandler
import org.redrune.core.network.connection.ConnectionEvent
import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.session.Session
import org.redrune.core.network.model.session.setSession

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 26, 2020
 */
@ChannelHandler.Sharable
class NetworkEventHandler : ConnectionEvent {

    override fun onConnect(ctx: ChannelHandlerContext) {
        ctx.channel().setSession(Session(ctx.channel()))
    }

    override fun onDisconnect(ctx: ChannelHandlerContext) {

    }

    override fun onException(ctx: ChannelHandlerContext, exception: Throwable) {
        exception.printStackTrace()
    }

    override fun onInactive(ctx: ChannelHandlerContext) {

    }
}