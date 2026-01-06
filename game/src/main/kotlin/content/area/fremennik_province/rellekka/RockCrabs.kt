package content.area.fremennik_province.rellekka

import content.entity.combat.underAttack
import content.entity.combat.target
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class RockCrabs : Script {

    init {
        huntPlayer("rock*", "aggressive") { target ->
            // already a crab? just aggro the player
            if (transform.startsWith("rock_crab")) {
                interactPlayer(target, "Attack")
                return@huntPlayer
            }

            // pick correct crab form based on rock variant
            val combatForm = when (id) {
                "rock" -> "rock_crab"
                "rock_1" -> "rock_crab_1"
                else -> return@huntPlayer
            }

            // transform into crab
            transform(combatForm)

            // short stand-up delay before attacking
            softQueue("rock_stand_up", 2) {
                interactPlayer(target, "Attack")
                resetToRock(this@huntPlayer) // start inactivity timer for disguise reset
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
            if (npc.target != null || npc.underAttack) {
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
