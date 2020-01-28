package org.redrune.network.codec.game

import io.netty.channel.Channel
import org.redrune.network.Session
import org.redrune.network.message.Message
import org.redrune.tools.crypto.IsaacRandomPair

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class GameSession(channel: Channel) : Session(channel) {

    /**
     * The cipher for incoming packets
     */
    var isaacPair: IsaacRandomPair? = null

    override fun messageReceived(msg: Message) {

    }

}