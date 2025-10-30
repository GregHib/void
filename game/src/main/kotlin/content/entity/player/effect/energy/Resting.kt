package content.entity.player.effect.energy

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.npc.npcOperate

class Resting : Script {

    init {
        interfaceOption("Rest", id = "energy_orb") {
            if (player["movement", "walk"] == "rest") {
                player.message("You are already resting.")
            } else {
                player.mode = Rest(player, -1)
            }
        }

        npcOperate("Listen-to") {
            arriveDelay()
            if (def["song", -1] != -1) {
                player.mode = Rest(player, def["song"])
            }
        }
    }
}
