package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.type.Area

data class AreaEntered(val name: String, val area: Area) : SuspendableEvent