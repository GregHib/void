package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == it.gameFrame.name && component == "logout" && option == "Exit" }) { player: Player ->
    player.open("logout")
}

on<InterfaceOption>({ id == "logout" && (component == "lobby" || component == "login") && option == "*" }) { player: Player ->
    if (player.clocks.contains("in_combat")) {
        player.message("You can't log out until 8 seconds after the end of combat.")
        return@on
    }
    player.logout(true)
}