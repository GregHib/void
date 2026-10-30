package content.area

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

class Gulls(private val patrols: PatrolDefinitions) : Script {

    init {
        gullPatrols.forEach { gull ->
            npcDefaultMode(gull.route) { patrol(gull) }
        }
    }

    // Only the gull spawned at the start of each route patrols it
    private fun NPC.patrol(gull: GullPatrol): Mode? {
        val patrol = patrols.get(gull.route)
        if (get<Tile>("spawn_tile") != patrol.waypoints.first().first) {
            return null
        }
        return Patrol(this, patrol.waypoints, noCollision = gull.noCollision, resume = true)
    }

    private data class GullPatrol(val route: String, val noCollision: Boolean = false)

    companion object {
        private val gullPatrols =
            listOf(
                GullPatrol("gull_draynor"),
                GullPatrol("gull_draynor_2"),
                GullPatrol("gull_mudskipper_point", noCollision = true),
                GullPatrol("gull_mudskipper_point_2", noCollision = true),
            )
    }
}
