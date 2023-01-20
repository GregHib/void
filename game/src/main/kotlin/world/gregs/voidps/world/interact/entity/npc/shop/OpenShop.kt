package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.entity.character.mode.interact.interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.suspend.delayForever

data class OpenShop(val id: String): Event

context(SuspendableEvent) suspend fun Player.openShop(id: String) {
    interact.onStop = {
        println("Stop right there")
        if (id.endsWith("general_store")) {
            GeneralStores.unbind(this, id)
        }
        close("shop")
        close("item_info")
        close("shop_side")
    }
    events.emit(OpenShop(id))
    delayForever()
}