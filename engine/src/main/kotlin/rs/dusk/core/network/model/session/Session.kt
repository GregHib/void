package rs.dusk.core.network.model.session

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelPipeline
import io.netty.util.AttributeKey
import rs.dusk.core.utility.replace
import java.net.InetSocketAddress

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
abstract class Session(private val channel : Channel) {
	
	/**
	 * The ip address of the channel that connected to the server
	 */
	fun getIp() : String {
		return (channel.localAddress() as? InetSocketAddress)?.address?.hostAddress ?: "127.0.0.1"
	}
	
	/**
	 * The ip address of the server we are connected to
	 */
	fun getDestinationIp() : String {
		return (channel.remoteAddress() as? InetSocketAddress)?.address?.hostAddress ?: "127.0.0.1"
	}
	
	/**
	 * Sending of a message via [Channel.writeAndFlush]
	 */
	fun send(msg : Any) : ChannelFuture? {
		return channel.writeAndFlush(msg)
	}
	
	/**
	 * Disconnects the channel via [Channel.disconnect]
	 * @return ChannelFuture?
	 */
	fun disconnect() : ChannelFuture? {
		return channel.disconnect()
	}
	
	/**
	 * Replacing a handler in the [ChannelPipeline].
	 */
	fun replaceHandler(name : String, handler : ChannelHandler) : ChannelPipeline? {
		val pipeline = channel.pipeline()
		pipeline.replace(name, handler)
		return pipeline
	}
	
	companion object {
		/**
		 * The attribute in the [Channel] that identifies the session
		 */
		val SESSION_KEY : AttributeKey<Session> = AttributeKey.valueOf("session.key")
	}
}

/**
 * Gets the object in the [Session.SESSION_KEY] attribute
 * @receiver Channel
 * @return Session
 */
fun Channel.getSession() : Session {
	return attr(Session.SESSION_KEY).get()
}

/**
 * Sets the [Session.SESSION_KEY] attribute
 */
fun Channel.setSession(session : Session) {
	attr(Session.SESSION_KEY).set(session)
}