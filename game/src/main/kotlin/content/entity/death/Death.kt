package content.entity.death

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object Death : Event {

    override val size = 2

    override val notification = true

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_death"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun playerDeath(handler: suspend Death.(Player) -> Unit) {
    Events.handle("player_death", "player", handler = handler)
}

fun npcDeath(npc: String = "*", handler: suspend Death.(NPC) -> Unit) {
    Events.handle("npc_death", npc, handler = handler)
}

fun characterDeath(handler: suspend Death.(Character) -> Unit) {
    Events.handle("player_death", "player", handler = handler)
    Events.handle("npc_death", "*", handler = handler)
}
