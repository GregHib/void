package content.area.asgarnia.mudskipper_point

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

class MudskipperPointGulls(val patrols: PatrolDefinitions) : Script {

    init {
        npcDefaultMode("gull_mudskipper_point") { patrol("gull_mudskipper_point") }
        npcDefaultMode("gull_mudskipper_point_2") { patrol("gull_mudskipper_point_2") }
    }

    // Only the gull spawned at the start of each route patrols it
    private fun NPC.patrol(route: String): Mode? {
        val patrol = patrols.get(route)
        if (get<Tile>("spawn_tile") != patrol.waypoints.first().first) {
            return null
        }
        return Patrol(this, patrol.waypoints, noCollision = true, resume = true)
    }
}
