package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.engine.event.Event

data class ContainerUpdate(
    val container: String,
    val updates: List<ItemChanged>
) : Event