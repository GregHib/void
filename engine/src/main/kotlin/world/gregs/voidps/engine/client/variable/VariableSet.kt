package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
data class VariableSet(
    val key: String,
    val from: Any?,
    val to: Any?
) : Event {
    override val notification = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_set_variable"
        1 -> key
        2 -> dispatcher.identifier
        3 -> from
        4 -> to
        else -> null
    }

}

fun variableSet(vararg ids: String = arrayOf("*"), from: Any? = "*", to: Any? = "*", handler: suspend VariableSet.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("player_set_variable", id, "player", from, to, handler = handler)
    }
}

fun npcVariableSet(npc: String = "*", vararg variables: String = arrayOf("*"), from: Any? = "*", to: Any? = "*", handler: suspend VariableSet.(NPC) -> Unit) {
    for (variable in variables) {
        Events.handle("npc_set_variable", variable, npc, from, to, handler = handler)
    }
}