package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class RockLobster : Script {

    init {
        huntPlayer("rock_hidden_lobster", "aggressive") { target ->
            // already in combat form?
            if (transform == "rock_lobster") {
                interactPlayer(target, "Attack")
                return@huntPlayer
            }

            // transform into combat form
            transform("rock_lobster")

            // stand-up delay
            softQueue("stand_up", 2) {
                interactPlayer(target, "Attack")
                resetToHidden(this@huntPlayer)
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
