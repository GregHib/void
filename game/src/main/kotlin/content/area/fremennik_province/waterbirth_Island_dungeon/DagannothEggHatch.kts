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
 * Resets a dagannoth spawn back into an egg using Void-native NPC methods.
 */
fun resetDagannothToEgg(npc: NPC) {
    npc.softQueue("dagannoth_inactive", TimeUnit.SECONDS.toTicks(30)) {
        if (npc.target != null || npc.inCombat) {
            resetDagannothToEgg(npc) // still in combat
        } else if (npc.transform.startsWith("dagannoth_spawn")) {
            npc.transform("dagannoth_egg") // Void uses names, not numeric IDs
        }
    }

    // Forced 5-minute respawn
    npc.softQueue("dagannoth_respawn", TimeUnit.MINUTES.toTicks(5)) {
        if (!npc.transform.startsWith("dagannoth_egg")) {
            if (!npc.inCombat && npc.target == null) {
                npc.transform("dagannoth_egg") // respawn egg at same position
            } else {
                resetDagannothToEgg(npc) // retry
            }
        }
    }
}

/**
 * Dagannoth egg hatch in Void.
 * When a player enters hunt radius, egg transforms to spawn and attacks.
 */
huntPlayer("dagannoth_egg*", "aggressive") { npc ->

    // Already spawned? just attack
    if (!npc.transform.startsWith("dagannoth_egg")) {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        return@huntPlayer
    }

    // Transform to active spawn (Void style)
    npc.transform("dagannoth_spawn")

    // Small stand-up delay before attacking
    npc.softQueue("dagannoth_hatch_attack", 2) {
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        resetDagannothToEgg(npc) // start Void-style reset timers
    }
}
