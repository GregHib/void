package content.minigame.great_orb_project

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class GreatOrbProject : Script {
    init {
        exited("runecrafting_guild") {
            // TODO check target isn't part of the minigame
            close("rc_guild_overlay")
        }
    }

    companion object {
        fun join(player: Player, team: String) {
            player["gop_team"] = team
            player.open("rc_guild_overlay")
            player.interfaces.sendVisibility("rc_guild_overlay", "green_layer", team == "green")
            player.interfaces.sendVisibility("rc_guild_overlay", "yellow_layer", team == "yellow")
        }

        fun leave(player: Player) {
            player.clear("gop_team")
            player.close("rc_guild_overlay")
        }
    }
}
