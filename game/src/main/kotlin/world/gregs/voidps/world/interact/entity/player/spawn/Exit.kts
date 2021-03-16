package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

InterfaceOption where { name == player.gameFrame.name && component == "logout" && option == "Exit" } then {
    player.open("logout")
}

InterfaceOption where { name == "logout" && (component == "lobby" || component == "login") && option == "*" } then {
    player.logout(true)
}