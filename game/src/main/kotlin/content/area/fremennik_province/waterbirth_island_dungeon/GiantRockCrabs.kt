package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.transform
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class GiantRockCrabs {

    init {
        huntPlayer("boulder*", "aggressive") { npc ->
            if (npc.transform.startsWith("giant_rock_crab")) {
                return@huntPlayer
            }

            npc.transform(npc.id.replace("boulder", "giant_rock_crab"))

            // Give a short delay before attacking (they "stand up")
            npc.softQueue("stand_up", 2) {
                npc.interactPlayer(target, "Attack")
                scheduleReset(npc) // start reset countdown
            }
        }
    }

    /**
     * Reset the Rock Crab to its disguised form if out of combat for 30s.
     */
    fun scheduleReset(npc: NPC) {
        npc.softQueue("reset_to_boulder", TimeUnit.SECONDS.toTicks(30)) {
            if (npc.target != null || npc.inCombat) {
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
