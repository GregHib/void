package content.area.kharidian_desert.kalphite_lair

import content.entity.combat.attackers
import content.entity.effect.clearTransform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class KalphiteQueen : Script {
    init {
        npcCombatDamage("kalphite_queen") {
            // TODO 100% ranged and magic accuracy
            // TODO drain 1 prayer on spines damage
            // TODO 1/20 chance of spawning worker within X tiles
        }

        combatDamage {
            // TODO lightning bounce
        }

        npcLevelChanged(Skill.Constitution, "kalphite_queen") { _, _, to ->
            if (to > 10) {
                return@npcLevelChanged
            }
            levels.clear()
            for (attacker in attackers) {
                attacker.mode = EmptyMode
            }
            // TODO transform on death
            softTimers.start("kalphite_queen_revert")
        }

        npcTimerStart("kalphite_queen_revert") {
            TimeUnit.MINUTES.toTicks(20)
        }

        npcTimerTick("kalphite_queen_revert") {
            // TODO what if in combat?
            clearTransform()
            Timer.CANCEL
        }

    }

}