package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class WitchesHouse : Api {

    val patrols: PatrolDefinitions by inject()

    init {
        npcSpawn("nora_t_hagg") { npc ->
            val patrol = patrols.get("nora_t_hagg")
            npc.mode = Patrol(npc, patrol.waypoints)
        }
    }
}
