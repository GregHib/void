package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.event.Event

data class ContainerAction(
    val container: String,
    val item: String,
    val slot: Int,
    val option: String
) : Event