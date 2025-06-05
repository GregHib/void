package content.entity.death

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class AfterDeath(
    var dropItems: Boolean = true,
    var teleport: Boolean = true,
) : Event {

    override val size = 2

    override val notification = true

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_death"
        1 -> dispatcher.identifier
        else -> null
    }

}

fun playerAfterDeath(handler: suspend AfterDeath.(Player) -> Unit) {
    Events.handle("player_after_death", "player", handler = handler)
}

fun npcAfterDeath(npc: String = "*", handler: suspend AfterDeath.(NPC) -> Unit) {
    Events.handle("npc_after_death", npc, handler = handler)
}