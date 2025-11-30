package content.area.misthalin.lumbridge

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class CowFieldSignpost : Script {

    var cowDeathsToday: Int = 0

    init {
        npcDeath {
            if (!id.contains("cow")) return@npcDeath
            cowDeathsToday += 1
        }
        objectOperate("Read", "lumbridge_signpost_cow") {
            if (cowDeathsToday > 0) {
                statement("Local cowherders have reported that $cowDeathsToday cows have been slain in this field today by passing adventurers. Farmers throughout the land fear this may be an epidemic.")
            } else {
                statement("The Lumbridge cow population has been thriving today, without a single cow death to worry about!")
            }
        }
    }
}
