package rs.dusk.core.network.connection.event.type

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.connection.Connectable
import rs.dusk.core.network.connection.event.ChannelEvent

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 07, 2020
 */
class ChannelInactiveEvent(
	private val connectable : Connectable,
	private val collection : MutableCollection<Channel>
) : ChannelEvent {
	
	private val logger = InlineLogger()
	
	override fun run(ctx : ChannelHandlerContext, cause : Throwable?) {
		connectable.onDisconnect()
		
		logger.info { "A connection has been de-registered and removed from total list!" }
		
		collection.remove(ctx.channel())
	}
}