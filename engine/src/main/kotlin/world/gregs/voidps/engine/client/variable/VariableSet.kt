package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
interface VariableSet {
    fun variableSet(player: Player, key: String, from: Any?, to: Any?) {}
    fun variableSet(npc: NPC, key: String, from: Any?, to: Any?) {}

    companion object : VariableSet {
        var playerDispatcher = MapDispatcher<VariableSet>("@Variable")
        var npcDispatcher = MapDispatcher<VariableSet>("@Variable")

        override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
            playerDispatcher.forEach(key) { instance ->
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
