package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class ConsoleCommandHandler : Handler() {

    override fun consoleCommand(player: Player, command: String) {
        val parts = command.split(" ")
        val prefix = parts[0]
        player.events.emit(Command(prefix, command.removePrefix("$prefix ")))
    }

}