package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.*

object Registered : Event

fun playerSpawn(priority: Priority = Priority.MEDIUM, block: suspend Registered.(Player) -> Unit) {
    on<Registered>(priority = priority, block = block)
}

fun npcSpawn(npc: String = "*", block: suspend (NPC) -> Unit) {
    if (npc == "*") {
        onNPC<Registered> { character ->
            block.invoke(character)
        }
    } else {
        onNPC<Registered>({ wildcardEquals(npc, it.id) }) { character ->
            block.invoke(character)
        }
    }
}

fun characterSpawn(block: suspend Registered.(Character) -> Unit) {
    onCharacter<Registered>(block = block)
}

fun floorItemSpawn(item: String = "*", block: suspend Registered.(FloorItem) -> Unit) {
    onFloorItem<Registered>({ wildcardEquals(item, it.id) }, block = block)
}

fun objectSpawn(obj: String = "*", block: suspend Registered.(GameObject) -> Unit) {
    onObject<Registered>({ wildcardEquals(obj, it.id) }, block = block)
}

fun worldSpawn(block: suspend () -> Unit) {
    onWorld<Registered> {
        block.invoke()
    }
}
