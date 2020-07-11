import rs.dusk.engine.event.Priority
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.TickInput
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.login.Login
import rs.dusk.world.entity.player.login.LoginQueue

val loginQueue: LoginQueue by inject()

TickInput priority Priority.LOGIN_QUEUE then {
    loginQueue.tick()
}

Login then {
    loginQueue.add(this)
}