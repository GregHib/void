package org.redrune.network.codec.login

import io.netty.channel.Channel
import org.redrune.network.Session
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 3:23 p.m.
 */
class LoginSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Message) {
    }

}