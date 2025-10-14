package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.dispatch.ListDispatcher
import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
interface VariableSet {
    fun variableSet(player: Player, key: String, from: Any?, to: Any?) {}
    fun variableSet(npc: NPC, key: String, from: Any?, to: Any?) {}

    companion object : VariableSet {
        var playerDispatcher = ListDispatcher<VariableSet>()
        var npcDispatcher = MapDispatcher<VariableSet>("Id")

        override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
            for (instance in playerDispatcher.instances) {
                instance.variableSet(player, key, from, to)
            }
        }

        override fun variableSet(npc: NPC, key: String, from: Any?, to: Any?) {
            npcDispatcher.forEach(npc.id) { instance ->
                instance.variableSet(npc, key, from, to)
            }
        }
    }
}

fun variableSet(vararg ids: String = arrayOf("*"), from: Any? = "*", to: Any? = "*", handler: suspend VariableSet.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("player_set_variable", id, "player", from, to, handler = handler)
    }
}
