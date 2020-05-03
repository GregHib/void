package rs.dusk.network.rs

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.connection.event.ConnectionEvent
import rs.dusk.core.network.connection.event.ConnectionEventChain
import rs.dusk.core.network.connection.event.ConnectionEventType.*
import rs.dusk.core.network.model.session.Session
import rs.dusk.core.network.model.session.setSession

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 03, 2020
 */
class ServerConnectionEventChain(private val session: Session) : ConnectionEventChain() {

    private val logger = InlineLogger()

    init {
        append(ACTIVE, object : ConnectionEvent {
            override fun run(ctx: ChannelHandlerContext, cause: Throwable?) {
                ctx.channel().setSession(session)
                logger.info { "Session $session has just connected" }
            }
        })
        append(REGISTER, object : ConnectionEvent {
            override fun run(ctx: ChannelHandlerContext, cause: Throwable?) {
                logger.info { "Registration to ${session.getDestinationIp()} succeeded" }
            }
        })
        append(DEREGISTER, object : ConnectionEvent {
            override fun run(ctx: ChannelHandlerContext, cause: Throwable?) {
                logger.info { "Session $session has just disconnected" }
            }
        })
        append(INACTIVE, object : ConnectionEvent {
            override fun run(ctx: ChannelHandlerContext, cause: Throwable?) {
                ctx.channel().setSession(session)
                logger.info { "Session $session is inactive" }
            }
        })
    }
}