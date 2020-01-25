package org.redrune.network.codec.handshake.handler

import org.redrune.network.Session
import org.redrune.network.codec.handshake.UpdateMessageHandler
import org.redrune.network.codec.handshake.message.HandshakeMessage
import org.redrune.network.codec.handshake.message.HandshakeResponse
import org.redrune.network.codec.handshake.message.HandshakeResponseValue
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 5:19 p.m.
 */
class HandshakeMessageHandler : UpdateMessageHandler<HandshakeMessage>() {

    override fun handle(session: Session, msg: HandshakeMessage) {
        val response: HandshakeResponse =
            if (msg.version == NetworkConstants.CLIENT_MAJOR_BUILD) HandshakeResponse(HandshakeResponseValue.SUCCESSFUL) else
                HandshakeResponse(HandshakeResponseValue.OUT_OF_DATE)
        session.send(response)
    }

}