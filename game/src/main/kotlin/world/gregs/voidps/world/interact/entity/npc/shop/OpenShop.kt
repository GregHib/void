package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.suspend.delayForever

data class OpenShop(val id: String): Event

context(SuspendableEvent) suspend fun Player.openShop(id: String) {
    events.emit(OpenShop(id))
    delayForever()
}