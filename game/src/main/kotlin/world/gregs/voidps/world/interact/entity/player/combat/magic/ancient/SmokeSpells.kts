package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.isMultiTargetSpell
import world.gregs.voidps.world.interact.entity.player.poison
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.random.Random

fun isSpell(spell: String) = spell.startsWith("smoke_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (isMultiTargetSpell(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    player["spell_damage"] = maxHit(spell)
    player["spell_experience"] = experience(spell)
    if (player.hit(target) != -1 && Random.nextDouble() <= 0.2) {
        player.poison(target, damage(spell))
    }
    delay = 5
}

fun maxHit(spell: String) = when (spell) {
    "smoke_rush" -> 130
    "smoke_burst" -> 170
    "smoke_blitz" -> 230
    "smoke_barrage" -> 270
    else -> 0
}

fun experience(spell: String) = when (spell) {
    "smoke_rush" -> 30.0
    "smoke_burst" -> 36.0
    "smoke_blitz" -> 42.0
    "smoke_barrage" -> 48.0
    else -> 0.0
}

fun damage(spell: String) = when (spell) {
    "smoke_rush" -> 20
    "smoke_burst" -> 20
    "smoke_blitz" -> 48
    "smoke_barrage" -> 40
    else -> 0
}