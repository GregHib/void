package content.entity.player

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.inject
import content.entity.combat.inCombat

interfaceOption("Exit", "logout", "toplevel") {
    player.open("logout")
}

interfaceOption("Exit", "logout", "toplevel_full") {
    player.open("logout")
}

val accounts: AccountManager by inject()

interfaceOption(id = "logout") {
    if (player.inCombat) {
        player.message("You can't log out until 8 seconds after the end of combat.")
        return@interfaceOption
    }
    accounts.logout(player, true)
}