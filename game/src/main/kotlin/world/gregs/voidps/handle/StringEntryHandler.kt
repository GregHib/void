package world.gregs.voidps.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.dialogue.event.StringEntered

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class StringEntryHandler : Handler() {

    val bus: EventBus by inject()

    override fun stringEntered(player: Player, text: String) {
        sync {
            bus.emit(StringEntered(player, text))
        }
    }

}