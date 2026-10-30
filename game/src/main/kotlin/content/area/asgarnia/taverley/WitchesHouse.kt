package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol

class WitchesHouse(val patrols: PatrolDefinitions) : Script {

    init {
        npcDefaultMode("nora_t_hagg") {
            Patrol(this, patrols.get("nora_t_hagg").waypoints, resume = true)
        }
    }
}
