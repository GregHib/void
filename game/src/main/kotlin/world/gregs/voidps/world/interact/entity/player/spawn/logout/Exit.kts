package world.gregs.voidps.world.interact.entity.player.spawn.logout

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.engine.tick.TickInput
import world.gregs.voidps.network.codec.game.encode.LogoutEncoder
import world.gregs.voidps.network.connection.DisconnectQueue
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption
import world.gregs.voidps.world.interact.entity.player.spawn.PlayerDespawn

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