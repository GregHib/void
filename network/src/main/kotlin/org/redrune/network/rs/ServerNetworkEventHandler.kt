package org.redrune.network.rs

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.connection.ConnectionEvent
import org.redrune.core.network.model.session.Session
import org.redrune.core.network.model.session.setSession

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 26, 2020
 */
@ChannelHandler.Sharable
class ServerNetworkEventHandler(private val session: Session) : ConnectionEvent {

    private val logger = InlineLogger()

    override fun onActive(ctx: ChannelHandlerContext) {
        ctx.channel().setSession(session)
        logger.info { "Session $session has just connected" }
    }

    override fun onRegistration(ctx: ChannelHandlerContext) {
        logger.info { "Registration to ${session.getDestinationIp()} succeeded" }
    }

    override fun onDeregistration(ctx: ChannelHandlerContext) {
        logger.info { "Session $session has just disconnected" }
    }

    override fun onException(ctx: ChannelHandlerContext, exception: Throwable) {
        exception.printStackTrace()
    }

    override fun onInactive(ctx: ChannelHandlerContext) {

    }

}