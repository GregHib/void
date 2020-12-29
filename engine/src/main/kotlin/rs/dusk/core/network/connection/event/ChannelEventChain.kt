package rs.dusk.core.network.connection.event

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 02, 2020
 */
open class ChannelEventChain {
	
	private val logger = InlineLogger()
	
	/**
	 * A map of each [Event Type][ChannelEventType], which holds a list of the possible [connection events][ChannelEvent] that it may invoke
	 */
	private val events = hashMapOf<ChannelEventType, MutableList<ChannelEvent>>()
	
	/**
	 * The addition of an event to the [event type][ChannelEventType] list of events
	 */
	fun append(type : ChannelEventType, event : ChannelEvent) {
		val list = events[type] ?: mutableListOf()
		list.add(event)
		events[type] = list
	}
	
	/**
	 * This method invokes all [events][ChannelEvent] for the specified type
	 */
	fun handle(type : ChannelEventType, ctx : ChannelHandlerContext, error : Throwable? = null) {
		val connectionEvents = events[type]
		if (connectionEvents == null) {
			logger.warn { "No channel events of type [$type] were registered!" }
			return
		}
		connectionEvents.forEach { event ->
			event.run(ctx, error)
		}
	}
	
}