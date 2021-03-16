package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.client.ui.dialogue.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class StringEntryHandler : Handler() {

    val bus: EventBus by inject()

    override fun stringEntered(player: Player, text: String) {
        bus.emit(StringEntered(player, text))
    }

}