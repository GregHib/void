package world.gregs.void.world.interact.entity.player.spawn.logout

import world.gregs.void.engine.action.ActionType
import world.gregs.void.engine.action.Suspension
import world.gregs.void.engine.client.ui.open
import world.gregs.void.engine.entity.Unregistered
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.event.*
import world.gregs.void.engine.tick.TickInput
import world.gregs.void.network.codec.game.encode.LogoutEncoder
import world.gregs.void.network.connection.DisconnectQueue
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.entity.player.display.InterfaceOption
import world.gregs.void.world.interact.entity.player.spawn.PlayerDespawn

val logoutQueue: DisconnectQueue by inject()
val bus: EventBus by inject()
val logoutEncode: LogoutEncoder by inject()

on(InterfaceOption) {
    where {
        name == player.gameFrame.name && component == "logout" && option == "Exit"
    }
    then {
        player.open("logout")
    }
}

on(InterfaceOption) {
    where {
        name == "logout" && (component == "lobby" || component == "login") && option == "*"
    }
    then {
        logoutQueue.add(player)
    }
}

Logout then {
    logoutEncode.encode(player)
}

TickInput priority Priority.LOGOUT_QUEUE then {
    var player = logoutQueue.poll()
    while (player != null) {
        disconnect(player)
        player = logoutQueue.poll()
    }
}

fun disconnect(player: Player) {
    player.action.run(ActionType.Logout) {
        await<Unit>(Suspension.Infinite)
    }
    bus.emit(Logout(player))
    bus.emit(Unregistered(player))
    bus.emit(PlayerDespawn(player))
}