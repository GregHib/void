package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.engine.event.Event

data class ContainerUpdate(
    val containerId: Int,
    val secondary: Boolean,
    val updates: List<ItemChanged>
) : Event