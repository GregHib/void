package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.target
import content.entity.combat.underAttack
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class GiantRockCrabs : Script {

    init {
        huntPlayer("boulder*", "aggressive") { target ->
            if (transform.startsWith("giant_rock_crab")) {
                return@huntPlayer
            }

            transform(id.replace("boulder", "giant_rock_crab"))

            // Give a short delay before attacking (they "stand up")
            softQueue("stand_up", 2) {
                interactPlayer(target, "Attack")
                scheduleReset(this@huntPlayer) // start reset countdown
            }
        }
    }

    /**
     * Reset the Rock Crab to its disguised form if out of combat for 30s.
     */
    fun scheduleReset(npc: NPC) {
        npc.softQueue("reset_to_boulder", TimeUnit.SECONDS.toTicks(30)) {
            if (npc.target != null || npc.underAttack) {
                scheduleReset(npc) // still in combat, reschedule
            } else {
                npc.transform(npc.id.replace("giant_rock_crab", "boulder"))
            }
        }
    }

    /**
     * When a player comes close, disguised crabs transform and attack.
     */
}
