package world.gregs.voidps.handle

import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.dialogue.event.IntEntered

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class IntEntryHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()

    override fun integerEntered(player: Player, integer: Int) {
        sync {
            bus.emit(IntEntered(player, integer))
        }
    }

}