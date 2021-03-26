package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.network.Handler

class ConsoleCommandHandler : Handler<Command>() {

    override fun validate(player: Player, instruction: Command) {
        player.events.emit(instruction)
    }

}