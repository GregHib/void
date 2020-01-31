package org.redrune.network.session

import io.netty.channel.Channel
import io.netty.util.AttributeKey

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
abstract class Session(
    val channel: Channel
) {

    /**
     * The channels pipeline
     */
    protected val pipeline = channel.pipeline()

    /**
     * When a message is received, it will be handled here
     * @param msg Any
     */
    abstract fun messageReceived(msg: Any)

    /**
     * Sends a message through the pipeline
     * @param msg Any
     */
    fun send(msg: Any, write: Boolean = true, flush: Boolean = true) {
        if (write) {
            channel.write(msg)
        }
        if (flush) {
            channel.flush()
        }
    }

    companion object {
        /**
         * The attribute in the [Channel] that identifies the session
         */
        val SESSION_KEY: AttributeKey<Session> = AttributeKey.valueOf<Session>("session.key")
    }
}