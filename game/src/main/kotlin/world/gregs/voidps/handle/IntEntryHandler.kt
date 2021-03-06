package world.gregs.voidps.handle

import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.dialogue.event.IntEntered

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class IntEntryHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()

    override fun integerEntered(session: ClientSession, integer: Int) {
        val player = sessions.get(session) ?: return
        sync {
            bus.emit(IntEntered(player, integer))
        }
    }

}