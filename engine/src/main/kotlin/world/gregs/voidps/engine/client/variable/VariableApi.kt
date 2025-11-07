package world.gregs.voidps.engine.client.variable

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface VariableApi {
    /**
     * Variable with name [key] was set to [to] [from] previous value
     */
    fun variableSet(key: String = "*", handler: Player.(key: String, from: Any?, to: Any?) -> Unit) {
        Wildcards.find(key, Wildcard.Variables) { match ->
            setVar.getOrPut(match) { mutableListOf() }.add(handler)
        }
    }

    fun npcVariableSet(key: String = "*", id: String = "*", handler: NPC.(key: String, from: Any?, to: Any?) -> Unit) {
        Wildcards.find(key, Wildcard.Variables) { keyMatch ->
            Wildcards.find(id, Wildcard.Npc) { idMatch ->
                setVarNpc.getOrPut("$keyMatch:$idMatch") { mutableListOf() }.add(handler)
            }
        }
    }

    /**
     * Variable with name [key] had a sub-value [value] added to it
     */
    fun variableBitAdded(key: String, handler: Player.(value: Any) -> Unit) {
        varbitsAdded[key] = handler
    }

    /**
     * Variable with name [key] had a sub-value [value] removed from it
     */
    fun variableBitRemoved(key: String, handler: Player.(value: Any) -> Unit) {
        varbitsRemoved[key] = handler
    }


    companion object : AutoCloseable {
        private val setVar = Object2ObjectOpenHashMap<String, MutableList<(Player, String, Any?, Any?) -> Unit>>(500)
        private val setVarNpc = Object2ObjectOpenHashMap<String, MutableList<(NPC, String, Any?, Any?) -> Unit>>(2)

        private val varbitsAdded = Object2ObjectOpenHashMap<String, Player.(Any) -> Unit>(2)
        private val varbitsRemoved = Object2ObjectOpenHashMap<String, Player.(Any) -> Unit>(2)

        fun add(player: Player, key: String, value: Any) {
            varbitsAdded[key]?.invoke(player, value)
        }

        fun remove(player: Player, key: String, value: Any) {
            varbitsRemoved[key]?.invoke(player, value)
        }

        fun set(player: Player, key: String, from: Any?, to: Any?) {
            for (handler in setVar[key] ?: emptyList()) {
                handler(player, key, from, to)
            }
            for (handler in setVar["*"] ?: return) {
                handler(player, key, from, to)
            }
        }

        fun set(npc: NPC, key: String, from: Any?, to: Any?) {
            for (handler in setVarNpc["$key:${npc.id}"] ?: emptyList()) {
                handler(npc, key, from, to)
            }
            for (handler in setVarNpc["$key:*"] ?: emptyList()) {
                handler(npc, key, from, to)
            }
            for (handler in setVarNpc["*:${npc.id}"] ?: emptyList()) {
                handler(npc, key, from, to)
            }
            for (handler in setVarNpc["*:*"] ?: return) {
                handler(npc, key, from, to)
            }
        }

        override fun close() {
            setVar.clear()
            setVarNpc.clear()
        }
    }
}
