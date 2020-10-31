package rs.dusk.world.interact.entity.obj

import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventCompanion
import rs.dusk.utility.get

/**
 * Removes an existing map [gameObject].
 * The removal can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to removed just for one player.
 */
data class RemoveObject(
    val gameObject: GameObject,
    val ticks: Int,
    val owner: String? = null
) : Event<Unit>() {
    companion object : EventCompanion<RemoveObject>
}

fun GameObject.remove(ticks: Int = -1, owner: String? = null) {
    get<EventBus>().emit(RemoveObject(this, ticks, owner))
}

fun removeObject(
    original: GameObject,
    ticks: Int = -1,
    owner: String? = null
) = get<EventBus>().emit(RemoveObject(original, ticks, owner))