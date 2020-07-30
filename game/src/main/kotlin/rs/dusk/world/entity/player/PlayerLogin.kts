import rs.dusk.engine.event.Priority
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.TickInput
import rs.dusk.engine.model.entity.character.player.login.Login
import rs.dusk.engine.model.entity.character.player.login.LoginQueue
import rs.dusk.utility.inject

val loginQueue: LoginQueue by inject()

TickInput priority Priority.LOGIN_QUEUE then {
    loginQueue.tick()
}

Login then {
    loginQueue.add(this)
}