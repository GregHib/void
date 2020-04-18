package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.verify.ClientVerification
import rs.dusk.engine.entity.event.player.PlayerUpdate
import rs.dusk.engine.entity.model.Player
import rs.dusk.utility.get
import rs.dusk.utility.inject
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */

@Suppress("USELESS_CAST")
val clientSessionModule = module {
    single { ClientSessions() as Sessions }
}

class ClientSessions : Sessions() {

    private val logger = InlineLogger()
    val players = mutableMapOf<Session, Player>()
    val sessions = mutableMapOf<Player, Session>()
    val verification: ClientVerification by inject()

    override fun register(session: Session, player: Player) {
        players[session] = player
        sessions[player] = session
    }

    override fun deregister(session: Session) {
        val player = players.remove(session)
        sessions.remove(player)
    }

    override fun get(session: Session): Player? {
        return players[session]
    }

    override fun get(player: Player): Session? {
        return sessions[player]
    }

    override fun <T : PlayerUpdate> send(player: Player, clazz: KClass<T>, message: T) {
        val session = get(player) ?: return logger.debug { "Unable to find session for player $player." }
        session.send(message)
    }

    override fun <T : Message> send(session: Session, clazz: KClass<T>, message: T) {
        val player = get(session) ?: return logger.debug { "Unable to find player for session $session." }
        verification.verify(player, clazz, message)
    }
}

inline fun <reified T : PlayerUpdate> Player.send(update: T) = get<Sessions>().send(this, T::class, update)