package content.entity.player

import content.entity.combat.inCombat
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class Exit {

    val accounts: AccountManager by inject()

    init {
        interfaceOption("Exit", "logout", "toplevel*") {
            player.open("logout")
        }

        interfaceOption(id = "logout") {
            if (player.inCombat) {
                player.message("You can't log out until 8 seconds after the end of combat.")
                return@interfaceOption
            }
            accounts.logout(player, true)
        }
    }
}
