package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.entity.combat.underAttack

interfaceOption({ id == it.gameFrame.name && component == "logout" && option == "Exit" }) { player: Player ->
    player.open("logout")
}

interfaceOption({ id == "logout" && (component == "lobby" || component == "login") && option == "*" }) { player: Player ->
    if (player.underAttack) {
        player.message("You can't log out until 8 seconds after the end of combat.")
        return@interfaceOption
    }
    player.logout(true)
}