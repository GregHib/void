package world.gregs.voidps.world.interact.entity.death

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object Death : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_death"
        1 -> dispatcher.identifier
        else -> null
    }

}

fun playerDeath(override: Boolean = true, block: suspend Death.(Player) -> Unit) {
    Events.handle("player_death", "player", override = override, handler = block)
}

fun npcDeath(npc: String = "*", override: Boolean = true, block: suspend Death.(NPC) -> Unit) {
    Events.handle("npc_death", npc, override = override, handler = block)
}

fun characterDeath(override: Boolean = true, block: suspend Death.(Character) -> Unit) {
    Events.handle("player_death", "player", override = override, handler = block)
    Events.handle("npc_death", "*", override = override, handler = block)
}