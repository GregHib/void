package rs.dusk.world.interact.entity.player.spawn.logout

import org.koin.dsl.module
import rs.dusk.engine.entity.character.player.Player
import java.util.*

val logoutModule = module {
    single { LogoutQueue() }
}

class LogoutQueue(private val queue: LinkedList<Player> = LinkedList()) : Queue<Player> by queue