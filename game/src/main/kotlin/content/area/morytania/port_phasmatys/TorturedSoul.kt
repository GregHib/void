package content.area.morytania.port_phasmatys

import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class TorturedSoul : Script {
    init {
        npcDeath("tortured_soul") {
            say(when(random.nextInt(3)) {
                0 -> "Your soul will be forfeit for this, mortal!"
                1 -> "I will return, mortal!"
                else -> "You cannot kill the undead, mortal!"
            })
        }
    }
}