package world.gregs.void.world.interact.entity.player.spawn.logout

import org.koin.dsl.module
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.connection.DisconnectQueue
import java.util.*

@Suppress("USELESS_CAST")
val logoutModule = module {
    single { LogoutQueue() as DisconnectQueue }
}

class LogoutQueue(private val queue: LinkedList<Player> = LinkedList()) : Queue<Player> by queue, DisconnectQueue