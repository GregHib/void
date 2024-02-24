package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onWorld
import world.gregs.voidps.engine.event.onFloorItem
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

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

fun floorItemSpawn(block: suspend Registered.(FloorItem) -> Unit) {
    onFloorItem<Registered>(block = block)
}

fun worldSpawn(block: suspend () -> Unit) {
    onWorld<Registered> { _: World ->
        block.invoke()
    }
}
