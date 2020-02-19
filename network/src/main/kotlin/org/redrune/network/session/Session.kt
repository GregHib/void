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