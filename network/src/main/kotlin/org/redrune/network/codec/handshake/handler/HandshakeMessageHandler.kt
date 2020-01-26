package org.redrune.network.codec.handshake.handler

import org.redrune.network.Session
import org.redrune.network.codec.handshake.message.VersionMessage
import org.redrune.network.codec.handshake.message.HandshakeResponse
import org.redrune.network.codec.handshake.message.ResponseValue
import org.redrune.network.codec.update.UpdateCodecRepository
import org.redrune.network.codec.update.UpdateSession
import org.redrune.network.message.MessageHandler
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 5:19 p.m.
 */
class HandshakeMessageHandler : MessageHandler<VersionMessage>() {

    override fun handle(session: Session, msg: VersionMessage) {
        val response: HandshakeResponse =
            if (msg.version == NetworkConstants.CLIENT_MAJOR_BUILD)
                HandshakeResponse(ResponseValue.SUCCESSFUL)
            else
                HandshakeResponse(ResponseValue.OUT_OF_DATE)
        session.send(response)
        session.channel.attr(Session.SESSION_KEY).set(UpdateSession(session.channel, UpdateCodecRepository))
    }

}