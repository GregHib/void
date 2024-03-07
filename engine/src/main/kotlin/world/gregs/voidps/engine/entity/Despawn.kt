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

    override val all = true

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_despawn"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun playerDespawn(block: suspend Despawn.(Player) -> Unit) {
    Events.handle("player_despawn", "player", handler = block)
}

fun npcDespawn(npc: String = "*", block: suspend Despawn.(NPC) -> Unit) {
    Events.handle("npc_despawn", npc, handler = block)
}

fun characterDespawn(block: suspend Despawn.(Character) -> Unit) {
    Events.handle("player_despawn", "player", handler = block)
    Events.handle("npc_despawn", "*", handler = block)
}

fun floorItemDespawn(item: String = "*", block: suspend Despawn.(FloorItem) -> Unit) {
    Events.handle("floor_item_despawn", item, handler = block)
}

fun objectDespawn(obj: String = "*", block: suspend Despawn.(GameObject) -> Unit) {
    Events.handle("object_despawn", obj, handler = block)
}
