package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

class Excalibur : Script {

    init {
        timerStart("sanctuary") { 4 }
        timerTick("sanctuary", ::tick)
        timerStop("sanctuary") { clear("sanctuary") }
        specialAttackPrepare("sanctuary") { id ->
            if (!SpecialAttack.drain(this)) {
                return@specialAttackPrepare false
            }
            anim("${id}_special")
            gfx("${id}_special")
            sound("${id}_special")
            say("For Camelot!")
            if (weapon.id.startsWith("enhanced")) {
                levels.boost(Skill.Defence, multiplier = 0.15)
                set(id, TimeUnit.SECONDS.toTicks(if (seersVillageEliteTasks(this)) 24 else 12) / 4)
                softTimers.start(id)
            } else {
                levels.boost(Skill.Defence, amount = 8)
            }
            return@specialAttackPrepare false
        }
    }

    fun tick(player: Player): Int {
        val cycle = player["sanctuary", 1] - 1
        player["sanctuary"] = cycle
        if (cycle <= 0) {
            return Timer.CANCEL
        }
        player.levels.restore(Skill.Constitution, 40)
        return Timer.CONTINUE
    }

    fun seersVillageEliteTasks(player: Player) = false
}
