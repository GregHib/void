package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

object Unregistered : Event

fun playerDespawn(filter: Unregistered.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(Player) -> Unit) {
    on<Unregistered>(filter, priority, block)
}

fun npcDespawn(filter: Unregistered.(NPC) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(NPC) -> Unit) {
    on<Unregistered>(filter, priority, block)
}

fun characterDespawn(filter: Unregistered.(Character) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(Character) -> Unit) {
    on<Unregistered>(filter, priority, block)
}

fun floorItemDespawn(filter: Unregistered.(FloorItem) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend Unregistered.(FloorItem) -> Unit) {
    on<Unregistered>(filter, priority, block)
}
