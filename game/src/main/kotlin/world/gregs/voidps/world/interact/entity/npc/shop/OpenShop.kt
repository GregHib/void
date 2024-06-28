package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class OpenShop(val id: String): Event {
    override val size = 2

    override val notification: Boolean = true

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "open_shop"
        1 -> id
        else -> null
    }
}

fun shopOpen(shop: String = "*", handler: suspend OpenShop.(Player) -> Unit) {
    Events.handle("open_shop", shop, handler = handler)
}

fun Player.openShop(id: String) {
    emit(OpenShop(id))
}