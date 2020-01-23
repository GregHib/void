package org.redrune.network.session

import io.netty.channel.Channel

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:28 a.m.
 */
class LoginSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Any) {
    }

}