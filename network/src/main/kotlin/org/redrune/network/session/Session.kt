package org.redrune.network.session

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import org.redrune.tools.constants.NetworkConstants
import java.net.InetSocketAddress

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
open class Session(
    val channel: Channel
) {

    fun getHost(): String {
        return (channel?.remoteAddress() as? InetSocketAddress)?.address?.hostAddress ?: NetworkConstants.LOCALHOST
    }

    companion object {
        /**
         * The attribute in the [Channel] that identifies the session
         */
        val SESSION_KEY: AttributeKey<Session> = AttributeKey.valueOf<Session>("session.key")
    }
}

/**
 * Gets the object in the [Session.SESSION_KEY] attribute
 * @receiver Channel
 * @return Session
 */
fun Channel.getSession(): Session {
    return attr(Session.SESSION_KEY).get()
}

/**
 * Sets the [Session.SESSION_KEY] attribute
 */
fun Channel.setSession(session: Session) {
    attr(Session.SESSION_KEY).set(session)
}
