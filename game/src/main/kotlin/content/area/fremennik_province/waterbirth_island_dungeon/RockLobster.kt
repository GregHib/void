package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.transform
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class RockLobster {

    init {
        huntPlayer("rock_hidden_lobster", "aggressive") { npc ->
            // already in combat form?
            if (npc.transform == "rock_lobster") {
                npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
                return@huntPlayer
            }

            // transform into combat form
            npc.transform("rock_lobster")

            // stand-up delay
            npc.softQueue("stand_up", 2) {
                npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
                resetToHidden(npc)
            }
        }
    }

    /**
     * Resets Rock Lobster to hidden form after 30s of no combat.
     */
    fun resetToHidden(npc: NPC) {
        npc.softQueue("reset_hidden", TimeUnit.SECONDS.toTicks(30)) {
            if (npc.inCombat || npc.target != null) {
                resetToHidden(npc)
            } else {
                if (npc.transform == "rock_lobster") {
                    npc.transform("rock_hidden_lobster")
                }
            }
        }
    }

    /**
     * Rock Lobster disguised as a rock – transform when player walks nearby.
     */
}
