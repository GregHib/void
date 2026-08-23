package content.area.misthalin.draynor_village

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol

class DraynorGulls(val patrols: PatrolDefinitions) : Script {

    init {
        npcSpawn("gull_draynor") {
            val patrol = patrols.get("gull_draynor")
            if (tile != patrol.waypoints.first().first) {
                return@npcSpawn
            }
            mode = Patrol(this, patrol.waypoints)
        }

        npcSpawn("gull_draynor_2") {
            val patrol = patrols.get("gull_draynor_2")
            if (tile != patrol.waypoints.first().first) {
                return@npcSpawn
            }
            mode = Patrol(this, patrol.waypoints)
        }
    }
}
