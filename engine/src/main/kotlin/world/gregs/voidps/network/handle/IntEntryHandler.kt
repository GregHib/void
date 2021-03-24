package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.client.ui.dialogue.IntEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class IntEntryHandler : Handler() {

    override fun integerEntered(player: Player, integer: Int) {
        player.events.emit(IntEntered(integer))
    }

}