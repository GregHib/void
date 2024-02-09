package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

object Unregistered : Event

fun playerDespawn(priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(Player) -> Unit) {
    on<Unregistered>(priority = priority, block = block)
}

fun npcDespawn(priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(NPC) -> Unit) {
    on<Unregistered>(priority = priority, block = block)
}

fun characterDespawn(block: suspend Unregistered.(Character) -> Unit) {
    on<Unregistered>(block = block)
}

fun floorItemDespawn(block: suspend Unregistered.(FloorItem) -> Unit) {
    on<Unregistered>(block = block)
}
