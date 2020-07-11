package rs.dusk.engine.event

import rs.dusk.engine.event.Priority.EVENT_PROCESS
import rs.dusk.engine.model.engine.TickInput
import rs.dusk.engine.model.entity.index.player.PlayerUnregistered
import rs.dusk.utility.inject

val buffer: EventBuffer by inject()

PlayerUnregistered then {
    buffer.remove(player)
}

TickInput priority EVENT_PROCESS then {
    buffer.buffered.forEach { (_, list) ->
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            iterator.next().invoke()
            iterator.remove()
        }
    }
}