package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.event.SuspendableEvent

data class FloorItemOption(
    val item: FloorItem,
    val option: String
) : SuspendableEvent()