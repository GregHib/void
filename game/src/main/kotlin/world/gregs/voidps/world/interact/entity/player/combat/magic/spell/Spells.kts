package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit.HitEffectiveLevelOverride
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit
import kotlin.math.floor
import kotlin.math.round

on<HitEffectiveLevelOverride>({ defence && type == "magic" && target is Player }, priority = Priority.LOW) { _: Character ->
    target as Player
    val level = floor(target.levels.get(Skill.Magic) * 0.7)
    this.level = (floor(this.level * 0.3) + level).toInt()
}

on<HitDamageModifier>({ type == "magic" && weapon.def["magic_damage", 0] > 0 }, priority = Priority.HIGHER) { _: Character ->
    val damageMultiplier = 1.0 + (weapon.def["magic_damage", 0] / 100.0)
    this.damage = round(this.damage * damageMultiplier)
}

on<CombatHit>({ spell.isNotBlank() }) { character: Character ->
    character.setGraphic("${spell}_hit")
}

/**
 * Clear one use spell
 */
on<CombatSwing>({ it.contains("spell") }, Priority.LOWEST) { player: Player ->
    player.clear("spell")
}

on<CombatSwing>({ (delay ?: -1) >= 0 && it.spell.isNotBlank() }, Priority.LOWEST) { character: Character ->
    character.clear("spell")
    if (character is Player && !character.contains("autocast")) {
        character.queue.clearWeak()
    }
}

multiTargetHit({ Spell.isMultiTarget(spell) }, { 9 })