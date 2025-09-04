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
 * Reset the Rock Crab to its disguised form if out of combat for 30s.
 */
fun scheduleReset(npc: NPC) {
    npc.softQueue("reset_to_boulder", TimeUnit.SECONDS.toTicks(30)) {
        if (npc.target != null || npc.inCombat) {
            scheduleReset(npc) // still in combat, reschedule
        } else {
            when (npc.transform) {
                "giant_rock_crab" -> npc.transform("Boulder")
                "giant_rock_crab_1" -> npc.transform("Boulder_1")
            }
        }
    }
}

/**
 * When a player comes close, disguised crabs transform and attack.
 */
huntPlayer("Boulder*", "aggressive") { npc ->
    // Already in combat form, do nothing
    if (npc.transform.startsWith("giant_rock_crab")) return@huntPlayer

    // Transform disguised rock into combat form
    val combatForm = when (npc.id) {
        "Boulder" -> "giant_rock_crab"
        "Boulder_1" -> "giant_rock_crab_1"
        else -> return@huntPlayer
    }
    npc.transform(combatForm)

    // Give a short delay before attacking (they "stand up")
    npc.softQueue("stand_up", 2) {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        scheduleReset(npc) // start reset countdown
    }
}