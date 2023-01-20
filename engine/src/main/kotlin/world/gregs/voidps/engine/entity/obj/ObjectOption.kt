package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.event.SuspendableEvent

data class ObjectOption(val obj: GameObject, val def: ObjectDefinition, val option: String) : SuspendableEvent()