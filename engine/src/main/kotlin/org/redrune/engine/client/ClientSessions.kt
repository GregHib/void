package org.redrune.engine.client

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.session.Session
import org.redrune.engine.client.verify.ClientVerification
import org.redrune.engine.entity.model.Player
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
val clientSessionModule = module {
    single { ClientSessions() as Sessions }
}

class ClientSessions : Sessions {

    private val logger = InlineLogger()
    val sessions = mutableMapOf<Session, Player>()
    val verification: ClientVerification by inject()

    override fun register(session: Session, player: Player) {
        sessions[session] = player
    }

    override fun deregister(session: Session) {
        sessions.remove(session)
    }

    override fun get(session: Session): Player? {
        return sessions[session]
    }

    override fun send(session: Session, message: Message) {
        val player = get(session) ?: return logger.debug { "Unable to find player for session $session." }
        verification.verify(player, message)
    }
}