package content.entity.player.effect.energy

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.mode.interact.arriveDelay
import world.gregs.voidps.engine.event.Script

@Script
class Resting : Api {

    init {
        interfaceOption("Rest", id = "energy_orb") {
            if (player["movement", "walk"] == "rest") {
                player.message("You are already resting.")
            } else {
                player.mode = Rest(player, -1)
            }
        }

        npcOperate("Listen-to") { player, target ->
            player.arriveDelay()
            val def = target.def(player)
            if (def["song", -1] != -1) {
                player.mode = Rest(player, def["song"])
            }
        }
    }
}
