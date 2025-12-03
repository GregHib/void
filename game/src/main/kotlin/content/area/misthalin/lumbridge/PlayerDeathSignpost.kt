package content.area.misthalin.lumbridge

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings

class PlayerDeathSignpost : Script {
    var playerDeathsToday: Int = 0

    init {
        playerDeath {
            playerDeathsToday += 1
        }
        objectOperate("Read", "lumbridge_signpost_death") {
            if (playerDeathsToday > 0) {
                statement("So far today, $playerDeathsToday unlucky adventurers have died on ${Settings["server.name"]} and been sent to their respawn location. Be careful out there.")
            } else {
                statement("So far today, not a single adventurer on ${Settings["server.name"]} has met their end grisly or otherwise. Either the streets are getting safer or adventurers are getting warier.")
            }
        }
    }
}
