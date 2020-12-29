package rs.dusk.core.network.connection

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.AttributeKey

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class Session(private val channel : Channel) {
	
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