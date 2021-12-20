package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class InterfaceOnNpcClick(
    val npc: NPC,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : Event {
    var cancel = false
}