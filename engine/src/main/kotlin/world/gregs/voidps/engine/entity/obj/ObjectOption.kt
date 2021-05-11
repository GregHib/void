package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.event.Event

data class ObjectOption(val obj: GameObject, val option: String?, val partial: Boolean) : Event