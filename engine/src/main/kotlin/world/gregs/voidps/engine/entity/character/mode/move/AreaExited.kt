package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.event.SuspendableEvent

data class AreaExited(val area: String) : SuspendableEvent