package content.area.fremennik_province.rellekka

import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.transform
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class RockCrabs {

    init {
        huntPlayer("rock*", "aggressive") { npc ->
            // already a crab? just aggro the player
            if (npc.transform.startsWith("rock_crab")) {
                npc.interactPlayer(target, "Attack")
                return@huntPlayer
            }

            // pick correct crab form based on rock variant
            val combatForm = when (npc.id) {
                "rock" -> "rock_crab"
                "rock_1" -> "rock_crab_1"
                else -> return@huntPlayer
            }

            // transform into crab
            npc.transform(combatForm)

            // short stand-up delay before attacking
            npc.softQueue("rock_stand_up", 2) {
                npc.interactPlayer(target, "Attack")
                resetToRock(npc) // start inactivity timer for disguise reset
            }
        }
    }

    /**
     * Reset rock crab back into a rock after 30s of no combat,
     * just like real RuneScape behaviour.
     */
    fun resetToRock(npc: NPC) {
        npc.softQueue("rock_inactive", TimeUnit.SECONDS.toTicks(30)) {
            // still fighting? reschedule
            if (npc.target != null || npc.inCombat) {
                resetToRock(npc)
                return@softQueue
            }
            // transform back into correct rock disguise
            val disguise = when (npc.transform) {
                "rock_crab" -> "rock"
                "rock_crab_1" -> "rock_1"
                else -> return@softQueue // already a rock
            }
            npc.transform(disguise)
        }
    }

    /**
     * Rock crabs disguised as rocks – hatch into combat form when a player walks near.
     */
}
