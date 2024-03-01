package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object Registered : Event {

    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_spawn"
        1 -> dispatcher.identifier
        else -> ""
    }
}

fun playerSpawn(priority: Boolean = true, block: suspend Registered.(Player) -> Unit) {
    Events.handle("player_spawn", if (priority) "player" else "*", override = priority, handler = block)
}

fun npcSpawn(npc: String = "*", block: suspend Registered.(NPC) -> Unit) {
    Events.handle("npc_spawn", npc, handler = block)
}

fun characterSpawn(block: suspend Registered.(Character) -> Unit) {
    Events.handle("player_spawn", "*", handler = block)
    Events.handle("npc_spawn", "*", handler = block)
}

fun floorItemSpawn(item: String = "*", block: suspend Registered.(FloorItem) -> Unit) {
    Events.handle("floor_item_spawn", item, handler = block)
}

fun objectSpawn(obj: String = "*", block: suspend Registered.(GameObject) -> Unit) {
    Events.handle("object_spawn", obj, handler = block)
}

fun worldSpawn(block: suspend Registered.(World) -> Unit) {
    Events.handle("world_spawn", "world", handler = block)
}
