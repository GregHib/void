package world.gregs.voidps.world.interact.entity.player.spawn.logout

import org.koin.dsl.module
import world.gregs.voidps.Main
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.network.codec.game.encode.LogoutEncoder
import world.gregs.voidps.network.connection.DisconnectQueue
import world.gregs.voidps.world.interact.entity.player.spawn.PlayerDespawn
import world.gregs.voidps.world.interact.entity.player.spawn.login.LoginQueue
import java.util.*

@Suppress("USELESS_CAST")
val logoutModule = module {
    single { LogoutQueue(get(), get(), get()) as DisconnectQueue }
}

class LogoutQueue(
    private val bus: EventBus,
    private val loginQueue: LoginQueue,
    private val logoutEncode: LogoutEncoder,
    private val queue: LinkedList<Player> = LinkedList()
) : Queue<Player> by queue, DisconnectQueue {

    override fun run() {
        var player = poll()
        while (player != null) {
            disconnect(player)
            player = poll()
        }
    }


    private fun disconnect(player: Player) {
        player.action.run(ActionType.Logout) {
            await<Unit>(Suspension.Infinite)
        }
        logoutEncode.encode(player)
        loginQueue.remove(player.name)
        bus.emit(Unregistered(player))
        bus.emit(PlayerDespawn(player))
    }
}