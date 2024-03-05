package world.gregs.voidps.world.interact.entity.obj.door

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

object DoorOpened : Event {

    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "door_opened"
        else -> null
    }
}