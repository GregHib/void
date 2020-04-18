package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.verify.ClientVerification
import rs.dusk.engine.entity.model.Player
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
val clientSessionModule = module {
    single { ClientSessions() }
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