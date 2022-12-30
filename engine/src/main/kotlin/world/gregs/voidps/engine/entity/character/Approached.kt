package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.event.SuspendableEvent

data class Approached(val target: InteractiveEntity, val option: String, val partial: Boolean) : SuspendableEvent()