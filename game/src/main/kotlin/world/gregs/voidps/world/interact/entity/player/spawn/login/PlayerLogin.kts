import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.priority
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.tick.TickInput
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.spawn.login.Login
import world.gregs.voidps.world.interact.entity.player.spawn.login.LoginQueue
import world.gregs.voidps.world.interact.entity.player.spawn.logout.Logout

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