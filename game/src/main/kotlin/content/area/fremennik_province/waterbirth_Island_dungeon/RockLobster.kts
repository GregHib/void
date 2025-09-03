package content.area.fremennik_province.waterbirth_Island_dungeon

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
 * Resets the Rock Crab to its original disguised form after 30s of no combat.
 */
fun inactive(npc: NPC) {
    npc.softQueue("inactivity", TimeUnit.SECONDS.toTicks(30)) {
        if (npc.target != null || npc.inCombat) {
            inactive(npc) // still fighting, reschedule
        } else {
            // Transform back to correct disguise based on combat form
            val disguise: String = when (npc.transform) {
                "rock_lobster" -> "rock_hidden_lobster"
                else -> return@softQueue // if already a rock, do nothing
            }
            npc.transform(disguise)
        }
    }
}

/**
 * Hunt player handler for all disguised Rock Crabs.
 * Uses wildcard "rock*" to cover multiple disguised forms.
 */
huntPlayer("rock_hidden_lobster*", "aggressive") { npc ->
    // Skip if already in combat form
    if (npc.transform.startsWith("rock_lobster")) {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        return@huntPlayer
    }
    // Transform immediately to the correct combat form based on the rock ID
    val combatForm: String = when (npc.id) {
        "rock_hidden_lobster" -> "rock_lobster"
        else -> return@huntPlayer
    }
    npc.transform(combatForm)

    // Attack the player after a short delay
    npc.softQueue("stand_up", 2) {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        inactive(npc) // start the reset timer
    }
}
