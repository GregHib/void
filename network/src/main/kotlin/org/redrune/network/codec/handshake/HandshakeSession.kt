package org.redrune.network.codec.handshake

import io.netty.channel.Channel
import org.redrune.network.Session
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:25 a.m.
 */
class HandshakeSession(channel: Channel) : Session(channel) {

    override fun messageReceived(msg: Any) {
        when (msg) {
            is HandshakeMessage -> {
                if (msg.version == NetworkConstants.CLIENT_MAJOR_BUILD) {
                   send(HandshakeResponse(HandshakeResponseValue.SUCCESSFUL))
                } else {
                   send(HandshakeResponse(HandshakeResponseValue.OUT_OF_DATE))
                }
            }
        }
    }


}