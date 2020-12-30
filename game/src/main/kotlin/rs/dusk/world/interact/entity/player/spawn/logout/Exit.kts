package rs.dusk.world.interact.entity.player.spawn.logout

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.Suspension
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.*
import rs.dusk.engine.tick.TickInput
import rs.dusk.network.codec.game.encode.LogoutEncoder
import rs.dusk.network.connection.DisconnectQueue
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceOption
import rs.dusk.world.interact.entity.player.spawn.PlayerDespawn

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