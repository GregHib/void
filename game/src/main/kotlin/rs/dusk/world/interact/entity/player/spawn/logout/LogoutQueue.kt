package rs.dusk.world.interact.entity.player.spawn.logout

import org.koin.dsl.module
import rs.dusk.core.network.connection.DisconnectQueue
import rs.dusk.engine.entity.character.player.Player
import java.util.*

val logoutModule = module {
    single { LogoutQueue() as DisconnectQueue }
}

class LogoutQueue(private val queue: LinkedList<Player> = LinkedList()) : Queue<Player> by queue, DisconnectQueue