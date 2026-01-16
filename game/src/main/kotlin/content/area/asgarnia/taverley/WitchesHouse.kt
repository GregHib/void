package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol

class WitchesHouse(val patrols: PatrolDefinitions) : Script {

    init {
        npcSpawn("nora_t_hagg") {
            val patrol = patrols.get("nora_t_hagg")
            mode = Patrol(this, patrol.waypoints)
        }
    }
}
