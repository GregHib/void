package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class ConsoleCommandHandler : Handler() {

    val bus: EventBus by inject()

    override fun consoleCommand(player: Player, command: String) {
        val parts = command.split(" ")
        val prefix = parts[0]
        bus.emit(Command(player, prefix, command.removePrefix("$prefix ")))
    }

}