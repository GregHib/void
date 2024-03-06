package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Variable with name [key] had a sub-value [value] added to it
 */
data class VariableBitAdded(
    val key: String,
    val value: Any
) : Event {

    override fun size() = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_add_variable"
        1 -> key
        2 -> dispatcher.identifier
        3 -> value
        else -> null
    }

}

fun variableBitAdd(vararg ids: String = arrayOf("*"), value: Any? = "*", override: Boolean = true, block: suspend VariableBitAdded.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("player_add_variable", id, "player", value, override = override, handler = block)
    }
}

fun npcVariableBitAdd(npc: String = "*", vararg ids: String, value: Any? = "*", override: Boolean = true, block: suspend VariableBitAdded.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("npc_add_variable", id, npc, value, override = override, handler = block)
    }
}