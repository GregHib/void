package content.skill.ranged.weapon.special

import content.entity.combat.hit.*
import content.social.trade.returnedItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

class GodBows : Script {

    var Player.restoration: Int
        get() = this["restoration", 0]
        set(value) {
            this["restoration"] = value
        }

    init {
        combatAttack("range") { (target, damage, type, weapon, spell, special, delay) ->
            if (!special || (weapon.id != "guthix_bow" && weapon.id != "saradomin_bow" && weapon.id != "zamorak_bow")) {
                return@combatAttack
            }
            when (weapon.id) {
                "zamorak_bow" -> target.hit(this, weapon, type, CLIENT_TICKS.toTicks(delay), spell, true, type, damage)
                "saradomin_bow" -> {
                    restoration += damage * 2
                    set("restoration_amount", restoration / 10)
                    softTimers.start("restorative_shot")
                }
                "guthix_bow" -> {
                    restoration += (damage * 1.5).toInt()
                    set("restoration_amount", restoration / 10)
                    softTimers.start("balanced_shot")
                }
            }
        }

        combatDamage("range") { (source, _, _, weapon, _, special) ->
            if (weapon.id != "guthix_bow" && weapon.id != "saradomin_bow" && weapon.id != "zamorak_bow") {
                returnedItems
            }
            if (special) {
                gfx("${weapon.id}_special_impact")
                source.sound("god_bow_special_impact")
            }
        }

        timerStart("restorative_shot") { TimeUnit.SECONDS.toTicks(6) }
        timerStart("balanced_shot") { TimeUnit.SECONDS.toTicks(6) }

        timerTick("restorative_shot", ::restore)
        timerTick("balanced_shot", ::restore)

        timerStop("restorative_shot") {
            clear("restoration")
            clear("restoration_amount")
        }

        timerStop("balanced_shot") {
            clear("restoration")
            clear("restoration_amount")
        }
    }

    fun restore(player: Player): Int {
        val amount = player.restoration
        if (amount <= 0) {
            return Timer.CANCEL
        }
        val restore = player["restoration_amount", 0]
        player.restoration -= restore
        player.levels.restore(Skill.Constitution, restore)
        player.gfx("saradomin_bow_restoration")
        return Timer.CONTINUE
    }
}
