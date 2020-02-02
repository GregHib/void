package org.redrune.network.session

import io.netty.channel.Channel

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
open class Session(
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

/**
 * Returns the contents of the pipeline in order from head to tail as a [List] of type [String]
 * @receiver Channel
 * @return String
 */
fun Channel.getPipelineContents(): MutableList<String>? {
    val list = mutableListOf<String>()
    val names = pipeline().names()
    names.forEach{ list.add(it) }
    return names
}

/**
 * Returns the contents of the buffer in a readable format (hexadecimal)
 */
fun ByteBuf.getHexContents() : String {
    val dump = StringBuilder()
    ByteBufUtil.appendPrettyHexDump(dump, this)
    return dump.toString()
}