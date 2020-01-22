package org.redrune.network.session

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.AttributeKey
import mu.KotlinLogging
import org.redrune.network.codec.message.Message
import org.redrune.network.packet.Packet
import org.redrune.tools.constants.NetworkConstants
import org.redrune.tools.crypto.IsaacRandomPair
import java.net.InetSocketAddress

/**
 * This class represents the network session from the client (player) to the server
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
abstract class Session(
        /**
         * The channel for the connection
         */
        var channel: Channel
) {

    /**
     * When a message of type [M] is received, this function is invoked
     */
    abstract fun messageReceived(message: Any)

    /**
     * This function is called when the session is inactive
     */
    abstract fun onInactive()

    fun getHost(): String = (channel.remoteAddress() as? InetSocketAddress)?.address?.hostAddress
            ?: NetworkConstants.LOCALHOST

    companion object {

        /**
         * The attribute that contains the key for a session.
         */
        val SESSION_KEY: AttributeKey<Session> = AttributeKey.valueOf<Session>("session.key")
    }

}