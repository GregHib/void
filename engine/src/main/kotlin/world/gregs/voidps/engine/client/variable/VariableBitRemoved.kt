package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Variable with name [key] had a sub-value [value] removed from it
 */
data class VariableBitRemoved(
    val key: String,
    val value: Any,
) : Event {
    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_remove_variable"
        1 -> key
        2 -> dispatcher.identifier
        3 -> value
        else -> null
    }
}

fun variableBitRemove(vararg ids: String = arrayOf("*"), value: Any? = "*", handler: suspend VariableBitRemoved.(Player) -> Unit) {
    for (variable in ids) {
        Events.handle("player_remove_variable", variable, "player", value, handler = handler)
    }
}

fun npcVariableBitRemove(npc: String = "*", vararg ids: String = arrayOf("*"), value: Any? = "*", handler: suspend VariableBitRemoved.(NPC) -> Unit) {
    for (variable in ids) {
        Events.handle("player_remove_variable", variable, npc, value, handler = handler)
    }
}
