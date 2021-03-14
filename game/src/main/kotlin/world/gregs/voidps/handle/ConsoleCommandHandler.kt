package world.gregs.voidps.handle

import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.command.Command

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class ConsoleCommandHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()

    override fun consoleCommand(player: Player, command: String) {
        val parts = command.split(" ")
        val prefix = parts[0]
        sync {
            bus.emit(Command(player, prefix, command.removePrefix("$prefix ")))
        }
    }

}