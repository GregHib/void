package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot
import java.util.concurrent.TimeUnit

fun isSpell(spell: String) = spell.startsWith("miasmic_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("${spell}_cast")
    player.setGraphic("${spell}_cast")
    player.shoot(spell, target)
    player["spell_damage"] = maxHit(spell)
    player["spell_experience"] = experience(spell)
    if (player.hit(target) != -1) {
        target.start("miasmic", TimeUnit.SECONDS.toTicks(seconds(spell)))
    }
    delay = 5
}

fun meleeOrRanged(type: String) = type == "range" || type == "melee"

on<CombatSwing>({ delay != null && delay!! > 0 && it.hasEffect("miasmic") && meleeOrRanged(getWeaponType(it, it.weapon)) }, Priority.LOWEST) { _: Player ->
    delay = delay!! * 2
}

fun maxHit(spell: String) = when (spell) {
    "miasmic_rush" -> 180
    "miasmic_burst" -> 240
    "miasmic_blitz" -> 280
    "miasmic_barrage" -> 320
    else -> 0
}

fun experience(spell: String) = when (spell) {
    "miasmic_rush" -> 36.0
    "miasmic_burst" -> 42.0
    "miasmic_blitz" -> 48.0
    "miasmic_barrage" -> 54.0
    else -> 0.0
}

fun seconds(spell: String) = when (spell) {
    "miasmic_rush" -> 12
    "miasmic_burst" -> 24
    "miasmic_blitz" -> 36
    "miasmic_barrage" -> 48
    else -> 0
}