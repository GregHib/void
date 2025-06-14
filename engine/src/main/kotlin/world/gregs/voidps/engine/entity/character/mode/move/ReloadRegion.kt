package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

object ReloadRegion : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = if (index == 0) "reload_region" else null
}
