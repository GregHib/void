package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.world.interact.entity.combat.inCombat

interfaceOption("Exit", "logout", "toplevel*") {
    player.open("logout")
}

interfaceOption(id = "logout") {
    if (player.inCombat) {
        player.message("You can't log out until 8 seconds after the end of combat.")
        return@interfaceOption
    }
    player.logout(true)
}