package content.skill.ranged.weapon.special

import content.entity.combat.hit.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.*
import content.entity.sound.sound
import java.util.concurrent.TimeUnit

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
        "zamorak_bow" -> target.hit(source, weapon, type, mark, CLIENT_TICKS.toTicks(delay), spell, special, damage)
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
combatAttack("saradomin_bow", handler = specialHandler)
combatAttack("guthix_bow", handler = specialHandler)
combatAttack("zamorak_bow", handler = specialHandler)

val hitHandler: suspend CombatDamage.(Character) -> Unit = { character ->
    if (special) {
        character.gfx("${weapon.id}_special_impact")
        source.sound("god_bow_special_impact")
    }
}
combatDamage("saradomin_bow", handler = hitHandler)
combatDamage("guthix_bow", handler = hitHandler)
combatDamage("zamorak_bow", handler = hitHandler)

timerStart("restorative_shot", "balanced_shot") {
    interval = TimeUnit.SECONDS.toTicks(6)
}

timerTick("restorative_shot", "balanced_shot") { player ->
    val amount = player.restoration
    if (amount <= 0) {
        cancel()
        return@timerTick
    }
    val restore = player["restoration_amount", 0]
    player.restoration -= restore
    player.levels.restore(Skill.Constitution, restore)
    player.gfx("saradomin_bow_restoration")
}

timerStop("restorative_shot", "balanced_shot") { player ->
    player.clear("restoration")
    player.clear("restoration_amount")
}