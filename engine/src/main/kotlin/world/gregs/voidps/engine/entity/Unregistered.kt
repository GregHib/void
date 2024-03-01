package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object Unregistered : Event {

    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${
            when (dispatcher) {
                is NPC -> "npc"
                is FloorItem -> "floor_item"
                is GameObject -> "object"
                is Player -> "player"
                is World -> "world"
                else -> ""
            }
        }_despawn"
        1 -> when (dispatcher) {
            is NPC -> dispatcher.id
            is FloorItem -> dispatcher.id
            is GameObject -> dispatcher.id
            is Player -> "player"
            is World -> "world"
            else -> ""
        }
        else -> ""
    }
}

fun playerDespawn(block: suspend Unregistered.(Player) -> Unit) {
    Events.handle("player_despawn", "player", block = block)
}

fun npcDespawn(npc: String = "*", block: suspend Unregistered.(NPC) -> Unit) {
    Events.handle("npc_despawn", npc, block = block)
}

fun characterDespawn(block: suspend Unregistered.(Character) -> Unit) {
    Events.handle("player_despawn", "player", block = block)
    Events.handle("npc_despawn", "*", block = block)
}

fun floorItemDespawn(item: String = "*", block: suspend Unregistered.(FloorItem) -> Unit) {
    Events.handle("floor_item_despawn", item, block = block)
}

fun objectDespawn(obj: String = "*", block: suspend Unregistered.(GameObject) -> Unit) {
    Events.handle("object_despawn", obj, block = block)
}
