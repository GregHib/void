package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.event.SuspendableEvent

class Approached(val target: InteractiveEntity) : SuspendableEvent()