package content.area.misthalin.lumbridge.castle

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import org.rsmod.game.pathfinder.PathFinder
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.queue.queue

class Hans(
    private val patrols: PatrolDefinitions,
) : Script {

    private val pathFinder = PathFinder(flags = Collisions.map, useRouteBlockerFlags = true)

    init {
        npcSpawn("hans") {
            val patrol = patrols.get("hans")
            if (patrol.waypoints.isEmpty()) {
                return@npcSpawn
            }
            if (tile != patrol.waypoints.first().first) {
                return@npcSpawn
            }
            mode = Patrol(this, patrol.waypoints)
        }

        npcOperate("Talk-to", "hans") { (target) ->
            npc<Neutral>("Hello. What are you doing here?")
            choice {
                option<Neutral>("I'm looking for whoever is in charge of this place.") {
                    npc<Neutral>("Who, the Duke? He's in his study, on the first floor.")
                    resumePatrol(target)
                }
                option<EvilLaugh>("I have come to kill everyone in this castle!") {
                    target.say("Help! Help!")
                    target.mode = Retreat(target, this)
                    resumePatrol(target, delay = 10)
                }
                option<Confused>("I don't know. I'm lost. Where am I?") {
                    npc<Neutral>("You are in Lumbridge Castle.")
                    resumePatrol(target)
                }
            }
        }
    }

    private fun resumePatrol(target: world.gregs.voidps.engine.entity.character.npc.NPC, delay: Int = 0) {
        target.queue("hans_resume_patrol", delay) {
            val patrol = patrols.get("hans")
            if (patrol.waypoints.isEmpty()) {
                return@queue
            }
            val resumeIndex = patrol.waypoints.withIndex()
                .minByOrNull { target.tile.distanceTo(it.value.first) }
                ?.index ?: 0
            val waypoint = patrol.waypoints[resumeIndex].first
            val route = pathFinder.findPath(target.tile.level, target.tile.x, target.tile.y, waypoint.x, waypoint.y)
            target.steps.queueRoute(route, waypoint)
            target.set("patrol_index", resumeIndex)
            target.mode = EmptyMode
            target.mode = Patrol(target, patrol.waypoints)
        }
    }
}
