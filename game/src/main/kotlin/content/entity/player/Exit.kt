package content.entity.player

import content.entity.combat.underAttack
import content.minigame.duel_arena.duel
import content.minigame.duel_arena.forfeitDuel
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
            // Duellers are offered a forfeit instead of a refusal
            if (duel?.active == true) {
                forfeitDuel()
                return@interfaceOption
            }
            if (underAttack) {
                message("You can't log out until 8 seconds after the end of combat.")
                return@interfaceOption
            }
            accounts.logout(this, true)
        }
    }
}
