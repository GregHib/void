package org.redrune.network.codec.update.handler

import mu.KotlinLogging
import org.redrune.network.Session
import org.redrune.network.codec.update.UpdateMessageHandler
import org.redrune.network.codec.update.message.ClientStatusMessage
import org.redrune.tools.ReturnCode

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 9:03 p.m.
 */
class ClientStatusHandler : UpdateMessageHandler<ClientStatusMessage>() {

    private val logger = KotlinLogging.logger {}

    override fun handle(session: Session, msg: ClientStatusMessage) {
        val (login, value) = msg
        if (value != 0) {
            session.send(ReturnCode.BAD_SESSION_ID)
            logger.warn("Invalid login id ${session.getHost()} $value")
            return
        }

        logger.info("Client is ${if (login) "logged in" else "logged out"} ${session.getHost()}")
    }
}