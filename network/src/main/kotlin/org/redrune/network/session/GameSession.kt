package org.redrune.network.session

import com.sun.jmx.remote.internal.ArrayQueue
import io.netty.channel.Channel
import org.redrune.network.codec.message.Message
import org.redrune.tools.crypto.IsaacRandomPair
import java.util.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class GameSession(channel: Channel) : Session(channel) {
    /**
     * The cipher for incoming packets
     */
    var isaacPair: IsaacRandomPair? = null

    private val messageQueue = ArrayDeque<Message>()

    override fun messageReceived(message: Any) {
        println("Received message $message")
        synchronized(messageQueue) {
            messageQueue.add(message as Message)
        }
    }

    override fun onInactive() {
    }

}