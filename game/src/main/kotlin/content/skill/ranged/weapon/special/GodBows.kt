package content.skill.ranged.weapon.special

import content.entity.combat.hit.*
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

@Script
class GodBows : Api {

    var Player.restoration: Int
        get() = this["restoration", 0]
        set(value) {
            this["restoration"] = value
        }

    val specialHandler: suspend CombatAttack.(Player) -> Unit = combatAttack@{ source ->
        if (!special) {
            return@combatAttack
        }
        when (weapon.id) {
            "zamorak_bow" -> target.hit(source, weapon, type, CLIENT_TICKS.toTicks(delay), spell, special, type, damage)
            "saradomin_bow" -> {
                source.restoration += damage * 2
                source["restoration_amount"] = source.restoration / 10
                source.softTimers.start("restorative_shot")
            }
            "guthix_bow" -> {
                source.restoration += (damage * 1.5).toInt()
                source["restoration_amount"] = source.restoration / 10
                source.softTimers.start("balanced_shot")
            }
        }
    }
    val hitHandler: suspend CombatDamage.(Character) -> Unit = { character ->
        if (special) {
            character.gfx("${weapon.id}_special_impact")
            source.sound("god_bow_special_impact")
        }
    }

    @Timer("restorative_shot,balanced_shot")
    override fun start(player: Player, timer: String, restart: Boolean) = TimeUnit.SECONDS.toTicks(6)

    @Timer("restorative_shot,balanced_shot")
    override fun tick(player: Player, timer: String): Int {
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

    @Timer("restorative_shot,balanced_shot")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.clear("restoration")
        player.clear("restoration_amount")
    }

    init {
        combatAttack("saradomin_bow", handler = specialHandler)

        combatAttack("guthix_bow", handler = specialHandler)

        combatAttack("zamorak_bow", handler = specialHandler)

        combatDamage("saradomin_bow", handler = hitHandler)

        combatDamage("guthix_bow", handler = hitHandler)

        combatDamage("zamorak_bow", handler = hitHandler)
    }
}
