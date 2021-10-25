package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.engine.event.Event

data class ContainerUpdate(
    val container: String,
    val secondary: Boolean,
    val updates: List<ItemChanged>
) : Event