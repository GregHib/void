package content.area.misthalin.draynor_village

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

class DraynorGulls(val patrols: PatrolDefinitions) : Script {

    init {
        npcDefaultMode("gull_draynor") { patrol("gull_draynor") }
        npcDefaultMode("gull_draynor_2") { patrol("gull_draynor_2") }
    }

    // Only the gull spawned at the start of each route patrols it
    private fun NPC.patrol(route: String): Mode? {
        val patrol = patrols.get(route)
        if (get<Tile>("spawn_tile") != patrol.waypoints.first().first) {
            return null
        }
        return Patrol(this, patrol.waypoints, resume = true)
    }
}
