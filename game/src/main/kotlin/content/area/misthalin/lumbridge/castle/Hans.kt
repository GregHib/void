package content.area.misthalin.lumbridge.castle

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue

class Hans(
    private val patrols: PatrolDefinitions,
) : Script {

    init {
        npcSpawn("hans") {
            val patrol = patrols.get("hans")
            if (patrol.waypoints.isNotEmpty()) {
                set("hans_patrol_index", 0)
                mode = Patrol(this, patrol.waypoints)
            }
        }

        npcMoved("hans") {
            if (mode is Patrol) {
                set("hans_patrol_index", get("patrol_index", 0))
            }
        }

        npcOperate("Talk-to", "hans") { (target) ->
            var resumeFromNearest = false
            var resumeDelay = 10
            scheduleResume(this, target, resumeDelay) { resumeFromNearest }
            npc<Neutral>("Hello. What are you doing here?")
            choice {
                option<Neutral>("I'm looking for whoever is in charge of this place.") {
                    npc<Neutral>("Who, the Duke? He's in his study, on the first floor.")
                }
                option<EvilLaugh>("I have come to kill everyone in this castle!") {
                    resumeFromNearest = true
                    resumeDelay = 15
                    scheduleResume(this, target, resumeDelay) { resumeFromNearest }
                    target.say("Help! Help!")
                    target.mode = Retreat(target, this)
                }
                option<Confused>("I don't know. I'm lost. Where am I?") {
                    npc<Neutral>("You are in Lumbridge Castle.")
                }
            }
        }
    }

    private fun scheduleResume(player: Player, target: NPC, delay: Int, nearest: () -> Boolean) {
        target.queue.clear("hans_resume_patrol")
        target.queue("hans_resume_patrol", delay) {
            if (player.dialogue != null) {
                scheduleResume(player, target, 1, nearest)
                return@queue
            }
            resumePatrol(target, nearest = nearest())
        }
    }

    private fun resumePatrol(target: NPC, nearest: Boolean = false) {
        val patrol = patrols.get("hans")
        if (patrol.waypoints.isEmpty()) {
            return
        }
        val size = patrol.waypoints.size
        val baseIndex = if (nearest) {
            patrol.waypoints.withIndex()
                .minByOrNull { target.tile.distanceTo(it.value.first) }
                ?.index ?: 0
        } else {
            target["hans_patrol_index", 0].mod(size)
        }
        val resumeIndex = if (patrol.waypoints[baseIndex].first == target.tile) (baseIndex + 1).mod(size) else baseIndex
        target.mode = Patrol(target, patrol.waypoints)
        target.set("patrol_index", resumeIndex)
        target.steps.clear()
    }
}
