package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.isMultiTargetSpell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell.startsWith("blood_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (isMultiTargetSpell(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    player["spell_damage"] = maxHit(spell)
    player["spell_experience"] = experience(spell)
    val damage = player.hit(target)
    if (damage != -1) {
        player.levels.restore(Skill.Constitution, (damage / 4).coerceAtMost(maximum(spell)))
    }
    delay = 5
}

fun maxHit(spell: String) = when (spell) {
    "blood_rush" -> 150
    "blood_burst" -> 210
    "blood_blitz" -> 250
    "blood_barrage" -> 290
    else -> 0
}

fun experience(spell: String) = when (spell) {
    "blood_rush" -> 33.0
    "blood_burst" -> 39.0
    "blood_blitz" -> 45.0
    "blood_barrage" -> 51.0
    else -> 0.0
}

fun maximum(spell: String) = when (spell) {
    "blood_rush" -> 40
    "blood_burst" -> 50
    "blood_blitz" -> 60
    "blood_barrage" -> 119
    else -> 0
}