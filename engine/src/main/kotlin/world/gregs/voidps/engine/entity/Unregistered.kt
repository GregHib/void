package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onFloorItem
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC

object Unregistered : Event

fun playerDespawn(priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(Player) -> Unit) {
    on<Unregistered>(priority = priority, block = block)
}

fun npcDespawn(priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(NPC) -> Unit) {
    onNPC<Unregistered>(priority = priority, block = block)
}

fun characterDespawn(block: suspend Unregistered.(Character) -> Unit) {
    onCharacter<Unregistered>(block = block)
}

fun floorItemDespawn(block: suspend Unregistered.(FloorItem) -> Unit) {
    onFloorItem<Unregistered>(block = block)
}
