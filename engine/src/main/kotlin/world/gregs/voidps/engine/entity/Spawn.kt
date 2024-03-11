package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object Spawn : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_spawn"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun playerSpawn(priority: Boolean = true, handler: suspend Spawn.(Player) -> Unit) {
    Events.handle("player_spawn", if (priority) "player" else "*", handler = handler)
}

fun npcSpawn(npc: String = "*", handler: suspend Spawn.(NPC) -> Unit) {
    Events.handle("npc_spawn", npc, handler = handler)
}

fun characterSpawn(handler: suspend Spawn.(Character) -> Unit) {
    Events.handle("player_spawn", "*", handler = handler)
    Events.handle("npc_spawn", "*", handler = handler)
}

fun floorItemSpawn(item: String = "*", handler: suspend Spawn.(FloorItem) -> Unit) {
    Events.handle("floor_item_spawn", item, handler = handler)
}

fun objectSpawn(obj: String = "*", handler: suspend Spawn.(GameObject) -> Unit) {
    Events.handle("object_spawn", obj, handler = handler)
}

fun worldSpawn(handler: suspend Spawn.(World) -> Unit) {
    Events.handle("world_spawn", "world", handler = handler)
}
