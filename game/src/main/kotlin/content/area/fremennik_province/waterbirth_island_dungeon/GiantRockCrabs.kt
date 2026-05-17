package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.clearTransform
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class GiantRockCrabs : Script {

    init {
        /**
         * When a player comes close, disguised crabs transform and attack.
         */
        huntPlayer("boulder*", "aggressive") { target ->
            if (transform.startsWith("giant_rock_crab")) {
                return@huntPlayer
            }

            transform(id.replace("boulder", "giant_rock_crab"))

            // Give a short delay before attacking (they "stand up")
            queue("stand_up", 2) {
                interactPlayer(target, "Attack")
                scheduleReset(this@huntPlayer) // start reset countdown
            }
        }
    }

    /**
     * Reset the Rock Crab to its disguised form if out of combat for 30s.
     */
    fun scheduleReset(npc: NPC) {
        npc.queue("reset_to_boulder", TimeUnit.SECONDS.toTicks(30)) {
            if (npc.target != null || npc.inCombat) {
                scheduleReset(npc) // still in combat, reschedule
                return@queue
            }
            npc.clearTransform()
            npc.mode = EmptyMode
        }
    }
}
