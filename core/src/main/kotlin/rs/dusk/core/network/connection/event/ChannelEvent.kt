package rs.dusk.core.network.connection.event

import io.netty.channel.ChannelHandlerContext

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 02, 2020
 */
interface ChannelEvent {

    fun run(ctx: ChannelHandlerContext, cause: Throwable? = null)
}