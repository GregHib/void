package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
data class VariableSet(
    val key: String,
    val from: Any?,
    val to: Any?
) : Event

fun variableClear(key: String, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(key, this.key) && to == null }, block = block)
}

fun variableSet(variables: Set<String>, block: suspend VariableSet.(Player) -> Unit) {
    for (variable in variables) {
        on<VariableSet>({ wildcardEquals(variable, key) }, block = block)
    }
}

fun variableSet(id: String, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) }) { player ->
        block.invoke(this, player)
    }
}

fun variableSet(id: String, to: Any, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && to == this.to && from != to }) { player ->
        block.invoke(this, player)
    }
}

fun variableUnset(id: String, from: Any, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && from == this.from && to != from }) { player ->
        block.invoke(this, player)
    }
}

fun specialAttack(id: String, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ key == "special_attack" && to == true && from != true && wildcardEquals(id, it["weapon", Item.EMPTY].id) }) { player ->
        block.invoke(this, player)
    }
}

fun variableSet(id: String, from: Any?, to: Any?, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && from == this.from && to == this.to }) { player ->
        block.invoke(this, player)
    }
}

fun npcVariableSet(id: String, block: suspend VariableSet.(NPC) -> Unit) {
    onNPC<VariableSet>({ wildcardEquals(id, key) }) { npc ->
        block.invoke(this, npc)
    }
}

fun npcVariableSet(id: String, to: Any?, block: suspend VariableSet.(NPC) -> Unit) {
    onNPC<VariableSet>({ wildcardEquals(id, key) && to == this.to }) { npc ->
        block.invoke(this, npc)
    }
}

fun npcVariableSet(id: String, from: Any?, to: Any?, block: suspend VariableSet.(NPC) -> Unit) {
    onNPC<VariableSet>({ wildcardEquals(id, key) && from == this.from && to == this.to }) { npc ->
        block.invoke(this, npc)
    }
}