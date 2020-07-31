package rs.dusk.world.interact.entity.player.spawn.logout

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.*
import rs.dusk.engine.tick.TickInput
import rs.dusk.network.rs.codec.game.encode.message.LogoutMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceInteraction
import rs.dusk.world.interact.entity.player.spawn.PlayerDespawn

val logoutQueue: LogoutQueue by inject()
val bus: EventBus by inject()

on(InterfaceInteraction) {
    where {
        name == player.gameFrame.name && component == "logout" && option == "Exit"
    }
    then {
        player.open("logout")
    }
}

on(InterfaceInteraction) {
    where {
        name == "logout" && (component == "lobby" || component == "login") && option == "*"
    }
    then {
        logoutQueue.add(player)
    }
}

Logout then {
    player.send(LogoutMessage(false))
}

TickInput priority Priority.LOGOUT_QUEUE then {
    var player = logoutQueue.poll()
    while (player != null) {
        disconnect(player)
        player = logoutQueue.poll()
    }
}

fun disconnect(player: Player) {
    bus.emit(Logout(player))
    bus.emit(PlayerDespawn(player))
}