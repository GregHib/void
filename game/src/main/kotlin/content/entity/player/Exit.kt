package content.entity.player

import content.entity.combat.underAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.AccountManager

class Exit(val accounts: AccountManager) : Script {

    init {
        interfaceOption("Exit", "toplevel*:logout") {
            open("logout")
        }

        interfaceOption(id = "logout:*") {
            if (underAttack) {
                message("You can't log out until 8 seconds after the end of combat.")
                return@interfaceOption
            }
            accounts.logout(this, true)
        }
    }
}
