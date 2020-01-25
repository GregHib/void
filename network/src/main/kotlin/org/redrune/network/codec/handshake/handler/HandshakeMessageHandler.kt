package org.redrune.network.codec.handshake.handler

import org.redrune.network.Session
import org.redrune.network.codec.handshake.message.HandshakeMessage
import org.redrune.network.codec.handshake.message.HandshakeResponse
import org.redrune.network.codec.handshake.message.ResponseValue
import org.redrune.network.codec.update.UpdateMessageHandler
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 5:19 p.m.
 */
class HandshakeMessageHandler : UpdateMessageHandler<HandshakeMessage>() {

    override fun handle(session: Session, msg: HandshakeMessage) {
        val response: HandshakeResponse =
            if (msg.version == NetworkConstants.CLIENT_MAJOR_BUILD)
                HandshakeResponse(ResponseValue.SUCCESSFUL)
            else
                HandshakeResponse(ResponseValue.OUT_OF_DATE)
        session.send(response)
    }

}