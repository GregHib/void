package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.event.SuspendableEvent

class Operated(val target: InteractiveEntity) : SuspendableEvent()