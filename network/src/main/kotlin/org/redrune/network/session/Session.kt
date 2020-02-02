package org.redrune.network.session

import io.netty.channel.Channel
import io.netty.util.AttributeKey

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
open class Session(
    val channel: Channel
) {

    companion object {
        /**
         * The attribute in the [Channel] that identifies the session
         */
        val SESSION_KEY: AttributeKey<Session> = AttributeKey.valueOf<Session>("session.key")
    }
}