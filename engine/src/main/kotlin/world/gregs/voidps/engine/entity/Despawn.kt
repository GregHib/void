package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object Despawn : Event {

    override val notification: Boolean = true

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_despawn"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun playerDespawn(handler: suspend Despawn.(Player) -> Unit) {
    Events.handle("player_despawn", "player", handler = handler)
}

fun npcDespawn(npc: String = "*", handler: suspend Despawn.(NPC) -> Unit) {
    Events.handle("npc_despawn", npc, handler = handler)
}

fun characterDespawn(handler: suspend Despawn.(Character) -> Unit) {
    Events.handle("player_despawn", "player", handler = handler)
    Events.handle("npc_despawn", "*", handler = handler)
}

fun floorItemDespawn(item: String = "*", handler: suspend Despawn.(FloorItem) -> Unit) {
    Events.handle("floor_item_despawn", item, handler = handler)
}

fun objectDespawn(obj: String = "*", handler: suspend Despawn.(GameObject) -> Unit) {
    Events.handle("object_despawn", obj, handler = handler)
}

fun worldDespawn(handler: suspend Despawn.(World) -> Unit) {
    Events.handle("world_despawn", "world", handler = handler)
}
