package rs.dusk.world.entity.player.login

import rs.dusk.engine.event.Priority
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import kotlin.coroutines.resume

val list: LoginList by inject()
val loginPerTickCap: Int = getProperty("loginPerTickCap", 1)

Tick priority Priority.LOGIN_QUEUE then {
    var count = 0
    var next = list.poll()
    while (next != null) {
        next.resume(Unit)
        if (count++ >= loginPerTickCap) {
            break
        }
        next = list.poll()
    }
}