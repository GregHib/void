package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
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

@JvmName("variableSetPlayer")
fun variableSet(filter: VariableSet.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>(filter, priority, block)
}

@JvmName("variableSetNPC")
fun variableSet(filter: VariableSet.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend VariableSet.(NPC) -> Unit) {
    on<VariableSet>(filter, priority, block)
}

@JvmName("variableSetCharacter")
fun variableSet(filter: VariableSet.(Character) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend VariableSet.(Character) -> Unit) {
    on<VariableSet>(filter, priority, block)
}

fun variableSet(id: String, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) }) { player: Player ->
        block.invoke(this, player)
    }
}

fun variableSet(id: String, to: Any?, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && to == this.to }) { player: Player ->
        block.invoke(this, player)
    }
}

fun variableSet(id: String, from: Any?, to: Any?, block: suspend VariableSet.(Player) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && from == this.from && to == this.to }) { player: Player ->
        block.invoke(this, player)
    }
}

fun npcVariableSet(id: String, block: suspend VariableSet.(NPC) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) }) { npc: NPC ->
        block.invoke(this, npc)
    }
}

fun npcVariableSet(id: String, to: Any?, block: suspend VariableSet.(NPC) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && to == this.to }) { npc: NPC ->
        block.invoke(this, npc)
    }
}

fun npcVariableSet(id: String, from: Any?, to: Any?, block: suspend VariableSet.(NPC) -> Unit) {
    on<VariableSet>({ wildcardEquals(id, key) && from == this.from && to == this.to }) { npc: NPC ->
        block.invoke(this, npc)
    }
}