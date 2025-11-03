package content.entity.player.effect.energy

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.Rest

class Resting : Script {

    init {
        interfaceOption("Rest", id = "energy_orb:*") {
            if (get("movement", "walk") == "rest") {
                message("You are already resting.")
            } else {
                mode = Rest(this, -1)
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
