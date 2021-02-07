package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.logout.LogoutQueue
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

val logoutQueue: LogoutQueue by inject()

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