package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.emit

data class OpenShop(val id: String): Event

fun Player.openShop(id: String) {
    emit(OpenShop(id))
}