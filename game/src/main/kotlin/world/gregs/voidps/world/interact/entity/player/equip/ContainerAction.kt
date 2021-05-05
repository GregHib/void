package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class ContainerAction(
    val container: String,
    val item: Item,
    val slot: Int,
    val option: String
) : Event