package content.entity.player.effect.energy

import content.entity.combat.target
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest

class Resting : Script {

    init {
        interfaceOption("Rest", id = "energy_orb") {
            if (player["movement", "walk"] == "rest") {
                player.message("You are already resting.")
            } else {
                player.mode = Rest(player, -1)
            }
        }

        npcOperate("Listen-to") { (target) ->
            arriveDelay()
            val def = target.def(this)
            if (def["song", -1] != -1) {
                mode = Rest(this, def["song"])
            }
        }
    }
}
