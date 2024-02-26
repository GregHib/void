package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.*

object Unregistered : Event

fun playerDespawn(priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(Player) -> Unit) {
    on<Unregistered>(priority = priority, block = block)
}

fun npcDespawn(npc: String = "*", priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(NPC) -> Unit) {
    onNPC<Unregistered>({ wildcardEquals(npc, it.id) }, priority = priority, block = block)
}

fun characterDespawn(block: suspend Unregistered.(Character) -> Unit) {
    onCharacter<Unregistered>(block = block)
}

fun floorItemDespawn(item: String = "*", block: suspend Unregistered.(FloorItem) -> Unit) {
    onFloorItem<Unregistered>({ wildcardEquals(item, it.id) }, block = block)
}

fun objectDespawn(obj: String = "*", block: suspend Unregistered.(GameObject) -> Unit) {
    onObject<Unregistered>({ wildcardEquals(obj, it.id) }, block = block)
}
