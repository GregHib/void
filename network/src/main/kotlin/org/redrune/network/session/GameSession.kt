package org.redrune.network.session

import io.netty.channel.Channel
import org.redrune.network.message.Message
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

    override fun messageReceived(msg: Any) {
        println("Received message $msg")
        synchronized(messageQueue) {
            messageQueue.add(msg as Message)
        }
    }

}