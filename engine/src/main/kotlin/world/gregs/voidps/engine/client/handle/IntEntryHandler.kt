package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.client.ui.dialogue.IntEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler

class IntEntryHandler : Handler<IntEntered>() {

    override fun validate(player: Player, instruction: IntEntered) {
        player.events.emit(instruction)
    }

}