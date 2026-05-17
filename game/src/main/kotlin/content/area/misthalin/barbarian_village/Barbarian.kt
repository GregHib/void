package content.area.misthalin.barbarian_village

import content.entity.combat.inCombat
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class Barbarian : Script {
    init {
        val barbarians = "barbarian_alberich,barbarian_fafner,barbarian_fasolt,barbarian_siegfried,barbarian_minarch,barbarian_brunnhilde,barbarian_edelschwarz"
        npcOperate("Talk-to", barbarians) { (target) ->
            when (random.nextInt(5)) {
                0 -> npc<Quiz>("Wanna fight?")
                1 -> npc<Quiz>("Ah, you come for fight, ja?")
                2 -> npc<Angry>("You look funny.")
                3 -> npc<Angry>("Grrr!")
                4 -> npc<Quiz>("What you want?")
                else -> npc<Angry>("Go Away!")
            }
            target.interactPlayer(this, "Attack")
        }

        npcCombatStart {
            if (!id.startsWith("barbarian")) {
                return@npcCombatStart
            }
            softTimers.start("barbarian_war_cry")
        }

        npcTimerStart("barbarian_war_cry") {
            random.nextInt(5, 20)
        }

        npcTimerTick("barbarian_war_cry") {
            if (!inCombat) {
                softTimers.stop("barbarian_war_cry")
                return@npcTimerTick Timer.CANCEL
            }
            say("FOR GUNNAR!")
            Timer.CONTINUE
        }

        npcDeath(barbarians) {
            softTimers.stop("barbarian_war_cry")
        }
    }
}
