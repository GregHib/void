package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class OpenShop(val id: String): Event {
    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "open_shop"
        else -> null
    }
}

fun Player.openShop(id: String) {
    emit(OpenShop(id))
}