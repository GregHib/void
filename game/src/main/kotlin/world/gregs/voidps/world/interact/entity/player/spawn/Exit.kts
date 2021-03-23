package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ name == it.gameFrame.name && component == "logout" && option == "Exit" }) { player: Player ->
    player.open("logout")
}

on<InterfaceOption>({ name == "logout" && (component == "lobby" || component == "login") && option == "*" }) { player: Player ->
    player.logout(true)
}