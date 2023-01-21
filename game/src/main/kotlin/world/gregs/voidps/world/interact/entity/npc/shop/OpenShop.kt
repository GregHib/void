package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

data class OpenShop(val id: String): Event

fun Player.openShop(id: String) {
    events.emit(OpenShop(id))
}