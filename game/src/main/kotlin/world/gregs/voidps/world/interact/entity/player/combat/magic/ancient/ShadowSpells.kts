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

fun isSpell(spell: String) = spell.startsWith("shadow_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (isMultiTargetSpell(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    player["spell_damage"] = maxHit(spell)
    player["spell_experience"] = experience(spell)
    if (player.hit(target) != -1) {
        target.levels.drain(Skill.Attack, multiplier = multiplier(spell))
    }
    delay = 5
}

fun maxHit(spell: String) = when (spell) {
    "shadow_rush" -> 140
    "shadow_burst" -> 180
    "shadow_blitz" -> 240
    "shadow_barrage" -> 280
    else -> 0
}

fun experience(spell: String) = when (spell) {
    "shadow_rush" -> 31.0
    "shadow_burst" -> 37.0
    "shadow_blitz" -> 43.0
    "shadow_barrage" -> 48.0
    else -> 0.0
}

fun multiplier(spell: String) = when (spell) {
    "shadow_rush" -> 0.1
    "shadow_burst" -> 0.1
    "shadow_blitz" -> 0.1
    "shadow_barrage" -> 0.15
    else -> 0.0
}