package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.event.SuspendableEvent

class Approached(val target: Entity) : SuspendableEvent()