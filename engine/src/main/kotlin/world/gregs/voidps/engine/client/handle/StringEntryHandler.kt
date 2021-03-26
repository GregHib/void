package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.client.ui.dialogue.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler

class StringEntryHandler : Handler<StringEntered>() {

    override fun validate(player: Player, instruction: StringEntered) {
        player.events.emit(instruction)
    }

}