package rs.dusk.core.network.connection

import io.netty.channel.Channel
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 07, 2020
 */
abstract class ConnectionFactory {
	
	/**
	 * The group of [channel][Channel]s connected
	 */
	val channels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
	
}