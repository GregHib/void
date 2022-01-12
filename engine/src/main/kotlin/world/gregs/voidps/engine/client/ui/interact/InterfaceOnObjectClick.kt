package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.CancellableEvent

data class InterfaceOnObjectClick(
    val gameObject: GameObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : CancellableEvent()