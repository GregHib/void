package content.area.fremennik_province.rellekka

import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.transform
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Resets the Rock Crab to a disguised rock variant after 30s of no combat.
 */
fun inactive(npc: NPC) {
    npc.softQueue("inactivity", TimeUnit.SECONDS.toTicks(30)) {
        if (npc.target != null || npc.inCombat) {
            inactive(npc) // still fighting, reschedule
        } else {
            // Randomly transform back to one of the disguised rocks
            npc.transform(listOf("rock", "rock_1").random())
        }
    }
}

/**
 * Hunt player handler for all disguised Rock Crabs.
 * Uses wildcard "rock*" to cover multiple disguised forms.
 */
huntPlayer("rock*", "aggressive") { npc ->
    // Skip if already in combat form
    if (npc.transform == "rock_crab") {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        return@huntPlayer
    }
    // Transform immediately to combat form
    npc.transform("rock_crab")
    // Attack the player after a short delay
    npc.softQueue("stand_up", 2) {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        inactive(npc) // start the reset timer
    }
}
