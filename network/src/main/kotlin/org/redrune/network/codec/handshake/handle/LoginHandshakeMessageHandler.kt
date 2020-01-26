package org.redrune.network.codec.handshake.handle

import org.redrune.network.Session
import org.redrune.network.codec.handshake.message.LoginHandshakeMessage
import org.redrune.network.codec.handshake.message.LoginHandshakeResponse
import org.redrune.network.codec.handshake.message.StatusCodes
import org.redrune.network.codec.login.LoginSession
import org.redrune.network.message.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 3:04 p.m.
 */
class LoginHandshakeMessageHandler : MessageHandler<LoginHandshakeMessage>() {
    override fun handle(session: Session, msg: LoginHandshakeMessage) {
        val response = LoginHandshakeResponse(StatusCodes.LOGIN_EXCHANGE_KEYS)
        println("handling msg $response, $msg")
        session.send(response)
        session.channel.attr(Session.SESSION_KEY).set(LoginSession(session.channel))
    }
}