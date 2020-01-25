package org.redrune.network.codec.update.handler

import mu.KotlinLogging
import org.redrune.network.Session
import org.redrune.network.codec.handshake.UpdateMessageHandler
import org.redrune.network.codec.update.message.ClientConnectionMessage
import org.redrune.network.codec.update.message.ClientResponseMessage
import org.redrune.tools.ReturnCode


/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:49 p.m.
 */
class ClientConnectionHandler : UpdateMessageHandler<ClientConnectionMessage>() {

    private val logger = KotlinLogging.logger {}
    override fun handle(session: Session, msg: ClientConnectionMessage) {
        if (msg.connectionId != 3) {
            session.send(ClientResponseMessage(ReturnCode.BAD_SESSION_ID))
            logger.warn("Invalid connection id ${session.getHost()} ${msg.connectionId}")
            return
        }

        logger.info("Connection complete ${session.getHost()}")
    }

}