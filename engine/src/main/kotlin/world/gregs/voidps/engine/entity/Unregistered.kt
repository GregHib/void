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
        0 -> "${dispatcher.key}_despawn"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun playerDespawn(block: suspend Unregistered.(Player) -> Unit) {
    Events.handle("player_despawn", "player", handler = block)
}

fun npcDespawn(npc: String = "*", block: suspend Unregistered.(NPC) -> Unit) {
    Events.handle("npc_despawn", npc, handler = block)
}

fun characterDespawn(block: suspend Unregistered.(Character) -> Unit) {
    Events.handle("player_despawn", "player", handler = block)
    Events.handle("npc_despawn", "*", handler = block)
}

fun floorItemDespawn(item: String = "*", block: suspend Unregistered.(FloorItem) -> Unit) {
    Events.handle("floor_item_despawn", item, handler = block)
}

fun objectDespawn(obj: String = "*", block: suspend Unregistered.(GameObject) -> Unit) {
    Events.handle("object_despawn", obj, handler = block)
}
