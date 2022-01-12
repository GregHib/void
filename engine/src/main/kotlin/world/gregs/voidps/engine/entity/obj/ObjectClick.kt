package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.event.CancellableEvent

/**
 * Object click before the attempt to walk within interact distance
 */
data class ObjectClick(val obj: GameObject, val option: String?) : CancellableEvent()