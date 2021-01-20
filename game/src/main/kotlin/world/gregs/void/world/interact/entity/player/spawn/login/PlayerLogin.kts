import world.gregs.void.engine.entity.character.update.visual.player.name
import world.gregs.void.engine.event.Priority
import world.gregs.void.engine.event.priority
import world.gregs.void.engine.event.then
import world.gregs.void.engine.tick.TickInput
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.entity.player.spawn.login.Login
import world.gregs.void.world.interact.entity.player.spawn.login.LoginQueue
import world.gregs.void.world.interact.entity.player.spawn.logout.Logout

val loginQueue: LoginQueue by inject()

TickInput priority Priority.LOGIN_QUEUE then {
    loginQueue.tick()
}

Login then {
    loginQueue.add(this)
}

Logout then {
    loginQueue.remove(player.name)
}